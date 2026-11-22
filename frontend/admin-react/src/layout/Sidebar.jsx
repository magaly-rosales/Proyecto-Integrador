import { NavLink } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { ICONOS, IconoSalir } from './Iconos';
import { SECCIONES } from './secciones';

function iniciales(nombre) {
    return (nombre || 'A').split(/\s+/).slice(0, 2).map((p) => p[0]).join('').toUpperCase();
}

export default function Sidebar({ resumen, onNavegar }) {
    const { usuario, cerrarSesion } = useAuth();

    return (
        <aside className="lateral" aria-label="Menú del panel">
            <div className="lateral-marca">
                <img src="/logo.png" alt="" />
                <div>
                    <strong>SeñasApp</strong>
                    <span>ADMINISTRACIÓN</span>
                </div>
            </div>

            <NavLink to="/perfil" className="lateral-perfil" onClick={onNavegar} title="Mi perfil">
                <span className="lateral-avatar">
                    {iniciales(usuario?.nombre)}
                    <i aria-hidden="true" />
                </span>
                <span className="lateral-correo">{usuario?.email || 'Administrador'}</span>
            </NavLink>

            <nav className="lateral-menu">
                {SECCIONES.map((s) => {
                    const Icono = ICONOS[s.icono];
                    const total = resumen && s.contador ? s.contador(resumen) : null;
                    return (
                        <NavLink
                            key={s.ruta}
                            to={s.ruta}
                            onClick={onNavegar}
                            className={({ isActive }) => `lateral-enlace${isActive ? ' activo' : ''}`}
                        >
                            <Icono />
                            <span>{s.etiqueta}</span>
                            {total !== null && <b className="lateral-contador">{total}</b>}
                        </NavLink>
                    );
                })}
            </nav>

            <div className="lateral-pie">
                <button type="button" className="lateral-salir" onClick={cerrarSesion}>
                    <IconoSalir /> Cerrar sesión
                </button>
            </div>
        </aside>
    );
}