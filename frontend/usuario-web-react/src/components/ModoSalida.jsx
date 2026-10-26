import { IconoAmbos, IconoTexto, IconoVoz } from "./Iconos";

const OPCIONES = [
  { id: "texto", etiqueta: "Texto", Icono: IconoTexto },
  { id: "voz", etiqueta: "Voz", Icono: IconoVoz },
  { id: "ambos", etiqueta: "Ambos", Icono: IconoAmbos },
];

// Selector de cómo se entrega la traducción: texto, voz o ambos.
export default function ModoSalida({ modo, onCambiar }) {
  return (
    <section className="tarjeta">
      <h3>Modo de salida</h3>
      <div className="modos" role="radiogroup" aria-label="Modo de salida">
        {OPCIONES.map(({ id, etiqueta, Icono }) => (
          <button
            key={id}
            type="button"
            role="radio"
            aria-checked={modo === id}
            className={modo === id ? "modo activo" : "modo"}
            onClick={() => onCambiar(id)}
          >
            <Icono />
            <span>{etiqueta}</span>
          </button>
        ))}
      </div>
    </section>
  );
}
