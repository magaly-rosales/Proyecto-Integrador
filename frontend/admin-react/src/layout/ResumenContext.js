import { createContext, useContext } from 'react';

// Números del menú y de las tarjetas (señas, categorías, usuarios), compartidos por todas las pantallas
export const ResumenContext = createContext({ resumen: null, recargar: () => {} });

export const useResumen = () => useContext(ResumenContext);