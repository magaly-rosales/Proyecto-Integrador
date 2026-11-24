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

    // Actualiza el nombre y el correo que se muestran (y los guardados) después de editar el perfil
    const actualizarUsuario = useCallback((datos) => {
        setUsuario((actual) => {
            const nuevo = { ...actual, ...datos };
            sesion.guardar(sesion.token(), nuevo);
            return nuevo;
        });
    }, []);

    // Si Django rechaza el token, se cierra la sesión
    useEffect(() => {
        window.addEventListener(EVENTO_SESION_VENCIDA, cerrarSesion);
        return () => window.removeEventListener(EVENTO_SESION_VENCIDA, cerrarSesion);
    }, [cerrarSesion]);

    const valor = useMemo(
        () => ({ autenticado: Boolean(token), usuario, cerrarSesion, actualizarUsuario }),
        [token, usuario, cerrarSesion, actualizarUsuario]
    );

    return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>;
}