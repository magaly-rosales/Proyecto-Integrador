import { useCallback, useEffect, useRef, useState } from "react";
import CamaraMano from "../components/CamaraMano";
import { guardarMuestra, obtenerSenas } from "../services/api";

const CUENTA_REGRESIVA_S = 3;
const MUESTRAS_RAFAGA = 20;
const INTERVALO_RAFAGA_MS = 300;

const esperar = (ms) => new Promise((resolver) => setTimeout(resolver, ms));

// Pantalla de apoyo para el equipo: graba ejemplos de cada seña para que
// el clasificador del backend pueda reconocerla después.
export default function Entrenamiento() {
  const manosRef = useRef([]); // manos más recientes (no provoca renders)
  const montadoRef = useRef(true); // para cortar la ráfaga si se sale de la pantalla
  const [senas, setSenas] = useState([]);
  const [idSena, setIdSena] = useState("");
  const [errorSenas, setErrorSenas] = useState("");
  const [cantidadManos, setCantidadManos] = useState(0);
  const [muestras, setMuestras] = useState({}); // { idSena: cantidad en esta sesión }
  const [mensaje, setMensaje] = useState(null); // { tipo: "ok" | "aviso" | "error", texto }
  const [ocupado, setOcupado] = useState(false);

  // CamaraMano llama a esto en cada frame con las manos detectadas.
  const alDetectar = useCallback((manos) => {
    manosRef.current = manos;
    setCantidadManos(manos.length); // React ignora el cambio si el valor es igual
  }, []);

  useEffect(() => {
    montadoRef.current = true;
    return () => {
      montadoRef.current = false;
    };
  }, []);

  // Carga las señas del backend para llenar el selector
  useEffect(() => {
    let cancelado = false;
    obtenerSenas()
      .then((lista) => {
        if (!cancelado) setSenas(lista);
      })
      .catch((error) => {
        if (!cancelado) setErrorSenas(error.message);
      });
    return () => {
      cancelado = true;
    };
  }, []);

  async function enviarMuestra(manos) {
    await guardarMuestra(idSena, manos);
    setMuestras((previas) => ({ ...previas, [idSena]: (previas[idSena] ?? 0) + 1 }));
  }

  async function capturarUna() {
    const manos = manosRef.current;
    if (manos.length === 0) {
      setMensaje({ tipo: "aviso", texto: "No hay ninguna mano a la vista." });
      return;
    }
    setOcupado(true);
    try {
      await enviarMuestra(manos);
      setMensaje({
        tipo: "ok",
        texto: `Muestra guardada (${manos.length} ${manos.length === 1 ? "mano" : "manos"}).`,
      });
    } catch (error) {
      setMensaje({ tipo: "error", texto: error.message });
    } finally {
      setOcupado(false);
    }
  }

  // Con las dos manos en la seña no se puede hacer clic: por eso hay una
  // cuenta regresiva y luego se graban varias muestras seguidas.
  async function capturarRafaga() {
    setOcupado(true);
    try {
      for (let s = CUENTA_REGRESIVA_S; s > 0; s--) {
        setMensaje({ tipo: "aviso", texto: `Prepara la seña... ${s}` });
        await esperar(1000);
        if (!montadoRef.current) return;
      }

      // El número de manos se fija al empezar: no se mezclan muestras de 1 y 2 manos
      const manosInicio = manosRef.current.length;
      if (manosInicio === 0) {
        setMensaje({ tipo: "aviso", texto: "No se detectó ninguna mano. Intenta de nuevo." });
        return;
      }

      let guardadas = 0;
      let intentos = 0;
      while (guardadas < MUESTRAS_RAFAGA && intentos < MUESTRAS_RAFAGA * 5) {
        intentos++;
        const manos = manosRef.current;
        if (manos.length === manosInicio) {
          await enviarMuestra(manos);
          guardadas++;
          setMensaje({ tipo: "aviso", texto: `Grabando... ${guardadas}/${MUESTRAS_RAFAGA}` });
        }
        await esperar(INTERVALO_RAFAGA_MS);
        if (!montadoRef.current) return;
      }

      setMensaje(
        guardadas === MUESTRAS_RAFAGA
          ? { tipo: "ok", texto: `Ráfaga completa: ${guardadas} muestras guardadas.` }
          : {
              tipo: "aviso",
              texto: `Se guardaron ${guardadas} de ${MUESTRAS_RAFAGA}: la mano salió de la imagen.`,
            }
      );
    } catch (error) {
      setMensaje({ tipo: "error", texto: error.message });
    } finally {
      if (montadoRef.current) setOcupado(false);
    }
  }

  const totalSesion = Object.values(muestras).reduce((suma, n) => suma + n, 0);

  return (
    <div>
      <CamaraMano onLandmarks={alDetectar} />

      <section className="panel-entrenamiento">
        {errorSenas && <p className="error">{errorSenas}</p>}

        <label>
          Seña a grabar{" "}
          <select value={idSena} onChange={(e) => setIdSena(e.target.value)} disabled={ocupado}>
            <option value="">Elige una seña…</option>
            {senas.map((s) => (
              <option key={s.idSena} value={s.idSena}>
                {s.nombre}
              </option>
            ))}
          </select>
        </label>

        <p className="aviso">
          Manos en pantalla: <strong>{cantidadManos}</strong>
          {cantidadManos > 0 && ` (${cantidadManos * 21} puntos)`}
        </p>

        <div className="acciones">
          <button onClick={capturarUna} disabled={ocupado || !idSena}>
            Capturar muestra
          </button>
          <button onClick={capturarRafaga} disabled={ocupado || !idSena}>
            Ráfaga de {MUESTRAS_RAFAGA} ({CUENTA_REGRESIVA_S} s de cuenta regresiva)
          </button>
        </div>

        {mensaje && <p className={`mensaje mensaje-${mensaje.tipo}`}>{mensaje.texto}</p>}

        <p className="aviso">
          Muestras de esta seña (esta sesión): <strong>{muestras[idSena] ?? 0}</strong> · Total:{" "}
          <strong>{totalSesion}</strong>
        </p>
        <p className="aviso">
          Graba cada seña siempre con el mismo número de manos y mueve un poco la mano
          durante la ráfaga para tener variedad.
        </p>
      </section>
    </div>
  );
}