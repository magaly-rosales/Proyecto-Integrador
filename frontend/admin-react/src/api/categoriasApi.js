import cliente from './cliente';

export const listarCategorias = () => cliente.get('/categorias/');

// Con imagen se envía FormData: nombre, color e imagen
export const crearCategoria = (formData) => cliente.post('/categorias/', formData);

export const actualizarCategoria = (id, formData) => cliente.patch(`/categorias/${id}/`, formData);

export const eliminarCategoria = (id) => cliente.delete(`/categorias/${id}/`);

// Señas de una categoría: [{ id, nombre, descripcion, imagen }]
export const listarSenasDeCategoria = (id) => cliente.get(`/categorias/${id}/senas/`);