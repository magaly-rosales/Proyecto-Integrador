export default function ResultadoSena({ sena, estado, mensajeError }) {
  return (
    // Contenedor principal: Utiliza Flexbox en columna para distribuir el espacio verticalmente
    // y permitir que la configuración de salida (marginTop: 'auto') se fije al fondo del panel.
    <div className="contenedor-resultado" style={{ height: '100%', display: 'flex', flexDirection: 'column' }}>

      {/*
        SECCIÓN 1: Indicador de Estado
        Renderizado condicional basado en la prop 'estado' gestionada por el contenedor padre (Reconocer.jsx).
        Informa al usuario el estado de la comunicación con el modelo de IA o el backend.
      */}
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

      {/*
        SECCIÓN 2: Resultado de la Traducción
        Renderiza la predicción final devuelta por el backend.
        Contiene la estructura visual del botón Text-to-Speech (TTS), cuya lógica se implementará en la HU-02.
      */}
      <div className="traduccion-caja">
        <h1 className="texto-traducido">{sena ? sena : "Hola"}</h1>
        <button className="btn-audio-circular" title="Escuchar">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5"></polygon>
            <path d="M15.54 8.46a5 5 0 0 1 0 7.07"></path>
            <path d="M19.07 4.93a10 10 0 0 1 0 14.14"></path>
          </svg>
        </button>
      </div>

      {/*
        SECCIÓN 3: Panel de Preferencias de Usuario
        Componentes visuales para la selección del modo de salida (Texto, Voz, Ambos).
        Estructura preparada para ser conectada con los estados globales en las HU-05 y HU-06.
      */}
      <div className="configuracion-salida" style={{ marginTop: 'auto' }}>
        <h3>Modo de salida</h3>
        <div className="botones-modo">
          <button className="btn-modo">📄 Texto</button>
          <button className="btn-modo">🎙️ Voz</button>
          <button className="btn-modo activo">🔊 Ambos <span className="check-activo">✓</span></button>
        </div>
      </div>

    </div>
  );
}