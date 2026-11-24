import { IconoRehacer } from "./Iconos";

// "Hace 5 min", "Hace 2 h", "Ayer"... a partir de la fecha guardada
function hace(fecha) {
  const min = Math.max(0, Math.round((Date.now() - fecha) / 60000));
  if (min < 1) return "Ahora";
  if (min < 60) return `Hace ${min} min`;
  const horas = Math.round(min / 60);
  if (horas < 24) return `Hace ${horas} h`;
  const dias = Math.round(horas / 24);
  return dias === 1 ? "Ayer" : `Hace ${dias} días`;
}

// Últimas frases traducidas en este dispositivo (solo se guardan en el navegador)
export default function FrasesRecientes({ items, onUsar }) {
  return (
    <section className="sa-t-tarjeta" aria-label="Frases recientes">
      <div className="sa-t-tarjeta-cabeza">
        <span className="sa-t-etiqueta">Frases recientes</span>
        <small>Solo en este dispositivo</small>
      </div>

      {items.length === 0 ? (
        <p className="sa-t-vacio">Aquí aparecerán las frases que traduzcas.</p>
      ) : (
        <ul className="sa-t-recientes">
          {items.map((it) => (
            <li key={it.fecha}>
              <button type="button" className="sa-t-reciente" onClick={() => onUsar(it.palabras)}>
                <span>
                  <strong>{it.palabras.join(" ")}</strong>
                  <small>{hace(it.fecha)}</small>
                </span>
                <i aria-hidden="true"><IconoRehacer tamano={16} /></i>
              </button>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
