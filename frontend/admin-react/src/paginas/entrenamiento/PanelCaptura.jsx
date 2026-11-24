import { useCallback, useRef, useState } from 'react';
import CamaraMano from '../../components/CamaraMano';
import useCaptura, { DURACION_MOV_S, MUESTRAS_RAFAGA } from '../../hooks/useCaptura';

const Icono = ({ children }) => (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"
        strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
        {children}
    </svg>
);

const IconoCamara = () => (
    <Icono>
        <path d="M4 8h3l1.6-2.4h6.8L17 8h3a1 1 0 0 1 1 1v9a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V9a1 1 0 0 1 1-1z" />
        <circle cx="12" cy="13" r="3.5" />
    </Icono>
);

const IconoRayo = () => (
    <Icono><path d="M13 2 4 14h7l-1 8 9-12h-7z" /></Icono>
);

// Cámara con la mano detectada, seña elegida y botones para grabar muestras o movimientos
export default function PanelCaptura({ sena, onGuardado, onBorrar }) {
    const manosRef = useRef([]);
    const [cantidadManos, setCantidadManos] = useState(0);
    const [puntos, setPuntos] = useState(0);

    const alDetectar = useCallback((manos) => {
        manosRef.current = manos;
        setCantidadManos(manos.length);
        setPuntos(manos.reduce((total, m) => total + m.puntos.length, 0)); // solo cuenta los puntos detectados
    }, []);

    const { mensaje, ocupado, capturarUna, capturarRafaga, grabarMovimiento } = useCaptura({
        idSena: sena ? sena.id : '',
        manosRef,
        onGuardado,
    });

    const sinSena = !sena;

    return (
        <section className="captura-panel">
            {/* La cámara va arriba, alineada con el borde superior del recuadro de la lista */}
            <div className="captura-escenario">
                <CamaraMano onLandmarks={alDetectar} />
                <span className="captura-guia" aria-hidden="true" />
                <span className="captura-chip captura-chip-arriba">
                    <i className={cantidadManos > 0 ? 'con-mano' : ''} aria-hidden="true" />
                    Manos en pantalla: {cantidadManos}
                </span>
                {cantidadManos > 0 && (
                    <span className="captura-chip captura-chip-abajo">
                        {puntos} puntos detectados{sena ? ` · ${sena.nombre}` : ''}
                    </span>
                )}
            </div>

            <header className="captura-cabecera">
                {sena ? (
                    <>
                        <h2 className="captura-nombre">
                            <i style={{ background: sena.categoria_color || 'var(--texto-suave)' }} aria-hidden="true" />
                            {sena.nombre}
                        </h2>
                        {sena.categoria && <span className="texto-suave">{sena.categoria}</span>}
                        <span className="captura-cabecera-derecha">
                            <span className={`pildora ${sena.entrenada ? 'pildora-exito' : 'pildora-aviso'}`}>
                                <i aria-hidden="true" />
                                {sena.entrenada ? 'Entrenada' : 'Por entrenar'}
                            </span>
                            <button
                                type="button"
                                className="boton-estado boton-estado-rojo"
                                disabled={ocupado || (sena.muestras.actual === 0 && sena.movimientos.actual === 0)}
                                onClick={() => onBorrar(sena)}
                            >
                                Borrar ejemplos
                            </button>
                        </span>
                    </>
                ) : (
                    <h2 className="captura-nombre">Elige una seña de la lista</h2>
                )}
            </header>

            <div className="captura-acciones">
                <button type="button" className="boton-principal" onClick={capturarUna} disabled={ocupado || sinSena}>
                    <IconoCamara /> Capturar muestra
                </button>
                <button type="button" className="boton-contorno" onClick={capturarRafaga} disabled={ocupado || sinSena}>
                    <IconoRayo /> Ráfaga de {MUESTRAS_RAFAGA}
                </button>
                <button type="button" className="boton-contorno" onClick={grabarMovimiento} disabled={ocupado || sinSena}>
                    <i className="captura-punto-rojo" aria-hidden="true" /> Grabar movimiento ({DURACION_MOV_S} s)
                </button>
            </div>

            {mensaje && <p className={`captura-mensaje captura-mensaje-${mensaje.tipo}`} role="status">{mensaje.texto}</p>}
        </section>
    );
}
