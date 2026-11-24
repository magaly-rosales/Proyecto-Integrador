import { useEffect, useState } from 'react';
import { listarSenasDeCategoria } from '../../api/categoriasApi';
import { mensajeError } from '../../api/errores';
import { IconoAlerta, IconoDerecha, IconoImagen } from '../../components/ui/IconosUi';
import '../../estilos/Categorias.css';

// Señas de una categoría, mostradas en la misma página (datos reales del servidor)
export default function VerSenasCategoria({ categoria, onVolver }) {
    const [estado, setEstado] = useState({ cargando: true, error: '', senas: [] });
    const [version, setVersion] = useState(0);

    useEffect(() => {
        let vigente = true;
        listarSenasDeCategoria(categoria.id)
            .then(({ data }) => vigente && setEstado({ cargando: false, error: '', senas: data }))
            .catch((err) => vigente && setEstado({
                cargando: false,
                error: mensajeError(err, 'No se pudieron cargar las señas.'),
                senas: [],
            }));
        return () => {
            vigente = false;
        };
    }, [categoria.id, version]);

    const reintentar = () => {
        setEstado({ cargando: true, error: '', senas: [] });
        setVersion((v) => v + 1);
    };

    const { cargando, error, senas } = estado;

    return (
        <>
            <div className="barra-pagina">
                <button type="button" className="boton-contorno" onClick={onVolver}>
                    <span className="icono-atras"><IconoDerecha /></span> Volver a categorías
                </button>
                <p className="texto-suave">
                    <strong className="categoria-titulo">{categoria.nombre}</strong>
                    {!cargando && !error && ` · ${senas.length} seña${senas.length === 1 ? '' : 's'}`}
                </p>
            </div>

            {cargando && (
                <div className="categorias-grilla" role="status" aria-label="Cargando señas">
                    {Array.from({ length: 3 }, (_, i) => <span key={i} className="categoria-esqueleto" />)}
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

            {!cargando && !error && senas.length === 0 && (
                <section className="tabla-caja">
                    <div className="tabla-aviso">
                        <strong>Esta categoría todavía no tiene señas</strong>
                        <p>Agrégalas desde la sección Señas, eligiendo esta categoría.</p>
                    </div>
                </section>
            )}

            {!cargando && !error && senas.length > 0 && (
                <ul className="categorias-grilla">
                    {senas.map((s) => (
                        <li key={s.id} className="categoria-tarjeta" style={{ '--acento': categoria.color }}>
                            <div className="categoria-imagen">
                                {s.imagen ? (
                                    <img src={s.imagen} alt={`Imagen de la seña ${s.nombre}`} loading="lazy" />
                                ) : (
                                    <span className="categoria-sin-imagen"><IconoImagen tam={40} /></span>
                                )}
                            </div>
                            <div className="categoria-datos">
                                <strong>{s.nombre}</strong>
                                <span className="texto-suave categoria-descripcion">
                                    {s.descripcion || 'Sin descripción'}
                                </span>
                            </div>
                        </li>
                    ))}
                </ul>
            )}
        </>
    );
}
