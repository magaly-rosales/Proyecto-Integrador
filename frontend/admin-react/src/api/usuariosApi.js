import cliente from './cliente';

// Filtros opcionales: { q, estado } (estado: 'activo' | 'inactivo')
export const listarUsuarios = (params) => cliente.get('/usuarios/', { params });

// Activa o desactiva la cuenta; el cambio lo hace el backend
export const cambiarEstadoUsuario = (id, activo) => cliente.patch(`/usuarios/${id}/`, { is_active: activo });
