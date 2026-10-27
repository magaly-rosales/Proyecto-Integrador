import axios from 'axios';

const API_BASE_URL = 'http://127.0.0.1:8000/api';

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