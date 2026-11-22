import { useEffect, useState } from 'react';

// Devuelve el valor solo después de que deja de cambiar durante `retraso` ms
export default function useDebounce(valor, retraso = 300) {
    const [estable, setEstable] = useState(valor);

    useEffect(() => {
        const id = setTimeout(() => setEstable(valor), retraso);
        return () => clearTimeout(id);
    }, [valor, retraso]);

    return estable;
}