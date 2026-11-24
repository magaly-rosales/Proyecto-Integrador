import cliente from './cliente';

// Orden que espera el backend: 1 mano = 21 puntos; 2 manos = la izquierda (Left) primero y luego la derecha (Right).
// Solo se da forma a los datos; el backend los procesa y los guarda.
const aPuntos = (manos) => {
    const ordenadas = manos.length === 2
        ? [...manos].sort((a, b) => (a.etiqueta === b.etiqueta ? 0 : a.etiqueta === 'Left' ? -1 : 1))
        : manos;
    return ordenadas.flatMap((mano) => mano.puntos.map(({ x, y, z }) => ({ x, y, z })));
};

// Una muestra: los puntos de la mano en un instante (señas quietas)
export const guardarMuestra = (idSena, manos) =>
    cliente.post(`/entrenamiento/muestras/${idSena}/`, aPuntos(manos));

// Un movimiento: varios cuadros seguidos (señas con movimiento)
export const guardarSecuencia = (idSena, cuadros) =>
    cliente.post(`/entrenamiento/secuencias/${idSena}/`, cuadros.map(aPuntos));

// tipo: 'muestras' | 'secuencias' | 'todas'
export const borrarEntrenamiento = (idSena, tipo = 'todas') =>
    cliente.delete(`/entrenamiento/${idSena}/`, { params: { tipo } });
