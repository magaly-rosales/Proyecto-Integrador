export default function ResultadoSena({ sena, estado, mensajeError }) {
  return (
    <div className="contenedor-resultado">

      {/* 1. Indicador de Estado */}
      {estado === "listo" && sena && (
        <div className="estado-reconocimiento">
          <span className="icono-check">✅</span> Seña reconocida
        </div>
      )}
      {estado === "error" && (
        <div className="estado-reconocimiento" style={{ color: '#EF4444' }}>
          ❌ Error: {mensajeError}
        </div>
      )}
      {estado === "sin_mano" && (
        <div className="estado-reconocimiento" style={{ color: '#6B7280' }}>
          Esperando señas...
        </div>
      )}

      {/* 2. Texto Traducido (Muestra la seña o unos puntos suspensivos) */}
      <h1 className="texto-traducido">
        {sena ? sena : "..."}
      </h1>

      {/* 3. Configuración de Salida (Preparado para la HU-05) */}
      <div className="configuracion-salida">
        <h3>Modo de salida</h3>
        <div className="botones-modo">
          <button className="btn-modo">📄 Texto</button>
          <button className="btn-modo">🎙️ Voz</button>
          <button className="btn-modo activo">🔊 Ambos</button>
        </div>
      </div>
    </div>
  );
}