import { useCallback, useEffect, useMemo, useState } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import { getResumen } from '../api/resumenApi';
import useAsistente from '../hooks/useAsistente';
import { AsistenteContext } from './AsistenteContext';
import AsistenteFlotante from './AsistenteFlotante';
import Cabecera from './Cabecera';
import { ResumenContext } from './ResumenContext';
import { PERFIL, SECCIONES } from './secciones';
import Sidebar from './Sidebar';
import useTema from './useTema';
import '../estilos/Layout.css';

export default function Layout() {
    const { pathname } = useLocation();
    const { tema, alternar } = useTema();
    const [menuAbierto, setMenuAbierto] = useState(false);
    const [resumen, setResumen] = useState(null);
    const [version, setVersion] = useState(0);

    const seccion = [...SECCIONES, PERFIL].find((s) => pathname.startsWith(s.ruta)) || SECCIONES[0];

    const recargar = useCallback(() => setVersion((v) => v + 1), []);

    // La conversación del asistente vive aquí para que no se pierda al cambiar de sección
    const asistente = useAsistente({ onCuentaCambiada: recargar });

    // Trae los números del menú; las pantallas piden recargarlos al crear/borrar algo
    useEffect(() => {
        let vigente = true;
        getResumen()
            .then(({ data }) => vigente && setResumen(data))
            .catch(() => {
                /* sin números: el menú se muestra sin contadores */
            });
        return () => {
            vigente = false;
        };
    }, [version]);

    const valor = useMemo(() => ({ resumen, recargar }), [resumen, recargar]);

    return (
        <ResumenContext.Provider value={valor}>
            <AsistenteContext.Provider value={asistente}>
                <div className={`app${menuAbierto ? ' menu-abierto' : ''}`}>
                    <Sidebar resumen={resumen} onNavegar={() => setMenuAbierto(false)} />
                    <div className="app-velo" onClick={() => setMenuAbierto(false)} />

                    <div className="app-contenido">
                        <Cabecera
                            seccion={seccion}
                            tema={tema}
                            onAlternarTema={alternar}
                            onMenu={() => setMenuAbierto(true)}
                        />
                        <main className="app-principal">
                            <Outlet />
                        </main>
                    </div>
                </div>
                <AsistenteFlotante />
            </AsistenteContext.Provider>
        </ResumenContext.Provider>
    );
}
