import { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';
import Conversacion from '../paginas/asistente/Conversacion';
import Robot from '../paginas/asistente/Robot';
import '../estilos/Asistente.css';

// Secciones donde no aparece: Entrenamiento (usa la cámara) y el propio Asistente
const SIN_FLOTANTE = ['/entrenamiento', '/asistente'];

// Botón flotante abajo a la derecha que abre una ventana pequeña con el asistente
export default function AsistenteFlotante() {
    const { pathname } = useLocation();
    const [abierto, setAbierto] = useState(false);
    const oculto = SIN_FLOTANTE.some((ruta) => pathname.startsWith(ruta));

    // Esc cierra la ventana
    useEffect(() => {
        if (!abierto) return undefined;
        const cerrar = (e) => e.key === 'Escape' && setAbierto(false);
        document.addEventListener('keydown', cerrar);
        return () => document.removeEventListener('keydown', cerrar);
    }, [abierto]);

    if (oculto) return null;

    return (
        <div className="asistente-flotante">
            {abierto && (
                <section className="asistente-ventana" role="dialog" aria-label="Asistente con IA">
                    <header className="asistente-ventana-cabecera">
                        <Robot tam={34} />
                        <strong>Asistente</strong>
                        <button type="button" onClick={() => setAbierto(false)} aria-label="Cerrar el asistente">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                                strokeWidth="2" strokeLinecap="round" aria-hidden="true">
                                <path d="M6 6l12 12M18 6 6 18" />
                            </svg>
                        </button>
                    </header>
                    <Conversacion compacto />
                </section>
            )}

            <button
                type="button"
                className="asistente-burbuja"
                onClick={() => setAbierto((a) => !a)}
                aria-expanded={abierto}
                aria-label={abierto ? 'Cerrar el asistente' : 'Abrir el asistente con IA'}
            >
                <Robot tam={68} />
            </button>
        </div>
    );
}
