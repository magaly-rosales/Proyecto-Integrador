import { useEffect, useRef, useState } from 'react';
import { IconoEditar, IconoImagen, IconoPapelera, IconoPlay, IconoPuntos } from '../../components/ui/IconosUi';

export function Miniatura({ sena, onVer }) {
    if (!sena.imagen) {
        return <span className="sena-miniatura"><IconoImagen /></span>;
    }
    return (
        <button
            type="button"
            className="sena-miniatura sena-miniatura-boton"
            onClick={() => onVer(sena)}
            aria-label={`Ver imagen de ${sena.nombre}`}
        >
            <img src={sena.imagen} alt="" loading="lazy" />
        </button>
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
export function MenuAcciones({ sena, onEditar, onEliminar }) {
    const [pos, setPos] = useState(null); // null = cerrado
    const caja = useRef(null);
    const boton = useRef(null);

    const cerrar = () => setPos(null);

    const alternar = () => {
        if (pos) return cerrar();
        const r = boton.current.getBoundingClientRect();
        const alto = 110; // alto aproximado del menú (2 opciones)
        const abajoCabe = window.innerHeight - r.bottom > alto + 8;
        setPos({
            right: window.innerWidth - r.right,
            ...(abajoCabe ? { top: r.bottom + 4 } : { bottom: window.innerHeight - r.top + 4 }),
        });
        return undefined;
    };

    useEffect(() => {
        if (!pos) return undefined;
        const fuera = (e) => !caja.current?.contains(e.target) && cerrar();
        const esc = (e) => e.key === 'Escape' && cerrar();
        document.addEventListener('mousedown', fuera);
        document.addEventListener('keydown', esc);
        window.addEventListener('scroll', cerrar, true);
        window.addEventListener('resize', cerrar);
        return () => {
            document.removeEventListener('mousedown', fuera);
            document.removeEventListener('keydown', esc);
            window.removeEventListener('scroll', cerrar, true);
            window.removeEventListener('resize', cerrar);
        };
    }, [pos]);

    return (
        <div className="menu-acciones" ref={caja}>
            <button
                ref={boton}
                type="button"
                className="boton-icono"
                aria-haspopup="menu"
                aria-expanded={Boolean(pos)}
                aria-label={`Acciones de ${sena.nombre}`}
                onClick={alternar}
            >
                <IconoPuntos />
            </button>
            {pos && (
                <ul className="menu-lista" role="menu" style={pos}>
                    <li role="none">
                        <button
                            type="button"
                            role="menuitem"
                            onClick={() => {
                                cerrar();
                                onEditar(sena);
                            }}
                        >
                            <IconoEditar /> Editar
                        </button>
                    </li>
                    <li role="none">
                        <button
                            type="button"
                            role="menuitem"
                            className="menu-peligro"
                            onClick={() => {
                                cerrar();
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