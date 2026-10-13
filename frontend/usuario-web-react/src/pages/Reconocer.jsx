import { useCallback, useEffect, useRef, useState } from "react";
import CamaraMano from "../components/CamaraMano";
import ResultadoSena from "../components/ResultadoSena";
import { enviarPuntos } from "../services/api";
import './Reconocer.css';

const INTERVALO_ENVIO_MS = 250; // unos 4 envíos por segundo

export default function Reconocer() {
  const manosRef = useRef([]);
  const enviandoRef = useRef(false);

  const [infoSena, setInfoSena] = useState({
    sena: null,
    estado: "sin_mano",
    mensajeError: "",
  });

  const alDetectar = useCallback((manos) => {
    manosRef.current = manos;
  }, []);

  useEffect(() => {
    const temporizador = setInterval(async () => {
      const manos = manosRef.current;

      if (manos.length === 0) {
        setInfoSena((prev) =>
          prev.estado === "sin_mano"
            ? prev
            : { sena: null, estado: "sin_mano", mensajeError: "" }
        );
        return;
      }

      if (enviandoRef.current) return;

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

  return (
    <div className="contenido-traductor">

      {/* Título de la sección (Ya sin el logo repetido arriba) */}
      <header className="cabecera-traductor" style={{ marginBottom: '24px' }}>
        <h2 style={{ fontSize: '28px', fontWeight: '700', color: '#1E1E2F', marginBottom: '6px' }}>
          Traducir en tiempo real
        </h2>
        <p style={{ color: '#8A8AA3', fontSize: '16px' }}>
          Usa la cámara para reconocer señas al instante.
        </p>
      </header>

      {/* Estructura de dos columnas (Cámara y Resultados) */}
      <div className="grid-traductor">

        {/* Columna Izquierda: Captura de Video */}
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

        {/* Columna Derecha: Panel de Resultados */}
        <section className="panel-resultados">
          <ResultadoSena {...infoSena} />
        </section>

      </div>

      {/* Banner de Consejos inferior */}
      <div className="banner-consejo">
        <div className="icono-foco">💡</div>
        <div className="texto-consejo">
          <h4>Consejo</h4>
          <p>Para mejores resultados, asegúrate de que la mano esté bien iluminada y dentro del marco de la cámara.</p>
        </div>
      </div>

    </div>
  );
}