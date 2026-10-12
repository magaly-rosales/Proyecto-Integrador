import { useEffect, useRef, useState } from "react";

// Muestra el video de la cámara. Pide permiso al montarse y la apaga al salir.
export default function CamaraMano() {
  const videoRef = useRef(null);
  const [estado, setEstado] = useState("iniciando"); // iniciando | listo | error
  const [mensajeError, setMensajeError] = useState("");

  useEffect(() => {
    let stream = null;
    let cancelado = false;

    async function iniciarCamara() {
      if (!navigator.mediaDevices?.getUserMedia) {
        setMensajeError("Tu navegador no permite acceder a la cámara.");
        setEstado("error");
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
        setEstado("listo");
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
        setEstado("error");
      }
    }

    iniciarCamara();

    // Al salir de la pantalla, se apaga la cámara
    return () => {
      cancelado = true;
      if (stream) stream.getTracks().forEach((t) => t.stop());
    };
  }, []);

  return (
    <div className="camara">
      {estado === "iniciando" && <p className="aviso">Abriendo cámara...</p>}
      {estado === "error" && <p className="error">{mensajeError}</p>}
      <video
        ref={videoRef}
        className="video-espejo"
        playsInline
        muted
        style={{ display: estado === "listo" ? "block" : "none" }}
      />
    </div>
  );
}