import { useCallback, useEffect, useState } from 'react';
import { listarCategorias } from '../api/categoriasApi';
import { mensajeError } from '../api/errores';

// Lista de categorías (con su total de señas)
export default function useCategorias() {
    const [estado, setEstado] = useState({ cargando: true, error: '', categorias: [] });
    const [version, setVersion] = useState(0);

    useEffect(() => {
        let vigente = true;
        listarCategorias()
            .then(({ data }) => {
                if (vigente) setEstado({ cargando: false, error: '', categorias: data });
            })
            .catch((err) => {
                if (vigente) {
                    setEstado((e) => ({
                        ...e,
                        cargando: false,
                        error: mensajeError(err, 'No se pudieron cargar las categorías.'),
                    }));
                }
            });
        return () => {
            vigente = false;
        };
    }, [version]);

    const reintentar = useCallback(() => {
        setEstado((e) => ({ ...e, cargando: true, error: '' }));
        setVersion((v) => v + 1);
    }, []);
    const refrescar = useCallback(() => setVersion((v) => v + 1), []);

    return { ...estado, reintentar, refrescar };
}