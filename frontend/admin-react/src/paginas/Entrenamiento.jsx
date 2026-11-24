import { useEffect, useState } from 'react';
import { listarCategorias } from '../api/categoriasApi';
import useSenas from '../hooks/useSenas';
import ConfirmarBorrado from './entrenamiento/ConfirmarBorrado';
import ListaEntrenamiento from './entrenamiento/ListaEntrenamiento';
import PanelCaptura from './entrenamiento/PanelCaptura';
import '../estilos/Comun.css';
import '../estilos/Senas.css';
import '../estilos/Estadisticas.css';
import '../estilos/Captura.css';

export default function Entrenamiento() {
    const [idSena, setIdSena] = useState('');
    const [categorias, setCategorias] = useState([]);
    const [aBorrar, setABorrar] = useState(null);

    // El estado de cada seña (muestras, movimientos, lo que falta y si está entrenada) lo calcula el backend
    const { senas, cargando, error, reintentar, refrescar } = useSenas({});

    useEffect(() => {
        listarCategorias()
            .then(({ data }) => setCategorias(data))
            .catch(() => {
                /* sin categorías: el filtro solo muestra "Todas" */
            });
    }, []);

    const seleccionada = senas.find((s) => String(s.id) === String(idSena)) || null;

    return (
        <>
            <div className="captura-grid">
                <PanelCaptura sena={seleccionada} onGuardado={refrescar} onBorrar={setABorrar} />
                <ListaEntrenamiento
                    senas={senas}
                    categorias={categorias}
                    cargando={cargando}
                    error={error}
                    idSena={idSena}
                    onReintentar={reintentar}
                    onElegir={setIdSena}
                />
            </div>

            {aBorrar && (
                <ConfirmarBorrado
                    sena={aBorrar}
                    onCerrar={() => setABorrar(null)}
                    onBorrado={() => {
                        setABorrar(null);
                        refrescar();
                    }}
                />
            )}
        </>
    );
}
