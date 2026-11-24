import { useEffect, useMemo, useState } from "react";
import { IconoChevron, IconoMano } from "../components/Iconos";
import { obtenerCatalogo, urlMedia } from "../services/api";
import "../estilos/catalogo.css";

const SIN_TILDES = (texto) =>
  String(texto || "").normalize("NFD").replace(/[̀-ͯ]/g, "").toLowerCase();

// Imagen de la categoría (la que se sube en el panel de administrador); si no hay, la mano
function IconoCategoria({ categoria }) {
  const [fallo, setFallo] = useState(false);
  if (categoria.imagen && !fallo) {
    return <img src={urlMedia(categoria.imagen)} alt="" onError={() => setFallo(true)} />;
  }
  return <IconoMano tamano={22} />;
}

// Imagen de la seña; si no tiene, la de su categoría; y si tampoco, la mano
function Imagen({ sena, grande = false }) {
  const [fallo, setFallo] = useState(false);
  const color = sena.color || "#2BBCC9";
  const ruta = sena.imagen || sena.imagenCategoria;
  return (
    <div className={grande ? "sa-cat-img grande" : "sa-cat-img"} style={{ "--cat": color }}>
      {ruta && !fallo ? (
        <img src={urlMedia(ruta)} alt={`Seña ${sena.nombre}`} onError={() => setFallo(true)} loading="lazy" />
      ) : (
        <IconoMano tamano={grande ? 96 : 56} />
      )}
    </div>
  );
}

// Detalle de una seña: video si tiene, y si no su imagen (se muestra en la misma página)
function Detalle({ sena, onVolver }) {
  const [videoFallo, setVideoFallo] = useState(false);
  const verVideo = sena.video && !videoFallo;
  return (
    <article className="sa-cat-detalle">
      <button type="button" className="sa-cat-volver" onClick={onVolver}>
        <IconoChevron tamano={18} /> Volver a las señas
      </button>

      <div className="sa-cat-detalle-cuerpo">
        {verVideo ? (
          <video
            className="sa-cat-video"
            src={urlMedia(sena.video)}
            controls
            autoPlay
            loop
            muted
            playsInline
            onError={() => setVideoFallo(true)}
          />
        ) : (
          <Imagen sena={sena} grande />
        )}

        <div className="sa-cat-detalle-info">
          <h2>{sena.nombre}</h2>
          <span className="sa-cat-chip" style={{ "--cat": sena.color || "#2BBCC9" }}>
            {sena.categoria}
          </span>
          {sena.descripcion && <p>{sena.descripcion}</p>}
        </div>
      </div>
    </article>
  );
}

export default function Senas() {
  const [catalogo, setCatalogo] = useState(null);
  const [error, setError] = useState("");
  const [categoria, setCategoria] = useState(null); // null = todas
  const [busqueda, setBusqueda] = useState("");
  const [elegida, setElegida] = useState(null);

  useEffect(() => {
    let vigente = true;
    obtenerCatalogo()
      .then((datos) => vigente && setCatalogo(datos))
      .catch((e) => vigente && setError(e.message));
    return () => {
      vigente = false;
    };
  }, []);

  const senas = useMemo(() => {
    if (!catalogo) return [];
    const q = SIN_TILDES(busqueda.trim());
    return catalogo.senas.filter(
      (s) => (!categoria || s.categoria === categoria) && (!q || SIN_TILDES(s.nombre).includes(q))
    );
  }, [catalogo, categoria, busqueda]);

  if (error) {
    return (
      <section className="sa-cat">
        <p className="sa-cat-estado" role="alert">{error}</p>
      </section>
    );
  }
  if (!catalogo) {
    return (
      <section className="sa-cat">
        <p className="sa-cat-estado">Cargando señas…</p>
      </section>
    );
  }

  return (
    <section className="sa-cat">
      <aside className="sa-cat-lateral">
        <input
          type="search"
          className="sa-cat-buscar"
          placeholder="Buscar seña..."
          value={busqueda}
          onChange={(e) => {
            setBusqueda(e.target.value);
            setElegida(null);
          }}
          aria-label="Buscar seña"
        />

        <h3>Categorías</h3>
        <ul className="sa-cat-lista">
          <li>
            <button
              type="button"
              className={categoria === null ? "sa-cat-item activo" : "sa-cat-item"}
              onClick={() => {
                setCategoria(null);
                setElegida(null);
              }}
            >
              <span className="sa-cat-punto"><IconoMano tamano={16} /></span>
              <span className="sa-cat-nombre">Todas</span>
              <span className="sa-cat-total">{catalogo.total}</span>
            </button>
          </li>
          {catalogo.categorias.map((c) => (
            <li key={c.nombre}>
              <button
                type="button"
                className={categoria === c.nombre ? "sa-cat-item activo" : "sa-cat-item"}
                style={{ "--cat": c.color || "#2BBCC9" }}
                onClick={() => {
                  setCategoria(c.nombre);
                  setElegida(null);
                }}
              >
                <span className="sa-cat-punto"><IconoCategoria categoria={c} /></span>
                <span className="sa-cat-nombre">{c.nombre}</span>
                {c.hayNuevas && <i className="sa-cat-nuevas" title="Tiene señas nuevas" />}
                <span className="sa-cat-total">{c.total}</span>
              </button>
            </li>
          ))}
        </ul>
      </aside>

      <div className="sa-cat-principal">
        {elegida ? (
          <Detalle sena={elegida} onVolver={() => setElegida(null)} />
        ) : (
          <>
            <header className="sa-cat-titulo">
              <h2>{categoria || "Todas las señas"}</h2>
              <span />
            </header>

            {senas.length === 0 ? (
              <p className="sa-cat-vacio">
                {catalogo.total === 0
                  ? "Todavía no hay señas entrenadas."
                  : "No se encontraron señas con ese filtro."}
              </p>
            ) : (
              <ul className="sa-cat-grid">
                {senas.map((s) => (
                  <li key={s.id}>
                    <button type="button" className="sa-cat-tarjeta" onClick={() => setElegida(s)}>
                      <Imagen sena={s} />
                      {s.nueva && <span className="sa-cat-insignia nueva">Nueva</span>}
                      {!s.nueva && s.video && <span className="sa-cat-insignia">Video</span>}
                      <strong>{s.nombre}</strong>
                      <span className="sa-cat-chip" style={{ "--cat": s.color || "#2BBCC9" }}>
                        {s.categoria}
                      </span>
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </>
        )}
      </div>
    </section>
  );
}
