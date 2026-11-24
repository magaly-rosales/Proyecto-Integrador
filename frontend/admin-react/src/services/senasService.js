import axios from 'axios';

const API_BASE_URL = 'http://127.0.0.1:8000/api';
const TOKEN_KEY = 'admin_token';

// Envía el token en cada petición; si el servidor lo rechaza, vuelve al login
axios.interceptors.request.use((config) => {
    const token = localStorage.getItem(TOKEN_KEY);
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
});

axios.interceptors.response.use(
    (respuesta) => respuesta,
    (error) => {
        const estado = error.response?.status;
        const esLogin = error.config?.url?.endsWith('/login/');
        if ((estado === 401 || estado === 403) && !esLogin) {
            localStorage.removeItem(TOKEN_KEY);
            window.location.reload();
        }
        return Promise.reject(error);
    }
);

export const loginAdmin = (email, password) =>
    axios.post(`${API_BASE_URL}/login/`, { email, password });

export const getSenas = () => axios.get(`${API_BASE_URL}/senas/`);

export const createSena = (formData) =>
    axios.post(`${API_BASE_URL}/senas/`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    });

export const updateSena = (id, formData) =>
    axios.put(`${API_BASE_URL}/senas/${id}/`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    });

export const deleteSena = (id) => axios.delete(`${API_BASE_URL}/senas/${id}/`);

export const predecirSena = (landmarks) =>
    axios.post(`${API_BASE_URL}/predecir/`, { landmarks });

// --- MÓDULO DE GESTIÓN DE USUARIOS (Commit 4) ---
export const getUsuarios = () => axios.get(`${API_BASE_URL}/usuarios/`);

export const toggleEstadoUsuario = (id, is_active) =>
    axios.patch(`${API_BASE_URL}/usuarios/${id}/`, { is_active });

// --- MÓDULO DE DASHBOARD Y REGISTRO DE USO (Commit 5) ---
export const getEstadisticasUso = () => axios.get(`${API_BASE_URL}/estadisticas/`);

export const getRegistroUso = () => axios.get(`${API_BASE_URL}/registro-uso/`);
// --- ENTRENAMIENTO ---
export const getResumenEntrenamiento = () => axios.get(`${API_BASE_URL}/entrenamiento/`);

export const borrarEntrenamiento = (id, tipo = 'todas') =>
    axios.delete(`${API_BASE_URL}/entrenamiento/${id}/`, { params: { tipo } });


// Mismo orden que usa la app del usuario: 1 mano = 21 puntos; 2 manos = Left primero y luego Right
const aPuntos = (manos) => {
    const ordenadas =
        manos.length === 2
            ? [...manos].sort((a, b) => (a.etiqueta === b.etiqueta ? 0 : a.etiqueta === 'Left' ? -1 : 1))
            : manos;
    return ordenadas.flatMap((mano) => mano.puntos.map(({ x, y, z }) => ({ x, y, z })));
};

export const guardarMuestra = (idSena, manos) =>
    axios.post(`${API_BASE_URL}/entrenamiento/muestras/${idSena}/`, aPuntos(manos));

export const guardarSecuencia = (idSena, cuadros) =>
    axios.post(`${API_BASE_URL}/entrenamiento/secuencias/${idSena}/`, cuadros.map(aPuntos));







