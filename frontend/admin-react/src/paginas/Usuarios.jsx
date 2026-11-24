import { useState } from 'react';
import useDebounce from '../hooks/useDebounce';
import useUsuarios from '../hooks/useUsuarios';
import { useResumen } from '../layout/ResumenContext';
import TarjetasResumen from './senas/TarjetasResumen';
import ConfirmarEstado from './usuarios/ConfirmarEstado';
import FiltrosUsuarios from './usuarios/FiltrosUsuarios';
import TablaUsuarios from './usuarios/TablaUsuarios';
import '../estilos/Comun.css';
import '../estilos/Senas.css';
import '../estilos/Usuarios.css';

export default function Usuarios() {
    const { resumen, recargar } = useResumen();
    const [texto, setTexto] = useState('');
    const [estado, setEstado] = useState('');
    const [aCambiar, setACambiar] = useState(null);

    const q = useDebounce(texto.trim(), 300);
    const { usuarios, cargando, error, reintentar, refrescar } = useUsuarios({ q, estado });

    // Tras activar/desactivar: se cierra la ventana y se actualizan lista y números
    const tras = () => {
        setACambiar(null);
        refrescar();
        recargar();
    };

    return (
        <>
            <TarjetasResumen resumen={resumen} />
            <FiltrosUsuarios texto={texto} onTexto={setTexto} estado={estado} onEstado={setEstado} />
            <TablaUsuarios
                usuarios={usuarios}
                total={resumen?.usuarios.total}
                cargando={cargando}
                error={error}
                hayFiltro={Boolean(q || estado)}
                onReintentar={reintentar}
                onCambiar={setACambiar}
            />
            {aCambiar && (
                <ConfirmarEstado usuario={aCambiar} onCerrar={() => setACambiar(null)} onCambiado={tras} />
            )}
        </>
    );
}
