import { useRef, useState } from 'react';
import { confirmarAccion, preguntarAsistente } from '../api/asistenteApi';
import { mensajeError } from '../api/errores';

// Conversación con el asistente. Los datos y las respuestas las pone el backend; aquí solo se envían y se muestran.
// Cada mensaje: { id, rol: 'usuario' | 'asistente', texto, error?, accion?, estadoAccion? }
// estadoAccion: 'pendiente' | 'enviando' | 'confirmada' | 'cancelada'
export default function useAsistente({ onCuentaCambiada } = {}) {
    const [mensajes, setMensajes] = useState([]);
    const [enviando, setEnviando] = useState(false);
    const contador = useRef(0);

    const nuevo = (datos) => ({ id: (contador.current += 1), ...datos });
    const cambiar = (id, cambios) =>
        setMensajes((lista) => lista.map((m) => (m.id === id ? { ...m, ...cambios } : m)));

    const enviar = async (pregunta) => {
        const texto = pregunta.trim();
        if (!texto || enviando) return;

        const mensaje = nuevo({ rol: 'usuario', texto });
        const historial = [...mensajes, mensaje]
            .filter((m) => !m.error)
            .map(({ rol, texto: t }) => ({ rol, texto: t }));

        setMensajes((lista) => [...lista, mensaje]);
        setEnviando(true);
        try {
            const { data } = await preguntarAsistente(historial);
            setMensajes((lista) => [...lista, nuevo({
                rol: 'asistente',
                texto: data.respuesta,
                accion: data.accion,
                estadoAccion: data.accion ? 'pendiente' : null,
            })]);
        } catch (err) {
            setMensajes((lista) => [...lista, nuevo({
                rol: 'asistente',
                texto: mensajeError(err, 'No se pudo consultar al asistente.'),
                error: true,
            })]);
        } finally {
            setEnviando(false);
        }
    };

    const confirmar = async (mensaje) => {
        cambiar(mensaje.id, { estadoAccion: 'enviando' });
        try {
            const { data } = await confirmarAccion(mensaje.accion.id_usuario, mensaje.accion.activar);
            cambiar(mensaje.id, { estadoAccion: 'confirmada' });
            setMensajes((lista) => [...lista, nuevo({ rol: 'asistente', texto: data.mensaje })]);
            onCuentaCambiada?.();
        } catch (err) {
            cambiar(mensaje.id, { estadoAccion: 'pendiente' });
            setMensajes((lista) => [...lista, nuevo({
                rol: 'asistente',
                texto: mensajeError(err, 'No se pudo cambiar el estado de la cuenta.'),
                error: true,
            })]);
        }
    };

    const cancelar = (mensaje) => cambiar(mensaje.id, { estadoAccion: 'cancelada' });

    return { mensajes, enviando, enviar, confirmar, cancelar };
}
