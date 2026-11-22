import cliente from './cliente';

// Filtros opcionales: { q, categoria }
export const listarSenas = (params) => cliente.get('/senas/', { params });

export const eliminarSena = (id) => cliente.delete(`/senas/${id}/`);