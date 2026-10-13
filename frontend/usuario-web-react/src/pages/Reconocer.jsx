import { useCallback, useEffect, useRef, useState } from "react";
import CamaraMano from "../components/CamaraMano";
import ResultadoSena from "../components/ResultadoSena";
import { enviarPuntos } from "../services/api";
import './Reconocer.css'; // Importación de los estilos visuales

const INTERVALO_ENVIO_MS = 250; // unos 4 envíos por segundo

export default function Reconocer() {
  const manosRef = useRef([]); // manos más recientes (no provoca renders)
  const enviandoRef = useRef(false); // evita peticiones superpuestas

  // Estado unificado: elimina la redundancia de usar tres useState separados
  const [infoSena, setInfoSena] = useState({
    sena: null,
    estado: "sin_mano", // sin_mano | enviando | listo | error
    mensajeError: "",
  });

  // CamaraMano llama a esto en cada frame; solo guardamos el último valor.
  const alDetectar = useCallback((manos) => {
    manosRef.current = manos;
  }, []);

  useEffect(() => {
    const temporizador = setInterval(async () => {
      const manos = manosRef.current;

      if (manos.length === 0) {
        // Evita actualizar el estado repetitivamente si ya está en "sin_mano"
        setInfoSena((prev) =>
          prev.estado === "sin_mano"
            ? prev
            : { sena: null, estado: "sin_mano", mensajeError: "" }
        );
        return;
      }

      if (enviandoRef.current) return; // todavía esperando la respuesta anterior

      enviandoRef.current = true;
      setInfoSena((prev) => ({ ...prev, estado: "enviando" }));

      try {
        const { sena } = await enviarPuntos(manos);
        setInfoSena({ sena, estado: "listo", mensajeError: "" });
      } catch (error) {
        setInfoSena({ sena: null, estado: "error", mensajeError: error.message });
      } finally {
        enviandoRef.current = false;
      }
    }, INTERVALO_ENVIO_MS);

    return () => clearInterval(temporizador);
  }, []);

  // === INTERFAZ VISUAL MAQUETADA ===
  return (
    <div className="layout-principal">
      <main className="contenido-traductor">
        <header className="cabecera-traductor">
          <div className="titulos">
            <h2>Traducir en tiempo real</h2>
            <p>Usa la cámara para reconocer señas al instante.</p>
          </div>
        </header>

        <div className="grid-traductor">

          {/* COLUMNA IZQUIERDA: Área de Captura */}
          <section className="panel-camara">
            <div className="marco-video">
              <div className="indicador-envivo">
                <span className="punto-rojo"></span> En vivo
              </div>
              <CamaraMano onLandmarks={alDetectar} />
            </div>
            <button className="btn-detener-camara">
              📷 Detener cámara
            </button>
          </section>

          {/* COLUMNA DERECHA: Resultados */}
          <section className="panel-resultados">
            {/* Pasamos todas las propiedades del objeto directamente */}
            <ResultadoSena {...infoSena} />
          </section>

        </div>
      </main>
    </div>
  );
}