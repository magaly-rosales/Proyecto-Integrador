import { Link, NavLink } from "react-router-dom";
import { useAuth } from "../context/useAuth";
import { usePreferencias } from "../context/usePreferencias";
import Logo from "./Logo";
import MenuUsuario from "./MenuUsuario";
import { IconoCampana, IconoInicio, IconoTraducir } from "./Iconos";

const claseEnlace = ({ isActive }) => (isActive ? "sa-enlace activo" : "sa-enlace");

// Barra superior: logo a la izquierda; menú, notificaciones y usuario a la derecha.
export default function Navbar() {
  const { usuario } = useAuth();
  const { prefs } = usePreferencias();

  return (
    <header className="sa-barra">
      <div className="sa-barra-in">
        <Link to="/" className="sa-logo" aria-label="SeñasApp, ir al inicio">
          <Logo tamano={80} />
          <span>
            Señas<b>App</b>
          </span>
        </Link>

        <div className="sa-derecha">
          <nav className="sa-nav" aria-label="Principal">
            <NavLink to="/" end className={claseEnlace}>
              <IconoInicio tamano={18} /> Inicio
            </NavLink>
            <NavLink to="/traducir" className={claseEnlace}>
              <IconoTraducir tamano={18} /> Traducir
            </NavLink>
          </nav>

          {usuario ? (
            <>
              <span className="sa-sep" aria-hidden="true" />
              {prefs.notificaciones && (
                <button type="button" className="sa-campana" aria-label="Notificaciones">
                  <IconoCampana tamano={22} />
                  <i className="sa-punto" />
                </button>
              )}
              <MenuUsuario />
            </>
          ) : (
            <Link to="/login" className="sa-boton-borde sa-btn-login">
              Iniciar sesión
            </Link>
          )}
        </div>
      </div>
    </header>
  );
}