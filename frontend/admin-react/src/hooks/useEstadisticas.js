import { useCallback, useEffect, useState } from 'react';
import { obtenerEstadisticas } from '../api/estadisticasApi';
import { mensajeError } from '../api/errores';

// Pide las estadísticas al backend según el periodo elegido
export default function useEstadisticas({ dias, desde, hasta }) {
    const [version, setVersion] = useState(0);
    const [estado, setEstado] = useState({ clave: '', datos: null, error: '' });

    // Identifica la consulta actual: mientras no coincida con la respuesta, se está cargando
    const clave = JSON.stringify([dias, desde, hasta, version]);

    useEffect(() => {
        let vigente = true;
        const params = desde || hasta ? { desde: desde || undefined, hasta: hasta || undefined } : { dias };
        obtenerEstadisticas(params)
            .then(({ data }) => {
                if (vigente) setEstado({ clave, datos: data, error: '' });
            })
            .catch((err) => {
                if (vigente) {
                    setEstado({
                        clave,
                        datos: null,
                        error: mensajeError(err, 'No se pudieron cargar las estadísticas.'),
                    });
                }
            });
        return () => {
            vigente = false;
        };
    }, [clave, dias, desde, hasta]);

    const reintentar = useCallback(() => setVersion((v) => v + 1), []);

    return {
        datos: estado.datos,
        error: estado.clave === clave ? estado.error : '',
        cargando: estado.clave !== clave,
        reintentar,
    };
}
