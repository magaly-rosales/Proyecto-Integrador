import cliente from './cliente';

// Filtros opcionales: { q, categoria }
export const listarSenas = (params) => cliente.get('/senas/', { params });

// Con archivos se envía FormData (axios agrega solo el Content-Type correcto)
export const crearSena = (formData) => cliente.post('/senas/', formData);

export const actualizarSena = (id, formData) => cliente.patch(`/senas/${id}/`, formData);

export const eliminarSena = (id) => cliente.delete(`/senas/${id}/`);