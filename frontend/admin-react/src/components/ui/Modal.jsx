import { useEffect, useRef } from 'react';
import { IconoCerrar } from './IconosUi';
import '../../estilos/Modal.css';

// Ventana emergente: se cierra con Esc, con la X o haciendo clic fuera
export default function Modal({ titulo, onCerrar, children, ancho = 480 }) {
    const caja = useRef(null);
    const cerrar = useRef(onCerrar);

    useEffect(() => {
        cerrar.current = onCerrar;
    });

    useEffect(() => {
        const anterior = document.activeElement;
        const alTeclear = (e) => e.key === 'Escape' && cerrar.current();
        document.addEventListener('keydown', alTeclear);
        caja.current?.focus();
        return () => {
            document.removeEventListener('keydown', alTeclear);
            anterior?.focus?.();
        };
    }, []);

    return (
        <div className="modal-fondo" onMouseDown={(e) => e.target === e.currentTarget && onCerrar()}>
            <div
                className="modal"
                role="dialog"
                aria-modal="true"
                aria-label={titulo}
                tabIndex={-1}
                ref={caja}
                style={{ maxWidth: ancho }}
            >
                <header className="modal-cabecera">
                    <h2>{titulo}</h2>
                    <button type="button" className="modal-cerrar" aria-label="Cerrar" onClick={onCerrar}>
                        <IconoCerrar />
                    </button>
                </header>
                <div className="modal-cuerpo">{children}</div>
            </div>
        </div>
    );
}