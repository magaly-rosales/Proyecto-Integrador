import { Link } from "react-router-dom";
import Aparecer from "../components/Aparecer";
import OndaVoz from "../components/OndaVoz";
import ParticulasFondo from "../components/ParticulasFondo";
import { useState } from "react";
import useEscribir from "../hooks/useEscribir";
import useRotar from "../hooks/useRotar";
import { IconoAmbos, IconoCamara, IconoMano, IconoTexto, IconoVoz } from "../components/Iconos";
const PALABRAS = ["Hola", "Gracias", "Por favor", "Ayuda", "Adiós"];

const TABS = [
  { id: "texto", Icono: IconoTexto, nombre: "Texto", titulo: "Texto en pantalla", texto: "Lee lo que dicen las manos en cuanto se detecta la seña.", color: "#0f766e" },
  { id: "voz", Icono: IconoVoz, nombre: "Voz", titulo: "Salida con voz", texto: "La app dice la seña en voz alta, ideal para conversar cara a cara.", color: "#c2185b" },
  { id: "manos", Icono: IconoAmbos, nombre: "Una o dos manos", titulo: "Una o dos manos", texto: "Reconoce señas hechas con una mano o con las dos, en tiempo real.", color: "#c77d00" },
];

const PASOS = [
  { n: 1, Icono: IconoCamara, titulo: "Activa la cámara", texto: "Inicia sesión y entra a Traducir." },
  { n: 2, Icono: IconoMano, titulo: "Muestra la seña", texto: "Detectamos tus manos y las comparamos con nuestras señas." },
  { n: 3, Icono: IconoVoz, titulo: "Lee o escucha", texto: "El resultado aparece como texto, voz o ambos." },
];

export default function Inicio() {
  const palabra = useRotar(PALABRAS, 3400);
  const escrita = useEscribir(palabra, 90);
  const [tab, setTab] = useState(0);
  const activa = TABS[tab];
  const [paso, setPaso] = useState(0);

  return (
    <div className="sa-inicio">
      <section className="sa-heroe">
        <ParticulasFondo />

        <div className="sa-heroe-texto">
          <span className="sa-insignia">
            <i /> Tecnología que incluye
          </span>
          <h1>
            Traducción de lengua de señas <span className="sa-resalte">en tiempo real</span>
          </h1>
          <h2>Entiende las señas, al instante.</h2>
          <p>
            Apunta la cámara a las manos de quien se comunica con señas y SeñasApp lo convierte en
            texto o en voz, para que cualquiera pueda conversar.
          </p>
          <div className="sa-traduccion">
            <OndaVoz activa />
            <div className="sa-palabras">
              <span className="sa-burbuja">“Hola”</span>
              <span className="sa-burbuja">“Por favor”</span>
            </div>
            <div className="sa-detectada">
              <small>Seña detectada</small>
              <strong>
                {escrita}
                <span className="sa-cursor" aria-hidden="true" />
              </strong>
            </div>
          </div>
          <div className="sa-botones">
            <Link to="/traducir" className="boton sa-boton-grande">
              <IconoCamara tamano={20} /> Empezar a traducir
            </Link>
          </div>
        </div>

        <div className="sa-heroe-visual">
          <div className="sa-foto">
            <img
              src="/hero.jpg"
              alt="Una mujer se comunica con señas y un hombre recibe la traducción en su computadora"
            />
          </div>
        </div>
      </section>

      <section className="sa-seccion">
        <Aparecer>
          <div className="sa-tabs">
            <div className="sa-tabs-lista" role="tablist" aria-label="Funciones de SeñasApp">
              {TABS.map((t, i) => (
                <button
                  key={t.id}
                  type="button"
                  role="tab"
                  aria-selected={i === tab}
                  className={i === tab ? "sa-tab activo" : "sa-tab"}
                  style={{ "--c": t.color }}
                  onClick={() => setTab(i)}
                >
                  <t.Icono tamano={22} /> {t.nombre}
                </button>
              ))}
            </div>

            <div className="sa-tabs-panel" role="tabpanel" key={activa.id} style={{ "--c": activa.color }}>
              <span className="sa-tabs-icono">
                <activa.Icono tamano={54} />
              </span>
              <div>
                <h3>{activa.titulo}</h3>
                <p>{activa.texto}</p>
              </div>
            </div>
          </div>
        </Aparecer>
      </section>

      <section className="sa-seccion sa-como">
        <Aparecer>
          <h2>Cómo funciona</h2>
          <div className="sa-subrayado" />
          <p className="sa-nota">Es fácil, rápido y pensado para todos.</p>
        </Aparecer>

        <ol className="sa-zigzag">
          {PASOS.map(({ n, Icono, titulo, texto }, i) => (
            <li key={n} className={i % 2 === 0 ? "sa-zz izq" : "sa-zz der"}>
              <Aparecer retraso={i * 140}>
                <div
                  className={i === paso ? "sa-zz-tarjeta activa" : "sa-zz-tarjeta"}
                  onClick={() => setPaso(i)}
                >
                  <span className="sa-zz-icono">
                    <Icono tamano={28} />
                  </span>
                  <div>
                    <strong>{titulo}</strong>
                    <p>{texto}</p>
                  </div>
                </div>
              </Aparecer>
              <button
                type="button"
                className={i === paso ? "sa-zz-num activo" : "sa-zz-num"}
                onClick={() => setPaso(i)}
                aria-label={`Paso ${n}: ${titulo}`}
                aria-pressed={i === paso}
              >
                {n}
              </button>
            </li>
          ))}
        </ol>
      </section>
    </div>
  );
}
