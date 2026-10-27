import axios from 'axios';

const API_BASE_URL = 'http://127.0.0.1:8000/api';

export const getSenas = () => axios.get(`${API_BASE_URL}/senas/`);

// Configurado para enviar tanto texto como archivos de video
export const createSena = (formData) =>
    axios.post(`${API_BASE_URL}/senas/`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    });

export const updateSena = (id, formData) =>
    axios.put(`${API_BASE_URL}/senas/${id}/`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    });

export const deleteSena = (id) => axios.delete(`${API_BASE_URL}/senas/${id}/`);

// Servicio para entrenamiento e inferencia de IA (landmarks de la mano)
export const predecirSena = (landmarks) =>
    axios.post(`${API_BASE_URL}/predecir/`, { landmarks });