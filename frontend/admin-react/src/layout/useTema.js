import { useCallback, useEffect, useState } from 'react';

const CLAVE = 'admin_tema';

function temaInicial() {
    try {
        const guardado = localStorage.getItem(CLAVE);
        if (guardado === 'dark' || guardado === 'light') return guardado;
    } catch {
        /* sin almacenamiento: se usa el claro */
    }
    return 'light';
}

// Tema claro/oscuro: se aplica en <html data-theme="..."> y se recuerda en el navegador
export default function useTema() {
    const [tema, setTema] = useState(temaInicial);

    useEffect(() => {
        document.documentElement.dataset.theme = tema;
        try {
            localStorage.setItem(CLAVE, tema);
        } catch {
            /* no se pudo recordar */
        }
    }, [tema]);

    const alternar = useCallback(() => setTema((t) => (t === 'dark' ? 'light' : 'dark')), []);
    return { tema, alternar };
}