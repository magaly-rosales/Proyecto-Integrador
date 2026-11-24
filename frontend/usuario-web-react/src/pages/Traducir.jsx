import { useCallback, useEffect, useRef, useState } from "react";
import CamaraMano from "../components/CamaraMano";
import FrasesRecientes from "../components/FrasesRecientes";
import { IconoPantalla } from "../components/Iconos";
import ResultadoSena from "../components/ResultadoSena";
import { usePreferencias } from "../context/usePreferencias";
import { hablar } from "../services/voz";
import { enviarPuntos, enviarSecuencia, registrarTraduccion } from "../services/api";

const INTERVALO_ENVIO_MS = 250; 
const CONFIRMACIONES = 3; 
const CICLOS_SIN_MANO = 4; 
const CICLOS_PAUSA = 8; 
const MOV_INTERVALO_MS = 100; 
const MOV_VENTANA = 20; 
const MOV_CADA = 3; 
const MOV_CONFIRMACIONES = 2; 

const esSenaValida = (s) =>
  Boolean(s) && s !== "desconocida" && s !== "sin_muestras" && s !== "dudosa";

// Frases recientes y guía: se recuerdan solo en este navegador
const CLAVE_RECIENTES = "senasapp-recientes";

function leerRecientes() {
  try {
    const v = JSON.parse(localStorage.getItem(CLAVE_RECIENTES));
    return Array.isArray(v) ? v : [];
  } catch {
    return [];
  }
}

export default function Traducir() {
  const { prefs, cambiar } = usePreferencias();
  const camaraRef = useRef(null);
  const [recientes, setRecientes] = useState(leerRecientes);
  const manosRef = useRef([]);
  const enviandoRef = useRef(false); 
  const modoRef = useRef(prefs.modo); 
  const candidataRef = useRef({ sena: null, veces: 0 });
  const confirmadaRef = useRef(null); 
  const sinManoRef = useRef(0);
  const fraseRef = useRef([]); 
  const habladaRef = useRef(true); 

  const [modo, setModo] = useState(prefs.modo); 
  const [sena, setSena] = useState(null); 
  const [frase, setFrase] = useState([]); 
  const [estado, setEstado] = useState("sin_mano"); 
  const [mensajeError, setMensajeError] = useState("");
  const [hablando, setHablando] = useState(false); 

 const alDetectar = useCallback((manos) => {
    manosRef.current = manos;
  }, []);

  function cambiarModo(nuevo) {
    modoRef.current = nuevo;
    setModo(nuevo);
    cambiar({ modo: nuevo }); // se recuerda la próxima vez
  }

  function guardarReciente(palabras) {
    if (palabras.length === 0) return;
    const texto = palabras.join(" ");
    setRecientes((previas) => {
      const nuevas = [
        { palabras: [...palabras], fecha: Date.now() },
        ...previas.filter((r) => r.palabras.join(" ") !== texto),
      ].slice(0, 3);
      try {
        localStorage.setItem(CLAVE_RECIENTES, JSON.stringify(nuevas));
      } catch {
        /* sin almacenamiento: solo se pierde el recuerdo */
      }
      return nuevas;
    });
  }

  function usarReciente(palabras) {
    fraseRef.current = [...palabras];
    habladaRef.current = true;
    setFrase(fraseRef.current);
  }

  function pantallaCompleta() {
    if (document.fullscreenElement) document.exitFullscreen?.();
    else camaraRef.current?.requestFullscreen?.();
  }

  function hablarFrase() {
    if (fraseRef.current.length === 0) return;
    guardarReciente(fraseRef.current);
    habladaRef.current = true;
    hablar(fraseRef.current.join(" "), setHablando);
  }

  function borrarUltima() {
    fraseRef.current = fraseRef.current.slice(0, -1);
    setFrase(fraseRef.current);
  }

  function limpiar() {
    guardarReciente(fraseRef.current);
    fraseRef.current = [];
    habladaRef.current = true;
    setFrase([]);
  }

    function agregarPalabra(palabra) {
    fraseRef.current = [...fraseRef.current, palabra];
    habladaRef.current = false;
    setFrase(fraseRef.current);
    registrarTraduccion(palabra).catch(() => {});
  }

  // Señas estáticas (un solo cuadro)
  useEffect(() => {
   function confirmar(valor) {
      if (!esSenaValida(valor)) {
        candidataRef.current = { sena: null, veces: 0 };
        return;
      }
      const c = candidataRef.current;
      candidataRef.current =
        c.sena === valor ? { sena: valor, veces: c.veces + 1 } : { sena: valor, veces: 1 };

      if (candidataRef.current.veces >= CONFIRMACIONES && confirmadaRef.current !== valor) {
        confirmadaRef.current = valor;
        agregarPalabra(valor);
      }
    }

    const temporizador = setInterval(async () => {
      const manos = manosRef.current;

      if (manos.length === 0) {
        sinManoRef.current += 1;
        if (sinManoRef.current >= CICLOS_SIN_MANO) {
          candidataRef.current = { sena: null, veces: 0 };
          confirmadaRef.current = null;
        }
        // pausa larga: la frase terminó, se dice en voz alta (si el modo lo permite)
        if (
          sinManoRef.current >= CICLOS_PAUSA &&
          !habladaRef.current &&
          fraseRef.current.length > 0
        ) {
          habladaRef.current = true;
          guardarReciente(fraseRef.current);
          if (modoRef.current !== "texto") hablar(fraseRef.current.join(" "), setHablando);
        }
        setEstado("sin_mano");
        setSena(null);
        return;
      }
      sinManoRef.current = 0;
      if (enviandoRef.current) return; // todavía esperando la respuesta anterior

      enviandoRef.current = true;
      try {
        const respuesta = await enviarPuntos(manos);
        setSena(respuesta.sena);
        setEstado("listo");
        confirmar(respuesta.sena);
      } catch (error) {
        setMensajeError(error.message);
        setEstado("error");
      } finally {
        enviandoRef.current = false;
      }
    }, INTERVALO_ENVIO_MS);

    return () => clearInterval(temporizador);
  }, []);

  // Señas con movimiento: se mira una ventana deslizante de los últimos ~2 s
  useEffect(() => {
    const buffer = [];
    let cuadros = 0;
    let enviando = false;
    let sinMano = 0;
    let candidata = { sena: null, veces: 0 };

    const temporizador = setInterval(async () => {
      const manos = manosRef.current;

      if (manos.length === 0) {
        sinMano += 1;
        if (sinMano >= 10) {
          // ~1 s sin mano: se empieza de cero
          buffer.length = 0;
          candidata = { sena: null, veces: 0 };
        }
        return;
      }
      sinMano = 0;

      // si cambia el número de manos, no se mezclan cuadros
      if (buffer.length > 0 && buffer[buffer.length - 1].length !== manos.length) {
        buffer.length = 0;
      }
      buffer.push(manos.map((m) => ({ etiqueta: m.etiqueta, puntos: m.puntos })));
      if (buffer.length > MOV_VENTANA) buffer.shift();

      cuadros += 1;
      if (buffer.length < MOV_VENTANA || cuadros % MOV_CADA !== 0 || enviando) return;

      enviando = true;
      try {
        const respuesta = await enviarSecuencia([...buffer]);
        const valor = respuesta.sena;
        if (!esSenaValida(valor)) {
          candidata = { sena: null, veces: 0 };
        } else {
          candidata =
            candidata.sena === valor
              ? { sena: valor, veces: candidata.veces + 1 }
              : { sena: valor, veces: 1 };
          if (candidata.veces >= MOV_CONFIRMACIONES && confirmadaRef.current !== valor) {
            confirmadaRef.current = valor; // evita repetirla mientras la mano sigue a la vista
            agregarPalabra(valor);
            buffer.length = 0; // la próxima seña empieza con ventana limpia
          }
        }
      } catch {
        // si falla una consulta se ignora; la siguiente lo intentará de nuevo
      } finally {
        enviando = false;
      }
    }, MOV_INTERVALO_MS);

    return () => clearInterval(temporizador);
  }, []);

  // Atajos: Espacio dice la frase en voz alta y Retroceso borra la última palabra
  useEffect(() => {
    function atajo(e) {
      if (/^(INPUT|TEXTAREA|SELECT|BUTTON)$/.test(e.target.tagName)) return;
      if (e.code === "Space") {
        e.preventDefault();
        hablarFrase();
      } else if (e.key === "Backspace") {
        e.preventDefault();
        borrarUltima();
      }
    }
    document.addEventListener("keydown", atajo);
    return () => document.removeEventListener("keydown", atajo);
  }, []);

  const textoEstado =
    estado === "error" ? "Sin conexión con el servidor" : estado === "listo" ? "Mano detectada" : "Sin mano a la vista";

  return (
    <section className="sa-t">
      <div className="sa-t-grid">
        <div className="sa-t-camara" ref={camaraRef}>
          <span className={estado === "listo" ? "sa-t-chip activo" : "sa-t-chip"}>
            <i aria-hidden="true" /> {textoEstado}
          </span>
          <button type="button" className="sa-t-pantalla" onClick={pantallaCompleta} aria-label="Pantalla completa" title="Pantalla completa">
            <IconoPantalla tamano={22} />
          </button>
          <div className="sa-escaner">
            <CamaraMano onLandmarks={alDetectar} />
          </div>
        </div>

        <div className="sa-t-lado">
          <ResultadoSena
            frase={frase}
            sena={sena}
            estado={estado}
            mensajeError={mensajeError}
            mostrarTexto={modo !== "voz"}
            hablando={hablando}
            modo={modo}
            onCambiarModo={cambiarModo}
            onHablar={hablarFrase}
            onBorrar={borrarUltima}
            onLimpiar={limpiar}
          />
          <FrasesRecientes items={recientes} onUsar={usarReciente} />
          <p className="sa-t-atajos">
            Atajos: <kbd>Espacio</kbd> decir en voz alta · <kbd>⌫</kbd> borrar última
          </p>
        </div>
      </div>
    </section>
  );
}