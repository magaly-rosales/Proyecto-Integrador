import cliente from './cliente';

export const getResumen = () => cliente.get('/resumen/');