import { useEffect } from 'react';
import { useAuth } from './AuthContext';
import { URL_ACCESO } from './rutas';

// Sin sesión no hay login propio: vuelve a la pantalla de acceso general
export default function RutaProtegida({ children }) {
    const { autenticado } = useAuth();

    useEffect(() => {
        if (!autenticado) window.location.replace(URL_ACCESO);
    }, [autenticado]);

    return autenticado ? children : null;
}