import { useEffect, useState } from "react";
import { FilesetResolver, HandLandmarker } from "@mediapipe/tasks-vision";

// Carga MediaPipe Hands una sola vez y devuelve el detector listo para usar.
// Los archivos wasm y el modelo se descargan desde internet al abrir la página.
const WASM_URL =
  "https://cdn.jsdelivr.net/npm/@mediapipe/tasks-vision@latest/wasm";
const MODELO_URL =
  "https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/1/hand_landmarker.task";

export default function useHandLandmarker() {
  const [detector, setDetector] = useState(null);
  const [estado, setEstado] = useState("cargando"); // cargando | listo | error

  useEffect(() => {
    let cancelado = false;
    let instancia = null;

    async function cargar() {
      try {
        const vision = await FilesetResolver.forVisionTasks(WASM_URL);
        instancia = await HandLandmarker.createFromOptions(vision, {
          baseOptions: { modelAssetPath: MODELO_URL },
          runningMode: "VIDEO",
          numHands: 2,
        });
        if (cancelado) {
          instancia.close();
          return;
        }
        setDetector(instancia);
        setEstado("listo");
      } catch {
        if (!cancelado) setEstado("error");
      }
    }

    cargar();

    return () => {
      cancelado = true;
      if (instancia) instancia.close();
    };
  }, []);

  return { detector, estado };
}