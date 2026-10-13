import { useState, useEffect } from "react";

export default function ResultadoSena({ sena, estado, mensajeError }) {
  // Estado para el modo de salida, inicializado desde localStorage para que "recuerde" la elección
  const [modoSalida, setModoSalida] = useState(() => {
    return localStorage.getItem("senasapp_modo_salida") || "ambos";
  });

  // Cada vez que cambie el modo, lo guardamos en la memoria del navegador (localStorage)
  useEffect(() => {
    localStorage.setItem("senasapp_modo_salida", modoSalida);
  }, [modoSalida]);

  // Función simulada de Text-to-Speech (Voz) que se ejecutará si el modo incluye voz
  const reproducirVoz = (texto) => {
    if ((modoSalida === "voz" || modoSalida === "ambos") && 'speechSynthesis' in window) {
      const utterance = new SpeechSynthesisUtterance(texto);
      utterance.lang = 'es-ES';
      window.speechSynthesis.speak(utterance);
    }
  };

  // Disparar audio automáticamente cuando llegue una seña nueva (si el modo lo permite)
  useEffect(() => {
    if (estado === "listo" && sena) {
      reproducirVoz(sena);
    }
  }, [sena, estado]);

  return (
    <div className="contenedor-resultado" style={{ height: '100%', display: 'flex', flexDirection: 'column' }}>

      {/* 1. Indicador de Estado */}
      <div className="estado-contenedor" style={{ marginBottom: '24px' }}>
        {estado === "listo" && sena && (
          <div className="estado-reconocimiento exito">
            <span className="icono-circulo">✓</span> Seña reconocida
          </div>
        )}
        {estado === "error" && (
          <div className="estado-reconocimiento error" style={{ backgroundColor: '#FEE2E2', color: '#EF4444' }}>
            <span className="icono-circulo">✕</span> Error de conexión
          </div>
        )}
        {(estado === "sin_mano" || estado === "enviando") && (
          <div className="estado-reconocimiento gris">
            <span className="icono-circulo">...</span> Esperando señas
          </div>
        )}
      </div>

      {/* 2. Texto Traducido y Botón de Audio Manual */}
      <div className="traduccion-caja">
        <h1 className="texto-traducido">
          {sena ? sena : "Hola"}
        </h1>
        {(modoSalida === "voz" || modoSalida === "ambos") && (
          <button
            className="btn-audio-circular"
            title="Escuchar"
            onClick={() => reproducirVoz(sena || "Hola")}
          >
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5"></polygon>
              <path d="M15.54 8.46a5 5 0 0 1 0 7.07"></path>
              <path d="M19.07 4.93a10 10 0 0 1 0 14.14"></path>
            </svg>
          </button>
        )}
      </div>

      {/* 3. Configuración de Salida Interactiva */}
      <div className="configuracion-salida" style={{ marginTop: 'auto' }}>
        <h3>Modo de salida</h3>
        <div className="botones-modo">
          <button
            className={`btn-modo ${modoSalida === "texto" ? "activo" : ""}`}
            onClick={() => setModoSalida("texto")}
          >
            📄 Texto
            {modoSalida === "texto" && <span className="check-activo">✓</span>}
          </button>

          <button
            className={`btn-modo ${modoSalida === "voz" ? "activo" : ""}`}
            onClick={() => setModoSalida("voz")}
          >
            🎙️ Voz
            {modoSalida === "voz" && <span className="check-activo">✓</span>}
          </button>

          <button
            className={`btn-modo ${modoSalida === "ambos" ? "activo" : ""}`}
            onClick={() => setModoSalida("ambos")}
          >
            🔊 Ambos
            {modoSalida === "ambos" && <span className="check-activo">✓</span>}
          </button>
        </div>
      </div>
    </div>
  );
}