import cliente from './cliente';

// Filtros: { dias: '7' | '30' | 'todo' }  o  { desde: 'AAAA-MM-DD', hasta: 'AAAA-MM-DD' }
// Los totales, porcentajes y rankings los calcula el backend.
export const obtenerEstadisticas = (params) => cliente.get('/estadisticas/', { params });
