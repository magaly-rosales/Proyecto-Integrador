import { IconoAlerta } from '../../components/ui/IconosUi';

function Esqueleto() {
    return (
        <div className="tabla-carga" role="status" aria-label="Cargando usuarios">
            {Array.from({ length: 6 }, (_, i) => <span key={i} />)}
        </div>
    );
}

function Aviso({ icono, titulo, texto, accion }) {
    return (
        <div className="tabla-aviso">
            {icono}
            <strong>{titulo}</strong>
            <p>{texto}</p>
            {accion}
        </div>
    );
}

// Foto de perfil del usuario; si aún no subió una, se muestran sus iniciales
function Avatar({ usuario }) {
    return (
        <span className="usuario-avatar" aria-hidden="true">
            {usuario.foto ? <img src={usuario.foto} alt="" loading="lazy" /> : usuario.iniciales}
        </span>
    );
}

function Persona({ usuario }) {
    return (
        <span className="usuario-persona">
            <Avatar usuario={usuario} />
            <strong>{usuario.nombre}</strong>
        </span>
    );
}

// Verde = activo, rojo = inactivo (el botón sigue el color del estado de la cuenta)
function PildoraEstado({ usuario }) {
    return (
        <span className={`pildora ${usuario.is_active ? 'pildora-exito' : 'pildora-error'}`}>
            <i aria-hidden="true" />
            {usuario.is_active ? 'Activo' : 'Inactivo'}
        </span>
    );
}

function BotonEstado({ usuario, onCambiar }) {
    return (
        <button
            type="button"
            className={`boton-estado ${usuario.is_active ? 'boton-estado-verde' : 'boton-estado-rojo'}`}
            onClick={() => onCambiar(usuario)}
        >
            {usuario.is_active ? 'Desactivar' : 'Activar'}
        </button>
    );
}

// Tabla en pantallas grandes y tarjetas en el móvil (mismos datos, dos presentaciones)
export default function TablaUsuarios({ usuarios, total, cargando, error, hayFiltro, onReintentar, onCambiar }) {
    if (cargando) return <section className="tabla-caja"><Esqueleto /></section>;

    if (error) {
        return (
            <section className="tabla-caja">
                <Aviso
                    icono={<IconoAlerta tam={28} />}
                    titulo="No se pudo cargar la lista"
                    texto={error}
                    accion={<button type="button" className="boton-contorno" onClick={onReintentar}>Reintentar</button>}
                />
            </section>
        );
    }

    if (usuarios.length === 0) {
        return (
            <section className="tabla-caja">
                <Aviso
                    titulo={hayFiltro ? 'Sin resultados' : 'Aún no hay usuarios'}
                    texto={hayFiltro ? 'Prueba con otra búsqueda o estado.' : 'Cuando alguien se registre en la web o la app, aparecerá aquí.'}
                />
            </section>
        );
    }

    return (
        <section className="tabla-caja">
            <table className="tabla tabla-usuarios">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Nombre</th>
                        <th>Correo</th>
                        <th>Estado</th>
                        <th>Acción</th>
                    </tr>
                </thead>
                <tbody>
                    {usuarios.map((u) => (
                        <tr key={u.id}>
                            <td className="texto-suave">{u.id}</td>
                            <td><Persona usuario={u} /></td>
                            <td className="texto-suave">{u.email}</td>
                            <td><PildoraEstado usuario={u} /></td>
                            <td className="celda-acciones"><BotonEstado usuario={u} onCambiar={onCambiar} /></td>
                        </tr>
                    ))}
                </tbody>
            </table>

            <ul className="sena-tarjetas">
                {usuarios.map((u) => (
                    <li key={u.id} className="sena-tarjeta">
                        <Persona usuario={u} />
                        <p className="usuario-correo">{u.email}</p>
                        <div className="sena-tarjeta-pie">
                            <PildoraEstado usuario={u} />
                            <BotonEstado usuario={u} onCambiar={onCambiar} />
                        </div>
                    </li>
                ))}
            </ul>

            <footer className="tabla-pie">
                Mostrando {usuarios.length} de {total ?? usuarios.length} usuarios · un usuario desactivado no puede
                iniciar sesión en la web ni en la app
            </footer>
        </section>
    );
}
