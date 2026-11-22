import { useEffect, useState } from 'react';
import { listarCategorias } from '../api/categoriasApi';
import useDebounce from '../hooks/useDebounce';
import useSenas from '../hooks/useSenas';
import { useResumen } from '../layout/ResumenContext';
import ConfirmarEliminar from './senas/ConfirmarEliminar';
import Filtros from './senas/Filtros';
import TablaSenas from './senas/TablaSenas';
import TarjetasResumen from './senas/TarjetasResumen';
import VerVideo from './senas/VerVideo';
import '../estilos/Comun.css';
import '../estilos/Senas.css';

export default function Senas() {
    const { resumen, recargar } = useResumen();
    const [texto, setTexto] = useState('');
    const [categoria, setCategoria] = useState('');
    const [categorias, setCategorias] = useState([]);
    const [verVideo, setVerVideo] = useState(null);
    const [aEliminar, setAEliminar] = useState(null);

    const q = useDebounce(texto.trim(), 300);
    const { senas, cargando, error, reintentar, refrescar } = useSenas({ q, categoria });

    useEffect(() => {
        listarCategorias()
            .then(({ data }) => setCategorias(data))
            .catch(() => {
                /* sin categorías: el filtro solo muestra "Todas" */
            });
    }, []);

    const tras = () => {
        setAEliminar(null);
        refrescar();
        recargar();
    };

    return (
        <>
            <TarjetasResumen resumen={resumen} />
            <Filtros
                texto={texto}
                onTexto={setTexto}
                categoria={categoria}
                onCategoria={setCategoria}
                categorias={categorias}
            />
            <TablaSenas
                senas={senas}
                cargando={cargando}
                error={error}
                hayFiltro={Boolean(q || categoria)}
                onReintentar={reintentar}
                onVer={setVerVideo}
                onEliminar={setAEliminar}
            />

            {verVideo && <VerVideo sena={verVideo} onCerrar={() => setVerVideo(null)} />}
            {aEliminar && (
                <ConfirmarEliminar sena={aEliminar} onCerrar={() => setAEliminar(null)} onEliminada={tras} />
            )}
        </>
    );
}