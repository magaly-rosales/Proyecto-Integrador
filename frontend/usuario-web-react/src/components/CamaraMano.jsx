import { useEffect, useRef, useState } from "react";
import { HandLandmarker } from "@mediapipe/tasks-vision";
import useHandLandmarker from "../hooks/useHandLandmarker";

// Muestra el video, detecta la mano y dibuja los 21 puntos en un canvas.
export default function CamaraMano({ onLandmarks }) {
  const videoRef = useRef(null);
  const canvasRef = useRef(null);
  const [estadoCamara, setEstadoCamara] = useState("iniciando"); // iniciando | listo | error
  const [mensajeError, setMensajeError] = useState("");
  const [manosDetectadas, setManosDetectadas] = useState("");
  const { detector, estado: estadoModelo } = useHandLandmarker();

  // Efecto 1: abrir la cámara
  useEffect(() => {
    let stream = null;
    let cancelado = false;

    async function iniciarCamara() {
      if (!navigator.mediaDevices?.getUserMedia) {
        setMensajeError("Tu navegador no permite acceder a la cámara.");
        setEstadoCamara("error");
        return;
      }
      try {
        stream = await navigator.mediaDevices.getUserMedia({
          video: { width: 640, height: 480 },
          audio: false,
        });
        if (cancelado) {
          stream.getTracks().forEach((t) => t.stop());
          return;
        }
        videoRef.current.srcObject = stream;
        await videoRef.current.play();
        setEstadoCamara("listo");
      } catch (error) {
        if (error.name === "NotAllowedError") {
          setMensajeError(
            "Permiso de cámara denegado. Actívalo en el navegador y recarga la página."
          );
        } else if (error.name === "NotFoundError") {
          setMensajeError("No se encontró ninguna cámara en este equipo.");
        } else {
          setMensajeError("No se pudo abrir la cámara.");
        }
        setEstadoCamara("error");
      }
    }

    iniciarCamara();

    return () => {
      cancelado = true;
      if (stream) stream.getTracks().forEach((t) => t.stop());
    };
  }, []);

    // Efecto 2: detectar hasta 2 manos frame a frame y dibujar
  useEffect(() => {
    if (!detector || estadoCamara !== "listo") return;

    const video = videoRef.current;
    const canvas = canvasRef.current;
    const ctx = canvas.getContext("2d");
    let idAnimacion;
    let ultimoTiempoVideo = -1;
    let ultimoTexto = "";

    // Cada mano se dibuja con un color distinto
    const COLORES = {
      Izquierda: { linea: "#2979ff", punto: "#00e5ff" },
      Derecha: { linea: "#00c853", punto: "#ff1744" },
    };

    function dibujarMano(puntos, color) {
      ctx.strokeStyle = color.linea;
      ctx.lineWidth = 3;
      for (const { start, end } of HandLandmarker.HAND_CONNECTIONS) {
        ctx.beginPath();
        ctx.moveTo(puntos[start].x * canvas.width, puntos[start].y * canvas.height);
        ctx.lineTo(puntos[end].x * canvas.width, puntos[end].y * canvas.height);
        ctx.stroke();
      }
      ctx.fillStyle = color.punto;
      for (const p of puntos) {
        ctx.beginPath();
        ctx.arc(p.x * canvas.width, p.y * canvas.height, 5, 0, 2 * Math.PI);
        ctx.fill();
      }
    }

    function bucle() {
      if (video.currentTime !== ultimoTiempoVideo) {
        ultimoTiempoVideo = video.currentTime;
        const resultado = detector.detectForVideo(video, performance.now());

        ctx.clearRect(0, 0, canvas.width, canvas.height);

        // MediaPipe asume imagen espejada; nuestro video no está volteado,
        // por eso se intercambian las etiquetas Left/Right.
          const manos = resultado.landmarks.map((puntos, i) => {
          const etiqueta = resultado.handednesses[i][0].categoryName; // "Left" | "Right" (crudo)
          const mano = etiqueta === "Left" ? "Derecha" : "Izquierda";
          return { mano, etiqueta, puntos };
        });

        for (const { mano, puntos } of manos) dibujarMano(puntos, COLORES[mano]);

        // Solo actualiza el texto si cambió (evita renders innecesarios)
        const texto = manos.map((m) => m.mano).join(" y ");
        if (texto !== ultimoTexto) {
          ultimoTexto = texto;
          setManosDetectadas(texto);
        }

        if (onLandmarks) onLandmarks(manos);
      }
      idAnimacion = requestAnimationFrame(bucle);
    }

    bucle();

    return () => cancelAnimationFrame(idAnimacion);
  }, [detector, estadoCamara, onLandmarks]);

  return (
    <div className="camara">
      {estadoCamara === "iniciando" && <p className="aviso">Abriendo cámara...</p>}
      {estadoCamara === "error" && <p className="error">{mensajeError}</p>}
      {estadoCamara === "listo" && estadoModelo === "cargando" && (
        <p className="aviso">Cargando modelo de detección...</p>
      )}
      {estadoModelo === "error" && (
        <p className="error">
          No se pudo cargar el modelo de detección. Revisa tu conexión a internet y recarga.
        </p>
      )}
      <div className="lienzo" style={{ display: estadoCamara === "listo" ? "block" : "none" }}>
        <video ref={videoRef} className="video-espejo" playsInline muted />
        <canvas
          ref={canvasRef}
          className="canvas-espejo"
          width={640}
          height={480}
        />
      </div>
            {estadoCamara === "listo" && estadoModelo === "listo" && (
        <p className="aviso">
          {manosDetectadas
            ? `Mano detectada: ${manosDetectadas}`
            : "Sin mano a la vista"}
        </p>
      )}
    </div>
  );
}