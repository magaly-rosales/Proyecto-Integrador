import { IconoAlerta } from '../../components/ui/IconosUi';
import { BotonVideo, Miniatura, MenuAcciones, PildoraCategoria, PildoraEntrenamiento } from './piezas';

function Esqueleto() {
    return (
        <div className="tabla-carga" role="status" aria-label="Cargando señas">
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

// Tabla en pantallas grandes y tarjetas en el móvil (mismos datos, dos presentaciones)
export default function TablaSenas({ senas, cargando, error, hayFiltro, onReintentar, onVer, onEliminar }) {
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

    if (senas.length === 0) {
        return (
            <section className="tabla-caja">
                <Aviso
                    titulo={hayFiltro ? 'Sin resultados' : 'Aún no hay señas'}
                    texto={hayFiltro ? 'Prueba con otra búsqueda o categoría.' : 'Cuando registres señas, aparecerán aquí.'}
                />
            </section>
        );
    }

    return (
        <section className="tabla-caja">
            <table className="tabla">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Nombre</th>
                        <th>Categoría</th>
                        <th>Descripción</th>
                        <th>Video</th>
                        <th>Entrenamiento</th>
                        <th><span className="solo-lector">Acciones</span></th>
                    </tr>
                </thead>
                <tbody>
                    {senas.map((s) => (
                        <tr key={s.id}>
                            <td className="texto-suave">{s.id}</td>
                            <td>
                                <span className="sena-nombre">
                                    <Miniatura sena={s} />
                                    <strong>{s.nombre}</strong>
                                    {s.es_nueva && <i className="punto-nueva" title="Nueva esta semana" />}
                                </span>
                            </td>
                            <td><PildoraCategoria sena={s} /></td>
                            <td className="celda-descripcion" title={s.descripcion}>{s.descripcion}</td>
                            <td><BotonVideo sena={s} onVer={onVer} /></td>
                            <td><PildoraEntrenamiento sena={s} /></td>
                            <td className="celda-acciones"><MenuAcciones sena={s} onEliminar={onEliminar} /></td>
                        </tr>
                    ))}
                </tbody>
            </table>

            <ul className="sena-tarjetas">
                {senas.map((s) => (
                    <li key={s.id} className="sena-tarjeta">
                        <div className="sena-tarjeta-cabeza">
                            <Miniatura sena={s} />
                            <div>
                                <strong>{s.nombre}</strong>
                                <PildoraCategoria sena={s} />
                            </div>
                            <MenuAcciones sena={s} onEliminar={onEliminar} />
                        </div>
                        <p>{s.descripcion}</p>
                        <div className="sena-tarjeta-pie">
                            <PildoraEntrenamiento sena={s} />
                            <BotonVideo sena={s} onVer={onVer} />
                        </div>
                    </li>
                ))}
            </ul>
        </section>
    );
}