import { useState } from 'react';
import { IconoAlerta, IconoImagen, IconoMas } from '../components/ui/IconosUi';
import useCategorias from '../hooks/useCategorias';
import { useResumen } from '../layout/ResumenContext';
import ConfirmarEliminarCategoria from './categorias/ConfirmarEliminarCategoria';
import FormularioCategoria from './categorias/FormularioCategoria';
import { MenuAcciones } from './senas/piezas';
import '../estilos/Comun.css';
import '../estilos/Senas.css';
import '../estilos/Categorias.css';

export default function Categorias() {
    const { recargar } = useResumen();
    const { categorias, cargando, error, reintentar, refrescar } = useCategorias();
    const [formulario, setFormulario] = useState(null); // { categoria: null } = nueva
    const [aEliminar, setAEliminar] = useState(null);

    // Tras crear, editar o eliminar: se cierra la ventana y se actualizan lista y contadores
    const tras = () => {
        setFormulario(null);
        setAEliminar(null);
        refrescar();
        recargar();
    };

    return (
        <>
            <div className="barra-pagina">
                <p className="texto-suave">
                    {cargando ? 'Cargando…' : `${categorias.length} categoría${categorias.length === 1 ? '' : 's'}`}
                </p>
                <button type="button" className="boton-principal" onClick={() => setFormulario({ categoria: null })}>
                    <IconoMas /> Nueva categoría
                </button>
            </div>

            {cargando && (
                <div className="categorias-grilla" role="status" aria-label="Cargando categorías">
                    {Array.from({ length: 6 }, (_, i) => <span key={i} className="categoria-esqueleto" />)}
                </div>
            )}

            {!cargando && error && (
                <section className="tabla-caja">
                    <div className="tabla-aviso">
                        <IconoAlerta tam={28} />
                        <strong>No se pudo cargar la lista</strong>
                        <p>{error}</p>
                        <button type="button" className="boton-contorno" onClick={reintentar}>Reintentar</button>
                    </div>
                </section>
            )}

            {!cargando && !error && categorias.length === 0 && (
                <section className="tabla-caja">
                    <div className="tabla-aviso">
                        <strong>Aún no hay categorías</strong>
                        <p>Crea la primera para poder clasificar tus señas.</p>
                    </div>
                </section>
            )}

            {!cargando && !error && categorias.length > 0 && (
                <ul className="categorias-grilla">
                    {categorias.map((c) => (
                        <li key={c.id} className="categoria-tarjeta" style={{ '--acento': c.color }}>
                            <div className="categoria-imagen">
                                {c.imagen ? (
                                    <img src={c.imagen} alt={`Imagen de la categoría ${c.nombre}`} loading="lazy" />
                                ) : (
                                    <span className="categoria-sin-imagen"><IconoImagen tam={40} /></span>
                                )}
                                <div className="categoria-menu">
                                    <MenuAcciones
                                        sena={c}
                                        onEditar={(cat) => setFormulario({ categoria: cat })}
                                        onEliminar={setAEliminar}
                                    />
                                </div>
                            </div>
                            <div className="categoria-datos">
                                <strong>{c.nombre}</strong>
                                <span className="texto-suave">
                                    {c.total_senas} seña{c.total_senas === 1 ? '' : 's'}
                                </span>
                            </div>
                        </li>
                    ))}
                </ul>
            )}

            {formulario && (
                <FormularioCategoria
                    categoria={formulario.categoria}
                    onCerrar={() => setFormulario(null)}
                    onGuardada={tras}
                />
            )}
            {aEliminar && (
                <ConfirmarEliminarCategoria
                    categoria={aEliminar}
                    onCerrar={() => setAEliminar(null)}
                    onEliminada={tras}
                />
            )}
        </>
    );
}