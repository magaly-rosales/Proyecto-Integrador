import { useEffect, useRef, useState } from 'react';
import { IconoAlerta, IconoBuscar } from '../../components/ui/IconosUi';

const VISTAS = [
    { valor: 'pendientes', texto: 'Por entrenar' },
    { valor: 'entrenadas', texto: 'Entrenadas' },
];

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

// Botón desplegable para cambiar entre "Por entrenar" y "Entrenadas"
function SelectorVista({ vista, total, onCambiar }) {
    const [abierto, setAbierto] = useState(false);
    const caja = useRef(null);
    const actual = VISTAS.find((v) => v.valor === vista);

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
        <div className="captura-selector" ref={caja}>
            <button
                type="button"
                className="captura-selector-boton"
                aria-haspopup="listbox"
                aria-expanded={abierto}
                onClick={() => setAbierto((a) => !a)}
            >
                <strong>{actual.texto}</strong>
                <span className="captura-contador">{total}</span>
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4"
                    strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                    <path d="m6 9 6 6 6-6" />
                </svg>
            </button>
            {abierto && (
                <ul className="captura-selector-menu" role="listbox">
                    {VISTAS.map((v) => (
                        <li key={v.valor} role="none">
                            <button
                                type="button"
                                role="option"
                                aria-selected={v.valor === vista}
                                className={v.valor === vista ? 'activo' : ''}
                                onClick={() => {
                                    onCambiar(v.valor);
                                    setAbierto(false);
                                }}
                            >
                                {v.texto}
                            </button>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
}

// Lista de señas para elegir cuál entrenar. Las metas, lo que falta y el estado "Entrenada" los entrega el backend.
export default function ListaEntrenamiento({
    senas, categorias, cargando, error, idSena, onReintentar, onElegir,
}) {
    const [vista, setVista] = useState('pendientes'); // 'pendientes' | 'entrenadas'
    const [texto, setTexto] = useState('');
    const [categoria, setCategoria] = useState('');

    const buscado = texto.trim().toLowerCase();
    const visibles = senas.filter((s) => (
        (vista === 'entrenadas' ? s.entrenada : !s.entrenada)
        && (!categoria || String(s.categoria_id) === categoria)
        && (!buscado || s.nombre.toLowerCase().includes(buscado))
    ));
    const hayFiltro = Boolean(buscado || categoria);

    return (
        <section className="tabla-caja captura-lista-caja">
            <header className="captura-lista-cabecera">
                <SelectorVista vista={vista} total={cargando ? '…' : visibles.length} onCambiar={setVista} />
                <select
                    className="captura-categoria"
                    value={categoria}
                    onChange={(e) => setCategoria(e.target.value)}
                    aria-label="Categoría"
                >
                    <option value="">Categorías</option>
                    {categorias.map((c) => (
                        <option key={c.id} value={c.id}>{c.nombre}</option>
                    ))}
                </select>
                <label className="filtros-buscar captura-buscar">
                    <IconoBuscar />
                    <input
                        type="search"
                        value={texto}
                        onChange={(e) => setTexto(e.target.value)}
                        placeholder="Buscar seña"
                        aria-label="Buscar seña"
                    />
                </label>
            </header>

            {cargando && (
                <div className="tabla-carga" role="status" aria-label="Cargando señas">
                    {Array.from({ length: 5 }, (_, i) => <span key={i} />)}
                </div>
            )}

            {!cargando && error && (
                <Aviso
                    icono={<IconoAlerta tam={28} />}
                    titulo="No se pudo cargar la lista"
                    texto={error}
                    accion={<button type="button" className="boton-contorno" onClick={onReintentar}>Reintentar</button>}
                />
            )}

            {!cargando && !error && visibles.length === 0 && (
                <Aviso
                    titulo={hayFiltro ? 'Sin resultados' : vista === 'entrenadas' ? 'Aún no hay señas entrenadas' : '¡Todo entrenado!'}
                    texto={hayFiltro
                        ? 'Prueba con otra búsqueda o categoría.'
                        : vista === 'entrenadas'
                            ? 'Cuando una seña llegue a su meta, aparecerá aquí.'
                            : 'No hay señas pendientes de entrenar.'}
                />
            )}

            {!cargando && !error && visibles.length > 0 && (
                <ul className="captura-lista">
                    {visibles.map((s) => (
                        <li key={s.id}>
                            <button
                                type="button"
                                className={`captura-item ${String(s.id) === String(idSena) ? 'elegida' : ''}`}
                                style={{ '--acento': s.categoria_color || 'var(--borde)' }}
                                aria-pressed={String(s.id) === String(idSena)}
                                onClick={() => onElegir(s.id)}
                            >
                                <span className="captura-item-titulo">
                                    <i aria-hidden="true" />
                                    <strong>{s.nombre}</strong>
                                </span>
                            </button>
                        </li>
                    ))}
                </ul>
            )}
        </section>
    );
}
