import { useCallback, useEffect, useRef, useState } from "react";
import CamaraMano from "../components/CamaraMano";
import ModoSalida from "../components/ModoSalida";
import ResultadoSena from "../components/ResultadoSena";
import { usePreferencias } from "../context/usePreferencias";
import { enviarPuntos } from "../services/api";
import { hablar } from "../services/voz";

const INTERVALO_ENVIO_MS = 250; // unos 4 envíos por segundo
const CONFIRMACIONES = 3; // respuestas seguidas iguales para dar por buena una seña
const CICLOS_SIN_MANO = 4; // ~1 s sin mano: se puede repetir la misma seña
const CICLOS_PAUSA = 8; // ~2 s sin mano: la frase se da por terminada y se dice en voz alta

const esSenaValida = (s) =>
  Boolean(s) && s !== "desconocida" && s !== "sin_muestras" && s !== "dudosa";

export default function Traducir() {
  const { prefs, cambiar } = usePreferencias();
  const manosRef = useRef([]); // manos más recientes (no provoca renders)
  const enviandoRef = useRef(false); // evita peticiones superpuestas
  const modoRef = useRef(prefs.modo); // copia del modo para usarla dentro del temporizador
  const candidataRef = useRef({ sena: null, veces: 0 });
  const confirmadaRef = useRef(null); // seña ya agregada mientras la mano sigue a la vista
  const sinManoRef = useRef(0);
  const fraseRef = useRef([]); // palabras de la frase actual
  const habladaRef = useRef(true); // true si la frase actual ya se dijo en voz alta

  const [modo, setModo] = useState(prefs.modo); // texto | voz | ambos
  const [sena, setSena] = useState(null); // última respuesta del backend
  const [frase, setFrase] = useState([]); // palabras que se muestran
  const [estado, setEstado] = useState("sin_mano"); // sin_mano | listo | error
  const [mensajeError, setMensajeError] = useState("");
  const [hablando, setHablando] = useState(false); // para animar la onda de voz

  // CamaraMano llama a esto en cada frame; solo guardamos el último valor.
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

  useEffect(() => {
    function agregar(palabra) {
      fraseRef.current = [...fraseRef.current, palabra];
      habladaRef.current = false;
      setFrase(fraseRef.current);
    }

    // Una seña solo se da por buena si se repite varias respuestas seguidas:
    // así la frase no salta entre señas mientras se mueve la mano.
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
        agregar(valor);
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