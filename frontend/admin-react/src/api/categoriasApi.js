import cliente from './cliente';

export const listarCategorias = () => cliente.get('/categorias/');