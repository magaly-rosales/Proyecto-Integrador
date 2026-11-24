import { useEffect, useRef, useState } from "react";
import OndaVoz from "./OndaVoz";
import { IconoAmbos, IconoBasura, IconoBorrar, IconoChevron, IconoTexto, IconoVoz } from "./Iconos";

const SALIDAS = [
  { id: "texto", etiqueta: "Solo texto", Icono: IconoTexto },
  { id: "voz", etiqueta: "Solo voz", Icono: IconoVoz },
  { id: "ambos", etiqueta: "Texto y voz", Icono: IconoAmbos },
];

// Menú desplegable para elegir cómo se entrega la traducción
function SelectorSalida({ modo, onCambiar }) {
  const [abierto, setAbierto] = useState(false);
  const caja = useRef(null);
  const actual = SALIDAS.find((s) => s.id === modo) || SALIDAS[2];

  useEffect(() => {
    if (!abierto) return undefined;
    const fuera = (e) => caja.current && !caja.current.contains(e.target) && setAbierto(false);
    const escape = (e) => e.key === "Escape" && setAbierto(false);
    document.addEventListener("mousedown", fuera);
    document.addEventListener("keydown", escape);
    return () => {
      document.removeEventListener("mousedown", fuera);
      document.removeEventListener("keydown", escape);
    };
  }, [abierto]);

  return (
    <div className="sa-t-selector" ref={caja}>
      <button type="button" onClick={() => setAbierto(!abierto)} aria-haspopup="menu" aria-expanded={abierto}>
        <actual.Icono tamano={16} /> {actual.etiqueta} <IconoChevron tamano={14} />
      </button>
      {abierto && (
        <div className="sa-t-selector-menu" role="menu">
          {SALIDAS.map(({ id, etiqueta, Icono }) => (
            <button
              key={id}
              type="button"
              role="menuitemradio"
              aria-checked={modo === id}
              className={modo === id ? "activo" : ""}
              onClick={() => {
                onCambiar(id);
                setAbierto(false);
              }}
            >
              <Icono tamano={16} /> {etiqueta}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}

// Tarjeta "Traducción": la frase armada y los botones para decirla, borrar o limpiar
export default function ResultadoSena({
  frase,
  sena,
  estado,
  mensajeError,
  mostrarTexto,
  hablando,
  modo,
  onCambiarModo,
  onHablar,
  onBorrar,
  onLimpiar,
}) {
  let aviso = "";
  if (estado === "error") aviso = mensajeError || "Error de conexión";
  else if (estado === "listo") {
    if (sena === "desconocida") aviso = "Seña no reconocida";
    else if (sena === "dudosa") aviso = "No estoy seguro, repite la seña";
    else if (sena === "sin_muestras") aviso = "Aún no hay muestras para este tipo de seña";
  }

  const hayFrase = frase.length > 0;

  return (
    <section className="sa-t-tarjeta sa-t-traduccion" aria-label="Traducción">
      <div className="sa-t-tarjeta-cabeza">
        <span className="sa-t-etiqueta">Traducción</span>
        <SelectorSalida modo={modo} onCambiar={onCambiarModo} />
      </div>

      <div className="sa-t-frase" aria-live="polite">
        {!mostrarTexto ? (
          <span className="sa-t-frase-vacia">Solo voz activada</span>
        ) : hayFrase ? (
          frase.map((palabra, i) => (
            <span key={i} className={i === frase.length - 1 ? "sa-t-palabra ultima" : "sa-t-palabra"}>
              {palabra}
            </span>
          ))
        ) : (
          <span className="sa-t-frase-vacia">Haz una seña para empezar…</span>
        )}
      </div>

      <OndaVoz activa={hablando} />
      {aviso && <p className="sa-t-aviso" role="status">{aviso}</p>}

      <div className="sa-t-botones">
        <button type="button" className="sa-t-hablar" onClick={onHablar} disabled={!hayFrase}>
          <IconoVoz tamano={20} /> Decir en voz alta
        </button>
        <button type="button" className="sa-t-icono" onClick={onBorrar} disabled={!hayFrase} aria-label="Borrar última palabra" title="Borrar última palabra">
          <IconoBorrar tamano={20} />
        </button>
        <button type="button" className="sa-t-icono peligro" onClick={onLimpiar} disabled={!hayFrase} aria-label="Limpiar la frase" title="Limpiar la frase">
          <IconoBasura tamano={20} />
        </button>
      </div>
    </section>
  );
}
