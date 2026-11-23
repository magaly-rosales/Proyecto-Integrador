import { useEffect, useState } from 'react';
import { listarCategorias } from '../api/categoriasApi';
import useDebounce from '../hooks/useDebounce';
import useSenas from '../hooks/useSenas';
import { useResumen } from '../layout/ResumenContext';
import ConfirmarEliminar from './senas/ConfirmarEliminar';
import Filtros from './senas/Filtros';
import FormularioSena from './senas/FormularioSena';
import TablaSenas from './senas/TablaSenas';
import TarjetasResumen from './senas/TarjetasResumen';
import VerVideo from './senas/VerVideo';
import '../estilos/Comun.css';
import '../estilos/Senas.css';
import VerImagen from './senas/VerImagen';

export default function Senas() {
    const { resumen, recargar } = useResumen();
    const [texto, setTexto] = useState('');
    const [categoria, setCategoria] = useState('');
    const [categorias, setCategorias] = useState([]);
    const [verVideo, setVerVideo] = useState(null);
    const [verImagen, setVerImagen] = useState(null);
    const [aEliminar, setAEliminar] = useState(null);
    const [formulario, setFormulario] = useState(null); // { sena: null } = nueva; { sena } = editar

    const q = useDebounce(texto.trim(), 300);
    const { senas, cargando, error, reintentar, refrescar } = useSenas({ q, categoria });

    useEffect(() => {
        listarCategorias()
            .then(({ data }) => setCategorias(data))
            .catch(() => {
                /* sin categorías: el filtro solo muestra "Todas" */
            });
    }, []);

    // Tras crear, editar o eliminar: se cierra la ventana y se actualizan lista y números
    const tras = () => {
        setAEliminar(null);
        setFormulario(null);
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
                onNueva={() => setFormulario({ sena: null })}
            />
            <TablaSenas
                senas={senas}
                cargando={cargando}
                error={error}
                hayFiltro={Boolean(q || categoria)}
                onReintentar={reintentar}
                onVer={setVerVideo}
                onEditar={(sena) => setFormulario({ sena })}
                onEliminar={setAEliminar}
                onVerImagen={setVerImagen}
            />

            {verVideo && <VerVideo sena={verVideo} onCerrar={() => setVerVideo(null)} />}
            {verImagen && <VerImagen sena={verImagen} onCerrar={() => setVerImagen(null)} />}
            {aEliminar && (
                <ConfirmarEliminar sena={aEliminar} onCerrar={() => setAEliminar(null)} onEliminada={tras} />
            )}
            {formulario && (
                <FormularioSena
                    sena={formulario.sena}
                    categorias={categorias}
                    onCerrar={() => setFormulario(null)}
                    onGuardada={tras}
                />
            )}
        </>
    );
}