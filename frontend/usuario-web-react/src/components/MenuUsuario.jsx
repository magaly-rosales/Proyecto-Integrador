import { useEffect, useRef, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/useAuth";
import { usePreferencias } from "../context/usePreferencias";
import {
  IconoAjustes,
  IconoChevron,
  IconoLuna,
  IconoPerfil,
  IconoSalir,
} from "./Iconos";

// Chip "Hola, nombre" con el menú desplegable del usuario.
export default function MenuUsuario() {
  const { usuario, logout } = useAuth();
  const { temaFinal, cambiar } = usePreferencias();
  const navegar = useNavigate();
  const [abierto, setAbierto] = useState(false);
  const caja = useRef(null);

  // Se cierra al hacer clic fuera o al presionar Escape
  useEffect(() => {
    if (!abierto) return;
    const fuera = (e) => {
      if (caja.current && !caja.current.contains(e.target)) setAbierto(false);
    };
    const escape = (e) => e.key === "Escape" && setAbierto(false);
    document.addEventListener("mousedown", fuera);
    document.addEventListener("keydown", escape);
    return () => {
      document.removeEventListener("mousedown", fuera);
      document.removeEventListener("keydown", escape);
    };
  }, [abierto]);

  const inicial = (usuario.nombre || "?").charAt(0).toUpperCase();
  const primerNombre = (usuario.nombre || "").split(" ")[0];
  const oscuro = temaFinal === "oscuro";

  function salir() {
    setAbierto(false);
    logout();
    navegar("/");
  }

  return (
    <div className="sa-usuario-caja" ref={caja}>
      <button
        type="button"
        className="sa-usuario"
        onClick={() => setAbierto(!abierto)}
        aria-expanded={abierto}
        aria-haspopup="menu"
      >
        <span className="sa-avatar">{inicial}</span>
        Hola, {primerNombre}
        <IconoChevron tamano={16} />
      </button>

      {abierto && (
        <div className="sa-menu" role="menu">
          <div className="sa-menu-cabeza">
            <span className="sa-avatar sa-avatar-grande">{inicial}</span>
            <div>
              <strong>{usuario.nombre}</strong>
              <small>{usuario.email}</small>
            </div>
          </div>

          <Link to="/configuracion" className="sa-menu-item" role="menuitem" onClick={() => setAbierto(false)}>
            <IconoPerfil tamano={20} /> Mi perfil
          </Link>
          <Link to="/configuracion" className="sa-menu-item" role="menuitem" onClick={() => setAbierto(false)}>
            <IconoAjustes tamano={20} /> Configuración
          </Link>

          <div className="sa-menu-item">
            <IconoLuna tamano={20} />
            <span id="etq-oscuro">Modo oscuro</span>
            <button
              type="button"
              role="switch"
              aria-checked={oscuro}
              aria-labelledby="etq-oscuro"
              className={oscuro ? "sa-switch activo" : "sa-switch"}
              onClick={() => cambiar({ tema: oscuro ? "claro" : "oscuro" })}
            />
          </div>

          <button type="button" className="sa-menu-item sa-menu-salir" role="menuitem" onClick={salir}>
            <IconoSalir tamano={20} /> Cerrar sesión
          </button>
        </div>
      )}
    </div>
  );
}
