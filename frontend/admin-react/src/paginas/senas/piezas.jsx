import { useEffect, useRef, useState } from 'react';
import { IconoImagen, IconoPapelera, IconoPlay, IconoPuntos } from '../../components/ui/IconosUi';

export function Miniatura({ sena }) {
    return (
        <span className="sena-miniatura">
            {sena.imagen ? <img src={sena.imagen} alt="" loading="lazy" /> : <IconoImagen />}
        </span>
    );
}

export function PildoraCategoria({ sena }) {
    if (!sena.categoria) return <span className="texto-suave">Sin categoría</span>;
    return (
        <span className="pildora pildora-categoria">
            <i style={{ background: sena.categoria_color || 'var(--texto-suave)' }} aria-hidden="true" />
            {sena.categoria}
        </span>
    );
}

export function PildoraEntrenamiento({ sena }) {
    const detalle = `${sena.muestras.actual}/${sena.muestras.meta} muestras · ${sena.movimientos.actual}/${sena.movimientos.meta} movimientos`;
    return (
        <span
            className={`pildora ${sena.entrenada ? 'pildora-exito' : 'pildora-aviso'}`}
            title={detalle}
        >
            <i aria-hidden="true" />
            {sena.entrenada ? 'Entrenada' : 'Sin entrenar'}
        </span>
    );
}

export function BotonVideo({ sena, onVer }) {
    if (!sena.video) return <span className="texto-suave">Sin video</span>;
    return (
        <button type="button" className="boton-ver" onClick={() => onVer(sena)}>
            <IconoPlay tam={12} /> Ver
        </button>
    );
}

// Menú de tres puntos con las acciones de cada fila
export function MenuAcciones({ sena, onEliminar }) {
    const [abierto, setAbierto] = useState(false);
    const caja = useRef(null);

    useEffect(() => {
        if (!abierto) return undefined;
        const fuera = (e) => !caja.current?.contains(e.target) && setAbierto(false);
        const esc = (e) => e.key === 'Escape' && setAbierto(false);
        document.addEventListener('mousedown', fuera);
        document.addEventListener('keydown', esc);
        return () => {
            document.removeEventListener('mousedown', fuera);
            document.removeEventListener('keydown', esc);
        };
    }, [abierto]);

    return (
        <div className="menu-acciones" ref={caja}>
            <button
                type="button"
                className="boton-icono"
                aria-haspopup="menu"
                aria-expanded={abierto}
                aria-label={`Acciones de ${sena.nombre}`}
                onClick={() => setAbierto((a) => !a)}
            >
                <IconoPuntos />
            </button>
            {abierto && (
                <ul className="menu-lista" role="menu">
                    <li role="none">
                        <button
                            type="button"
                            role="menuitem"
                            className="menu-peligro"
                            onClick={() => {
                                setAbierto(false);
                                onEliminar(sena);
                            }}
                        >
                            <IconoPapelera /> Eliminar
                        </button>
                    </li>
                </ul>
            )}
        </div>
    );
}