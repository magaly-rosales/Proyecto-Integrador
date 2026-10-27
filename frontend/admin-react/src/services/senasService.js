import axios from 'axios';

const API_BASE_URL = 'http://127.0.0.1:8000/api';

export const getSenas = () => axios.get(`${API_BASE_URL}/senas/`);
export const createSena = (data) => axios.post(`${API_BASE_URL}/senas/`, data);
export const updateSena = (id, data) => axios.put(`${API_BASE_URL}/senas/${id}/`, data);
export const deleteSena = (id) => axios.delete(`${API_BASE_URL}/senas/${id}/`);