import { useCallback, useEffect, useState } from 'react';
import { listarUsuarios } from '../api/usuariosApi';
import { mensajeError } from '../api/errores';

// Lista de usuarios con los filtros de búsqueda y estado (el filtrado lo hace el backend)
export default function useUsuarios({ q, estado }) {
    const [datos, setDatos] = useState({ cargando: true, error: '', usuarios: [] });
    const [version, setVersion] = useState(0);

    useEffect(() => {
        let vigente = true;
        listarUsuarios({ q: q || undefined, estado: estado || undefined })
            .then(({ data }) => {
                if (vigente) setDatos({ cargando: false, error: '', usuarios: data });
            })
            .catch((err) => {
                if (vigente) {
                    setDatos((d) => ({
                        ...d,
                        cargando: false,
                        error: mensajeError(err, 'No se pudieron cargar los usuarios.'),
                    }));
                }
            });
        return () => {
            vigente = false;
        };
    }, [q, estado, version]);

    const reintentar = useCallback(() => {
        setDatos((d) => ({ ...d, cargando: true, error: '' }));
        setVersion((v) => v + 1);
    }, []);
    const refrescar = useCallback(() => setVersion((v) => v + 1), []);

    return { ...datos, reintentar, refrescar };
}
