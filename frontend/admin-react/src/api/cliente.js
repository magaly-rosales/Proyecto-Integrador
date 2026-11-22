import axios from 'axios';

export const API_URL = 'http://127.0.0.1:8000/api';
export const EVENTO_SESION_VENCIDA = 'sesion-vencida';

const CLAVE_TOKEN = 'admin_token';
const CLAVE_USUARIO = 'admin_usuario';

// Sesión guardada en el navegador (con try/catch por si localStorage falla)
export const sesion = {
    token() {
        try {
            return localStorage.getItem(CLAVE_TOKEN);
        } catch {
            return null;
        }
    },
    usuario() {
        try {
            return JSON.parse(localStorage.getItem(CLAVE_USUARIO));
        } catch {
            return null;
        }
    },
    guardar(token, usuario) {
        try {
            localStorage.setItem(CLAVE_TOKEN, token);
            localStorage.setItem(CLAVE_USUARIO, JSON.stringify(usuario));
        } catch {
            /* sin almacenamiento: la sesión dura hasta recargar */
        }
    },
    borrar() {
        try {
            localStorage.removeItem(CLAVE_TOKEN);
            localStorage.removeItem(CLAVE_USUARIO);
        } catch {
            /* nada que borrar */
        }
    },
};

const cliente = axios.create({ baseURL: API_URL });

// Envía el token en cada petición
cliente.interceptors.request.use((config) => {
    const token = sesion.token();
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
});

// Si Django rechaza la sesión, se avisa para volver al login
cliente.interceptors.response.use(
    (respuesta) => respuesta,
    (error) => {
        const estado = error.response?.status;
        const esLogin = error.config?.url?.includes('/login/');
        if ((estado === 401 || estado === 403) && !esLogin) {
            sesion.borrar();
            window.dispatchEvent(new Event(EVENTO_SESION_VENCIDA));
        }
        return Promise.reject(error);
    }
);

export default cliente;