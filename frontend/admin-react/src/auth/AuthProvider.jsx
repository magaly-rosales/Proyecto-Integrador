import { useCallback, useEffect, useMemo, useState } from 'react';
import { EVENTO_SESION_VENCIDA, sesion } from '../api/cliente';
import { AuthContext } from './AuthContext';
import { URL_ACCESO } from './rutas';

export default function AuthProvider({ children }) {
    const [token, setToken] = useState(() => sesion.token());
    const [usuario, setUsuario] = useState(() => sesion.usuario());

    // Cierra la sesión y vuelve a la pantalla de acceso general
    const cerrarSesion = useCallback(() => {
        sesion.borrar();
        setToken(null);
        setUsuario(null);
        window.location.replace(URL_ACCESO);
    }, []);

    // Si Django rechaza el token, se cierra la sesión
    useEffect(() => {
        window.addEventListener(EVENTO_SESION_VENCIDA, cerrarSesion);
        return () => window.removeEventListener(EVENTO_SESION_VENCIDA, cerrarSesion);
    }, [cerrarSesion]);

    const valor = useMemo(
        () => ({ autenticado: Boolean(token), usuario, cerrarSesion }),
        [token, usuario, cerrarSesion]
    );

    return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>;
}