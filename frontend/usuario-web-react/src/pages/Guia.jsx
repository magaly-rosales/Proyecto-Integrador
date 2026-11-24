import { Link } from "react-router-dom";
import { IconoAyuda, IconoCamara } from "../components/Iconos";

const PASOS = [
  { titulo: "Apunta la cámara", texto: "Hacia la persona, a 1 metro más o menos." },
  { titulo: "Que se vea la mano", texto: "Con buena luz y dentro del recuadro." },
  { titulo: "Espera la seña", texto: "Cada seña se suma a la frase." },
  { titulo: "Escucha o lee", texto: "Toca “Decir en voz alta” al terminar." },
];

// Página aparte con la guía de uso (se abre desde el botón de ayuda del menú)
export default function Guia() {
  return (
    <section className="sa-guia-pagina">
      <header className="sa-guia-titulo">
        <h1>
          <IconoAyuda tamano={34} /> Cómo usar SeñasApp
        </h1>
      </header>

      <ol className="sa-guia-pasos">
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

      <Link to="/traducir" className="sa-pie-boton sa-guia-ir">
        <IconoCamara tamano={22} /> Ir a traducir
      </Link>
    </section>
  );
}
