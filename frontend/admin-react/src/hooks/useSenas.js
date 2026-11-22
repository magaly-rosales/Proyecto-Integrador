import { useCallback, useEffect, useState } from 'react';
import { listarSenas } from '../api/senasApi';
import { mensajeError } from '../api/errores';

// Lista de señas con los filtros de búsqueda y categoría
export default function useSenas({ q, categoria }) {
    const [estado, setEstado] = useState({ cargando: true, error: '', senas: [] });
    const [version, setVersion] = useState(0);

    useEffect(() => {
        let vigente = true;
        listarSenas({ q: q || undefined, categoria: categoria || undefined })
            .then(({ data }) => {
                if (vigente) setEstado({ cargando: false, error: '', senas: data });
            })
            .catch((err) => {
                if (vigente) {
                    setEstado((e) => ({
                        ...e,
                        cargando: false,
                        error: mensajeError(err, 'No se pudieron cargar las señas.'),
                    }));
                }
            });
        return () => {
            vigente = false;
        };
    }, [q, categoria, version]);

    // Reintentar muestra el estado de carga; refrescar actualiza sin parpadeo
    const reintentar = useCallback(() => {
        setEstado((e) => ({ ...e, cargando: true, error: '' }));
        setVersion((v) => v + 1);
    }, []);
    const refrescar = useCallback(() => setVersion((v) => v + 1), []);

    return { ...estado, reintentar, refrescar };
}