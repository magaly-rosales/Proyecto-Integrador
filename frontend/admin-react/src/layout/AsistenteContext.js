import { createContext, useContext } from 'react';

// Conversación del asistente compartida: la misma en la ventana flotante y en la sección Asistente
export const AsistenteContext = createContext(null);

export function useChatAsistente() {
    const contexto = useContext(AsistenteContext);
    if (!contexto) throw new Error('useChatAsistente debe usarse dentro del Layout');
    return contexto;
}
