import { useEffect, useRef, useState } from 'react';
import { IconoChat, IconoObjetivo, IconoUsuarios } from '../../layout/Iconos';
import { useChatAsistente } from '../../layout/AsistenteContext';
import Mensaje from './Mensaje';
import Robot from './Robot';

const IconoEtiqueta = (p) => (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"
        strokeLinecap="round" strokeLinejoin="round" aria-hidden="true" {...p}>
        <path d="M20.6 13.4 13.4 20.6a2 2 0 0 1-2.8 0L3 13V3h10l7.6 7.6a2 2 0 0 1 0 2.8z" />
        <circle cx="7.5" cy="7.5" r="1.2" />
    </svg>
);

const IconoEnviar = () => (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"
        strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
        <path d="M22 2 11 13M22 2l-7 20-4-9-9-4 20-7z" />
    </svg>
);

// Preguntas de ejemplo para empezar; las respuestas siempre salen de los datos reales
const EJEMPLOS = [
    { texto: '¿Cuántos usuarios activos e inactivos hay?', Icono: IconoUsuarios },
    { texto: '¿Cuántas categorías tengo registradas?', Icono: IconoEtiqueta },
    { texto: '¿Qué seña se traduce más?', Icono: IconoChat },
    { texto: '¿Qué señas faltan por entrenar?', Icono: IconoObjetivo },
];

// Mensajes y barra para escribir; se usa en la sección Asistente y en la ventana flotante
export default function Conversacion({ compacto = false }) {
    const { mensajes, enviando, enviar, confirmar, cancelar } = useChatAsistente();
    const [texto, setTexto] = useState('');
    const final = useRef(null);

    // Baja al último mensaje
    useEffect(() => {
        final.current?.scrollIntoView({ behavior: 'smooth', block: 'end' });
    }, [mensajes, enviando]);

    const enviarTexto = (evento) => {
        evento.preventDefault();
        enviar(texto);
        setTexto('');
    };

    return (
        <div className={`asistente-chat${compacto ? ' asistente-compacto' : ''}`}>
            <div className="asistente-conversacion" aria-live="polite">
                {mensajes.length === 0 && (
                    <div className="asistente-inicio">
                        <Robot tam={compacto ? 76 : 132} />
                        <h2>¿En qué te ayudo?</h2>
                        <p className="texto-suave">
                            Pregúntame por usuarios, categorías, señas, entrenamiento o estadísticas.
                        </p>
                        <div className="asistente-ejemplos">
                            {EJEMPLOS.map(({ texto: ejemplo, Icono }) => (
                                <button key={ejemplo} type="button" className="asistente-ejemplo" onClick={() => enviar(ejemplo)}>
                                    <Icono />
                                    <span>{ejemplo}</span>
                                </button>
                            ))}
                        </div>
                    </div>
                )}

                {mensajes.map((m) => (
                    <Mensaje key={m.id} mensaje={m} onConfirmar={confirmar} onCancelar={cancelar} />
                ))}

                {enviando && (
                    <div className="asistente-mensaje asistente-asistente">
                        <p className="texto-suave">Pensando…</p>
                    </div>
                )}
                <div ref={final} />
            </div>

            <form className="asistente-formulario" onSubmit={enviarTexto}>
                <input
                    type="text"
                    value={texto}
                    onChange={(e) => setTexto(e.target.value)}
                    placeholder="Escribe tu pregunta"
                    aria-label="Pregunta para el asistente"
                    maxLength={500}
                    autoFocus
                />
                <button
                    type="submit"
                    className="asistente-enviar"
                    disabled={enviando || !texto.trim()}
                    aria-label="Enviar pregunta"
                >
                    <IconoEnviar />
                </button>
            </form>
        </div>
    );
}
