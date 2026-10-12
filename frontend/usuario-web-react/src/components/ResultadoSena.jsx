// Muestra en grande la seña reconocida y el estado del envío al backend.
// estado: cargando | sin_mano | enviando | listo | error
const TEXTOS_ESTADO = {
  cargando: "Cargando modelo...",
  sin_mano: "Sin mano a la vista",
  enviando: "Enviando al servidor...",
  listo: "Seña reconocida",
  error: "Error de conexión",
};

export default function ResultadoSena({ sena, estado, mensajeError }) {
  let titulo = "—";
  let esAviso = false;
  if (sena === "desconocida") {
    titulo = "Seña no reconocida";
    esAviso = true;
  } else if (sena === "sin_muestras") {
    titulo = "Aún no hay muestras para este tipo de seña";
    esAviso = true;
  } else if (sena) {
    titulo = sena;
  }

  return (
    <section className="resultado">
      <p className={`estado estado-${estado}`}>{TEXTOS_ESTADO[estado]}</p>
      <p className={esAviso ? "sena-aviso" : "sena-grande"}>{titulo}</p>
      {estado === "error" && <p className="error">{mensajeError}</p>}
    </section>
  );
}