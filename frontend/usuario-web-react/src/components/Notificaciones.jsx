import { useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { usePreferencias } from "../context/usePreferencias";
import { obtenerCatalogo } from "../services/api";
import { IconoCampana } from "./Iconos";

const CLAVE_VISTO = "senasapp-avisos-visto";
const CADA_MS = 60000; // se vuelve a consultar cada minuto

function leerVisto() {
  try {
    return Number(localStorage.getItem(CLAVE_VISTO)) || 0;
  } catch {
    return 0;
  }
}

const fechaDe = (s) => new Date(s.fecha).getTime() || 0;

function formatearFecha(valor) {
  const f = new Date(valor);
  return Number.isNaN(f.getTime()) ? "" : f.toLocaleDateString("es-PE", { day: "numeric", month: "short" });
}

// Campana de avisos: lista las señas nuevas que el administrador agregó (datos reales del catálogo).
// Se puede activar o desactivar en Configuración.
export default function Notificaciones() {
  const { prefs } = usePreferencias();
  const [nuevas, setNuevas] = useState([]);
  const [visto, setVisto] = useState(leerVisto);
  const [abierto, setAbierto] = useState(false);
  const caja = useRef(null);

  // Consulta el catálogo (el backend indica cuáles son nuevas)
  useEffect(() => {
    if (!prefs.notificaciones) return;
    let activo = true;
    const cargar = () =>
      obtenerCatalogo()
        .then((d) => {
          if (!activo) return;
          const lista = (d.senas || []).filter((s) => s.nueva).sort((a, b) => fechaDe(b) - fechaDe(a));
          setNuevas(lista);
        })
        .catch(() => {});
    cargar();
    const t = setInterval(cargar, CADA_MS);
    return () => {
      activo = false;
      clearInterval(t);
    };
  }, [prefs.notificaciones]);

  // Se cierra al hacer clic fuera o con Escape
  useEffect(() => {
    if (!abierto) return;
    const fuera = (e) => caja.current && !caja.current.contains(e.target) && setAbierto(false);
    const escape = (e) => e.key === "Escape" && setAbierto(false);
    document.addEventListener("mousedown", fuera);
    document.addEventListener("keydown", escape);
    return () => {
      document.removeEventListener("mousedown", fuera);
      document.removeEventListener("keydown", escape);
    };
  }, [abierto]);

  if (!prefs.notificaciones) return null;

  const sinLeer = nuevas.filter((s) => fechaDe(s) > visto).length;

  function alternar() {
    const abrir = !abierto;
    setAbierto(abrir);
    if (abrir && nuevas.length > 0) {
      const ultimo = Math.max(...nuevas.map(fechaDe));
      setVisto(ultimo);
      try {
        localStorage.setItem(CLAVE_VISTO, String(ultimo));
      } catch {
        /* solo se pierde el recuerdo de lo leído */
      }
    }
  }

  return (
    <div className="sa-avisos" ref={caja}>
      <button
        type="button"
        className="sa-btn-ayuda sa-avisos-btn"
        onClick={alternar}
        aria-expanded={abierto}
        aria-label={sinLeer > 0 ? `Notificaciones, ${sinLeer} sin leer` : "Notificaciones"}
        title="Notificaciones"
      >
        <IconoCampana tamano={28} />
        {sinLeer > 0 && <span className="sa-avisos-punto">{sinLeer > 9 ? "9+" : sinLeer}</span>}
      </button>

      {abierto && (
        <div className="sa-avisos-panel" role="dialog" aria-label="Notificaciones">
          <h3>Notificaciones</h3>
          {nuevas.length === 0 ? (
            <p className="sa-avisos-vacio">No hay señas nuevas por ahora.</p>
          ) : (
            <ul>
              {nuevas.map((s) => (
                <li key={s.id}>
                  <Link to="/senas" onClick={() => setAbierto(false)}>
                    <strong>Nueva seña: {s.nombre}</strong>
                    <small>
                      {s.categoria ? `${s.categoria} · ` : ""}
                      {formatearFecha(s.fecha)}
                    </small>
                  </Link>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  );
}
