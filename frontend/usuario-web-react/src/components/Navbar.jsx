import { Link, NavLink } from "react-router-dom";
import { useAuth } from "../context/useAuth";
import Logo from "./Logo";
import MenuUsuario from "./MenuUsuario";
import Notificaciones from "./Notificaciones";
import { IconoAyuda, IconoCamara, IconoLibro } from "./Iconos";

const claseEnlace = ({ isActive }) => (isActive ? "sa-enlace activo" : "sa-enlace");

// Barra superior: logo a la izquierda, menú en el centro, ayuda y usuario a la derecha.
export default function Navbar() {
  const { usuario } = useAuth();

  return (
    <header className="sa-barra">
      <div className="sa-barra-in">
        <Link to="/" className="sa-logo" aria-label="SeñasApp, ir al inicio">
          <Logo tamano={104} />
          <span>
            Señas<b>App</b>
          </span>
        </Link>

        <nav className="sa-nav" aria-label="Principal">
          <NavLink to="/traducir" className={claseEnlace}>
            <IconoCamara tamano={26} /> Traducir
          </NavLink>
          <NavLink to="/senas" className={claseEnlace}>
            <IconoLibro tamano={26} /> Señas disponibles
          </NavLink>
        </nav>

        <div className="sa-derecha">
          {usuario ? (
            <>
              <Notificaciones />
              <Link to="/guia" className="sa-btn-ayuda" aria-label="Cómo usar SeñasApp" title="Cómo usar">
                <IconoAyuda tamano={28} />
              </Link>
              <MenuUsuario />
            </>
          ) : (
            <Link to="/" className="sa-boton-borde sa-btn-login">
              Iniciar sesión
            </Link>
          )}
        </div>
      </div>
    </header>
  );
}
