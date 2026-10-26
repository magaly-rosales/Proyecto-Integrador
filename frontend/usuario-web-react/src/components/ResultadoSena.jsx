import OndaVoz from "./OndaVoz";
import { IconoCheck, IconoVoz } from "./Iconos";


export default function ResultadoSena({
  frase,
  sena,
  estado,
  mensajeError,
  mostrarTexto,
  hablando,
  onHablar,
  onBorrar,
  onLimpiar,
}) {
  let aviso = { tipo: "neutro", texto: "Sin mano a la vista" };
  if (estado === "error") {
    aviso = { tipo: "error", texto: mensajeError || "Error de conexión" };
  } else if (estado === "listo") {
    if (sena === "desconocida") aviso = { tipo: "aviso", texto: "Seña no reconocida" };
    else if (sena === "dudosa") aviso = { tipo: "aviso", texto: "No estoy seguro, repite la seña" };
    else if (sena === "sin_muestras")
      aviso = { tipo: "aviso", texto: "Aún no hay muestras para este tipo de seña" };
    else if (sena) aviso = { tipo: "ok", texto: "Seña reconocida" };
  }

  const hayFrase = frase.length > 0;

  return (
    <section className="tarjeta sa-resultado">
      <h3>Frase</h3>

      <div className="sa-frase">
        {!mostrarTexto ? (
          <span className="sa-frase-vacia">Solo voz activada</span>
        ) : hayFrase ? (
          frase.map((palabra, i) => (
            <span
              key={i}
              className={i === frase.length - 1 ? "sa-palabra nueva" : "sa-palabra"}
            >
              {palabra}
            </span>
          ))
        ) : (
          <span className="sa-frase-vacia">Haz una seña para empezar…</span>
        )}
      </div>

      <OndaVoz activa={hablando} />

      <p className={`estado estado-${aviso.tipo}`}>
        {aviso.tipo === "ok" && <IconoCheck tamano={16} />}
        {aviso.texto}
      </p>

      <div className="sa-frase-botones">
        <button type="button" className="boton sa-fbtn-hablar" onClick={onHablar} disabled={!hayFrase}>
          <IconoVoz tamano={18} /> Hablar
        </button>
        <button type="button" className="sa-fbtn" onClick={onBorrar} disabled={!hayFrase}>
          Borrar última
        </button>
        <button type="button" className="sa-fbtn" onClick={onLimpiar} disabled={!hayFrase}>
          Limpiar
        </button>
      </div>
    </section>
  );
}