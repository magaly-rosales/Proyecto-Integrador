import { useCallback, useEffect, useRef, useState } from 'react';
import CamaraMano from './CamaraMano';
import {
    getResumenEntrenamiento,
    borrarEntrenamiento,
    guardarMuestra,
    guardarSecuencia,
    createSena,
} from '../services/senasService';
import '../estilos/Entrenamiento.css';

const CUENTA_REGRESIVA_S = 3;
const MUESTRAS_RAFAGA = 20;
const INTERVALO_RAFAGA_MS = 300;
const DURACION_MOV_MS = 2000;
const INTERVALO_MOV_MS = 100;
const MIN_CUADROS = 8;
const META_MUESTRAS = 20;
const META_MOVIMIENTOS = 10;

const esperar = (ms) => new Promise((resolver) => setTimeout(resolver, ms));
const textoError = (e) => e.response?.data?.error || e.message;

export const Entrenamiento = ({ onSenaCreada }) => {
    const manosRef = useRef([]);
    const montadoRef = useRef(true);
    const [resumen, setResumen] = useState([]);
    const [idSena, setIdSena] = useState('');
    const [cantidadManos, setCantidadManos] = useState(0);
    const [mensaje, setMensaje] = useState(null); // { tipo: 'ok' | 'aviso' | 'error', texto }
    const [ocupado, setOcupado] = useState(false);
    const [mostrarNueva, setMostrarNueva] = useState(false);
    const [nuevoNombre, setNuevoNombre] = useState('');
    const [nuevaCategoria, setNuevaCategoria] = useState('');
    const [nuevoVideo, setNuevoVideo] = useState(null);
    const alDetectar = useCallback((manos) => {
        manosRef.current = manos;
        setCantidadManos(manos.length);
    }, []);

    const cargarResumen = useCallback(async () => {
        try {
            const { data } = await getResumenEntrenamiento();
            if (montadoRef.current) setResumen(data);
        } catch (error) {
            console.error('Error al cargar el resumen:', error);
        }
    }, []);

    useEffect(() => {
        montadoRef.current = true;
        cargarResumen();
        return () => {
            montadoRef.current = false;
        };
    }, [cargarResumen]);

    async function cuentaRegresiva() {
        for (let s = CUENTA_REGRESIVA_S; s > 0; s--) {
            setMensaje({ tipo: 'aviso', texto: `Prepara la seña... ${s}` });
            await esperar(1000);
            if (!montadoRef.current) return false;
        }
        return true;
    }

    async function capturarUna() {
        const manos = manosRef.current;
        if (manos.length === 0) {
            setMensaje({ tipo: 'aviso', texto: 'No hay ninguna mano a la vista.' });
            return;
        }
        setOcupado(true);
        try {
            await guardarMuestra(idSena, manos);
            setMensaje({ tipo: 'ok', texto: 'Muestra guardada.' });
        } catch (error) {
            setMensaje({ tipo: 'error', texto: textoError(error) });
        } finally {
            setOcupado(false);
            cargarResumen();
        }
    }

    // Con las dos manos en la seña no se puede hacer clic: cuenta regresiva y varias muestras seguidas.
    async function capturarRafaga() {
        setOcupado(true);
        try {
            if (!(await cuentaRegresiva())) return;

            // El número de manos se fija al empezar: no se mezclan muestras de 1 y 2 manos
            const manosInicio = manosRef.current.length;
            if (manosInicio === 0) {
                setMensaje({ tipo: 'aviso', texto: 'No se detectó ninguna mano. Intenta de nuevo.' });
                return;
            }

            let guardadas = 0;
            let intentos = 0;
            while (guardadas < MUESTRAS_RAFAGA && intentos < MUESTRAS_RAFAGA * 5) {
                intentos++;
                const manos = manosRef.current;
                if (manos.length === manosInicio) {
                    await guardarMuestra(idSena, manos);
                    guardadas++;
                    setMensaje({ tipo: 'aviso', texto: `Grabando... ${guardadas}/${MUESTRAS_RAFAGA}` });
                }
                await esperar(INTERVALO_RAFAGA_MS);
                if (!montadoRef.current) return;
            }

            setMensaje(
                guardadas === MUESTRAS_RAFAGA
                    ? { tipo: 'ok', texto: `Ráfaga completa: ${guardadas} muestras guardadas.` }
                    : { tipo: 'aviso', texto: `Se guardaron ${guardadas} de ${MUESTRAS_RAFAGA}: la mano salió de la imagen.` }
            );
        } catch (error) {
            setMensaje({ tipo: 'error', texto: textoError(error) });
        } finally {
            if (montadoRef.current) setOcupado(false);
            cargarResumen();
        }
    }

    // Graba ~2 s de la seña en movimiento (≈20 cuadros) y la guarda como una secuencia
    async function grabarMovimiento() {
        setOcupado(true);
        try {
            if (!(await cuentaRegresiva())) return;

            const cuadros = [];
            let numManos = 0;
            const fin = Date.now() + DURACION_MOV_MS;
            setMensaje({ tipo: 'aviso', texto: '¡Haz la seña ahora!' });

            while (Date.now() < fin) {
                const manos = manosRef.current;
                if (manos.length > 0) {
                    if (numManos === 0) numManos = manos.length;
                    if (manos.length === numManos) {
                        cuadros.push(
                            manos.map((m) => ({
                                etiqueta: m.etiqueta,
                                puntos: m.puntos.map(({ x, y, z }) => ({ x, y, z })),
                            }))
                        );
                    }
                }
                await esperar(INTERVALO_MOV_MS);
                if (!montadoRef.current) return;
            }

            if (cuadros.length < MIN_CUADROS) {
                setMensaje({
                    tipo: 'aviso',
                    texto: `Solo se captaron ${cuadros.length} cuadros: mantén la mano a la vista y repite.`,
                });
                return;
            }

            await guardarSecuencia(idSena, cuadros);
            setMensaje({ tipo: 'ok', texto: `Movimiento guardado (${cuadros.length} cuadros).` });
        } catch (error) {
            setMensaje({ tipo: 'error', texto: textoError(error) });
        } finally {
            if (montadoRef.current) setOcupado(false);
            cargarResumen();
        }
    }

    async function crearSena(e) {
        e.preventDefault();
        if (!nuevoNombre.trim() || !nuevaCategoria.trim()) return;
        const formData = new FormData();
        formData.append('nombre', nuevoNombre.trim());
        formData.append('categoria', nuevaCategoria.trim());
        if (nuevoVideo) formData.append('video', nuevoVideo);
        try {
            const { data } = await createSena(formData);
            setNuevoNombre('');
            setNuevaCategoria('');
            setNuevoVideo(null);
            setMostrarNueva(false);
            await cargarResumen();
            onSenaCreada?.();
            if (data?.id) setIdSena(String(data.id));
            setMensaje({ tipo: 'ok', texto: `Seña "${data?.nombre ?? ''}" creada. Ya puedes grabarle muestras.` });
        } catch (error) {
            setMensaje({ tipo: 'error', texto: textoError(error) });
        }
    }

    async function handleBorrar(id, nombre, tipo) {
        const que = tipo === 'muestras' ? 'las muestras' : 'los movimientos';
        if (!window.confirm(`¿Borrar ${que} de "${nombre}"?`)) return;
        try {
            await borrarEntrenamiento(id, tipo);
            setMensaje({ tipo: 'ok', texto: `Se borraron ${que} de "${nombre}".` });
            cargarResumen();
        } catch (error) {
            setMensaje({ tipo: 'error', texto: textoError(error) });
        }
    }

    return (
        <div className="entrenamiento entrenamiento-grid">
            {/* Cámara + controles */}
            <section className="admin-card">
                <h3 className="card-title">🎥 Grabar ejemplos</h3>

                <div style={{ marginTop: '1rem' }}>
                    {!mostrarNueva ? (
                        <button className="btn-refresh" onClick={() => setMostrarNueva(true)} disabled={ocupado}>
                            ➕ Nueva seña
                        </button>
                    ) : (
                        <form onSubmit={crearSena} className="admin-form">
                            <input
                                className="admin-input"
                                placeholder="Nombre (ej: Buenos días)"
                                value={nuevoNombre}
                                onChange={(e) => setNuevoNombre(e.target.value)}
                                required
                            />
                            <input
                                className="admin-input"
                                placeholder="Categoría (ej: Saludos)"
                                value={nuevaCategoria}
                                onChange={(e) => setNuevaCategoria(e.target.value)}
                                required
                            />
                            <div className="form-group">
                                <label>Video MP4 de referencia (opcional, máx 50MB)</label>
                                <input
                                    type="file"
                                    accept="video/mp4,video/*"
                                    onChange={(e) => setNuevoVideo(e.target.files[0] || null)}
                                    className="admin-input"
                                />
                            </div>
                            <div style={{ display: 'flex', gap: '0.5rem' }}>
                                <button type="submit" className="btn-primary">Crear</button>
                                <button type="button" className="btn-refresh" onClick={() => setMostrarNueva(false)}>
                                    Cancelar
                                </button>
                            </div>
                        </form>
                    )}
                </div>

                <div className="form-group" style={{ marginTop: '1rem' }}>
                    <label>Seña a grabar</label>
                    <select
                        value={idSena}
                        onChange={(e) => setIdSena(e.target.value)}
                        disabled={ocupado}
                        className="admin-input"
                    >
                        <option value="">Elige una seña…</option>
                        {resumen.map((s) => (
                            <option key={s.id} value={s.id}>{s.nombre}</option>
                        ))}
                    </select>
                </div>

                <CamaraMano onLandmarks={alDetectar} />

                <p className="aviso">
                    Manos en pantalla: <strong>{cantidadManos}</strong>
                    {cantidadManos > 0 && ` (${cantidadManos * 21} puntos)`}
                </p>

                <div className="ent-acciones">
                    <button className="btn-primary" onClick={capturarUna} disabled={ocupado || !idSena}>
                        Capturar muestra
                    </button>
                    <button className="btn-primary" onClick={capturarRafaga} disabled={ocupado || !idSena}>
                        Ráfaga de {MUESTRAS_RAFAGA}
                    </button>
                    <button className="btn-primary" onClick={grabarMovimiento} disabled={ocupado || !idSena}>
                        Grabar movimiento ({DURACION_MOV_MS / 1000} s)
                    </button>
                </div>

                {mensaje && <p className={`ent-mensaje ent-mensaje-${mensaje.tipo}`}>{mensaje.texto}</p>}

                <p className="ent-guia">
                    Muestra y ráfaga sirven para señas <strong>quietas</strong>. «Grabar movimiento» sirve para
                    señas que <strong>se mueven</strong>. Graba cada seña siempre con el mismo número de manos
                    y varía un poco la posición y la velocidad.
                </p>
            </section>

            {/* Resumen por seña */}
            <section className="admin-card">
                <div className="card-header">
                    <h3 className="card-title">📚 Ejemplos por seña</h3>
                    <button onClick={cargarResumen} className="btn-refresh">🔄 Actualizar</button>
                </div>
                <p className="ent-guia">
                    Meta: al menos {META_MUESTRAS} muestras y {META_MOVIMIENTOS} movimientos por seña.
                </p>
                <div className="table-wrapper">
                    <table className="admin-table">
                        <thead>
                            <tr>
                                <th>Seña</th>
                                <th>Muestras</th>
                                <th>Movimientos</th>
                                <th>Borrar</th>
                            </tr>
                        </thead>
                        <tbody>
                            {resumen.length === 0 ? (
                                <tr><td colSpan="4" className="td-muted">Sin señas registradas.</td></tr>
                            ) : (
                                resumen.map((s) => (
                                    <tr key={s.id}>
                                        <td className="td-bold">{s.nombre}</td>
                                        <td className={s.muestras >= META_MUESTRAS ? 'ent-ok' : 'td-muted'}>
                                            {s.muestras}
                                        </td>
                                        <td className={s.secuencias >= META_MOVIMIENTOS ? 'ent-ok' : 'td-muted'}>
                                            {s.secuencias}
                                        </td>
                                        <td style={{ display: 'flex', gap: '0.4rem' }}>
                                            <button
                                                className="btn-danger btn-chico"
                                                disabled={s.muestras === 0}
                                                onClick={() => handleBorrar(s.id, s.nombre, 'muestras')}
                                            >
                                                Muestras
                                            </button>
                                            <button
                                                className="btn-danger btn-chico"
                                                disabled={s.secuencias === 0}
                                                onClick={() => handleBorrar(s.id, s.nombre, 'secuencias')}
                                            >
                                                Mov.
                                            </button>
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>
            </section>
        </div>
    );
};