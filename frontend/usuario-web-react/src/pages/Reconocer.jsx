import { useCallback, useEffect, useRef, useState } from "react";
import CamaraMano from "../components/CamaraMano";
import ResultadoSena from "../components/ResultadoSena";
import { enviarPuntos } from "../services/api";

const INTERVALO_ENVIO_MS = 250; // unos 4 envíos por segundo

export default function Reconocer() {
  const manosRef = useRef([]); // manos más recientes (no provoca renders)
  const enviandoRef = useRef(false); // evita peticiones superpuestas
  const [sena, setSena] = useState(null);
  const [estado, setEstado] = useState("sin_mano"); // sin_mano | enviando | listo | error
  const [mensajeError, setMensajeError] = useState("");

  // CamaraMano llama a esto en cada frame; solo guardamos el último valor.
  const alDetectar = useCallback((manos) => {
    manosRef.current = manos;
  }, []);

  useEffect(() => {
    const temporizador = setInterval(async () => {
      const manos = manosRef.current;

      if (manos.length === 0) {
        setEstado("sin_mano");
        setSena(null);
        return;
      }
      if (enviandoRef.current) return; // todavía esperando la respuesta anterior

      enviandoRef.current = true;
      setEstado("enviando");
      try {
        const respuesta = await enviarPuntos(manos);
        setSena(respuesta.sena);
        setEstado("listo");
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
    <div>
      <CamaraMano onLandmarks={alDetectar} />
      <ResultadoSena sena={sena} estado={estado} mensajeError={mensajeError} />
    </div>
  );
}