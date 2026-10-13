import { useEffect, useRef, useState } from "react";
import { HandLandmarker } from "@mediapipe/tasks-vision";
import useHandLandmarker from "../hooks/useHandLandmarker";

export default function CamaraMano({ onLandmarks }) {
  const videoRef = useRef(null);
  const canvasRef = useRef(null);
  const [estadoCamara, setEstadoCamara] = useState("iniciando");

  // Hook personalizado que maneja la inicialización del modelo de IA
  const { detector } = useHandLandmarker();

  /*
    EFECTO 1: Gestión de la cámara web.
    Solicita permisos al usuario y enlaza el flujo de video al elemento <video> del DOM.
    Incluye una función de limpieza para liberar la cámara al desmontar el componente.
  */
  useEffect(() => {
    let stream = null;
    let cancelado = false;

    async function iniciarCamara() {
      if (!navigator.mediaDevices?.getUserMedia) {
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
        setEstadoCamara("error");
      }
    }

    iniciarCamara();

    return () => {
      cancelado = true;
      if (stream) stream.getTracks().forEach((t) => t.stop());
    };
  }, []);

  /*
    EFECTO 2: Procesamiento de fotogramas y renderizado de la malla (Landmarks).
    Utiliza requestAnimationFrame para analizar el video de forma fluida sin bloquear el hilo principal.
  */
  useEffect(() => {
    if (!detector || estadoCamara !== "listo") return;

    const video = videoRef.current;
    const canvas = canvasRef.current;
    const ctx = canvas.getContext("2d");
    let idAnimacion;
    let ultimoTiempoVideo = -1;

    // Paleta de colores para diferenciar manos en el modo depuración (Canvas)
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
        // Corrección del error matemático: se agregó el operador de multiplicación (*)
        ctx.arc(p.x * canvas.width, p.y * canvas.height, 5, 0, 2 * Math.PI);
        ctx.fill();
      }
    }

    function bucle() {
      if (video.currentTime !== ultimoTiempoVideo) {
        ultimoTiempoVideo = video.currentTime;

        // Predicción de puntos sobre el fotograma actual
        const resultado = detector.detectForVideo(video, performance.now());
        ctx.clearRect(0, 0, canvas.width, canvas.height);

        // Mapeo de resultados y corrección de inversión de espejo (Left/Right)
        const manos = resultado.landmarks.map((puntos, i) => {
          const etiqueta = resultado.handednesses[i][0].categoryName;
          const mano = etiqueta === "Left" ? "Derecha" : "Izquierda";
          return { mano, etiqueta, puntos };
        });

        for (const { mano, puntos } of manos) dibujarMano(puntos, COLORES[mano]);

        // Emite las coordenadas al componente contenedor (Reconocer.jsx)
        if (onLandmarks) onLandmarks(manos);
      }
      idAnimacion = requestAnimationFrame(bucle);
    }

    bucle();

    return () => cancelAnimationFrame(idAnimacion);
  }, [detector, estadoCamara, onLandmarks]);

  /*
    INTERFAZ VISUAL:
    Se eliminaron los textos de estado locales. Ahora este componente es puramente de
    captura visual (Capa de Presentación), delegando el manejo de errores al panel derecho.
  */
  return (
    <div style={{ position: 'relative', width: '100%', height: '100%' }}>
      <video
        ref={videoRef}
        playsInline
        autoPlay
        muted
        style={{
          width: '100%',
          height: '100%',
          objectFit: 'cover',
          transform: 'scaleX(-1)',
          borderRadius: '16px'
        }}
      />
      <canvas
        ref={canvasRef}
        width={640}
        height={480}
        style={{
          position: 'absolute',
          top: 0,
          left: 0,
          width: '100%',
          height: '100%',
          transform: 'scaleX(-1)',
          pointerEvents: 'none'
        }}
      />
    </div>
  );
}