import { IconoAyuda } from "./Iconos";

const PASOS = [
  { titulo: "Apunta la cámara", texto: "Hacia la persona, a 1 metro más o menos." },
  { titulo: "Que se vea la mano", texto: "Con buena luz y dentro del recuadro." },
  { titulo: "Espera la seña", texto: "Cada seña se suma a la frase." },
  { titulo: "Escucha o lee", texto: "Toca “Decir en voz alta” al terminar." },
];

// Guía de 4 pasos que se puede ocultar
export default function GuiaUso({ visible, onAlternar }) {
  return (
    <section className="sa-t-guia" id="guia" aria-label="Cómo usar SeñasApp">
      <div className="sa-t-guia-cabeza">
        <h2>
          <IconoAyuda tamano={22} /> Cómo usar SeñasApp
        </h2>
        <button type="button" onClick={onAlternar} aria-expanded={visible}>
          {visible ? "Ocultar guía" : "Mostrar guía"}
        </button>
      </div>

      {visible && (
        <ol className="sa-t-pasos">
          {PASOS.map((p, i) => (
            <li key={p.titulo}>
              <span>{i + 1}</span>
              <div>
                <strong>{p.titulo}</strong>
                <p>{p.texto}</p>
              </div>
            </li>
          ))}
        </ol>
      )}
    </section>
  );
}
