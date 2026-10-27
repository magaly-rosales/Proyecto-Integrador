import { useCallback, useEffect, useRef, useState } from "react";
import CamaraMano from "../components/CamaraMano";
import ModoSalida from "../components/ModoSalida";
import ResultadoSena from "../components/ResultadoSena";
import { usePreferencias } from "../context/usePreferencias";
import { enviarPuntos, enviarSecuencia } from "../services/api";
import { hablar } from "../services/voz";

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

export default function Traducir() {
  const { prefs, cambiar } = usePreferencias();
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

  function hablarFrase() {
    if (fraseRef.current.length === 0) return;
    habladaRef.current = true;
    hablar(fraseRef.current.join(" "), setHablando);
  }

  function borrarUltima() {
    fraseRef.current = fraseRef.current.slice(0, -1);
    setFrase(fraseRef.current);
  }

  function limpiar() {
    fraseRef.current = [];
    habladaRef.current = true;
    setFrase([]);
  }

  function agregarPalabra(palabra) {
    fraseRef.current = [...fraseRef.current, palabra];
    habladaRef.current = false;
    setFrase(fraseRef.current);
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

  return (
    <section className="sa-pagina">
      <header className="sa-titulo-pagina">
        <h1>Traducir señas</h1>
        <p>Muestra tus manos a la cámara y elige cómo quieres recibir el resultado.</p>
      </header>

      <div className="sa-traducir">
        <div className="sa-camara-caja">
          <div className="sa-escaner">
            <CamaraMano onLandmarks={alDetectar} />
            <span className="sa-linea" aria-hidden="true" />
          </div>
          <p className="sa-consejo">
            Mantén la mano dentro del recuadro. Buena luz y fondo simple ayudan a reconocer mejor.
          </p>
        </div>

        <div className="sa-lateral">
          <ResultadoSena
            frase={frase}
            sena={sena}
            estado={estado}
            mensajeError={mensajeError}
            mostrarTexto={modo !== "voz"}
            hablando={hablando}
            onHablar={hablarFrase}
            onBorrar={borrarUltima}
            onLimpiar={limpiar}
          />
          <ModoSalida modo={modo} onCambiar={cambiarModo} />
        </div>
      </div>
    </section>
  );
}