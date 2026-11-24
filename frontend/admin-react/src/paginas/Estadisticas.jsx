import { useState } from 'react';
import { IconoAlerta } from '../components/ui/IconosUi';
import useEstadisticas from '../hooks/useEstadisticas';
import Periodo from './estadisticas/Periodo';
import { textoPeriodo } from './estadisticas/periodo';
import RankingSenas from './estadisticas/RankingSenas';
import TarjetasUso from './estadisticas/TarjetasUso';
import UsuariosEstado from './estadisticas/UsuariosEstado';
import '../estilos/Comun.css';
import '../estilos/Senas.css';
import '../estilos/Estadisticas.css';

const plural = (n) => `${n} ${n === 1 ? 'usuario' : 'usuarios'}`;

export default function Estadisticas() {
    const [filtro, setFiltro] = useState({ dias: '30', desde: '', hasta: '' });
    const { datos, cargando, error, reintentar } = useEstadisticas(filtro);

    return (
        <>
            <Periodo filtro={filtro} onCambiar={setFiltro} />

            {error ? (
                <section className="tabla-caja">
                    <div className="tabla-aviso">
                        <IconoAlerta tam={28} />
                        <strong>No se pudieron cargar las estadísticas</strong>
                        <p>{error}</p>
                        <button type="button" className="boton-contorno" onClick={reintentar}>Reintentar</button>
                    </div>
                </section>
            ) : (
                <>
                    <TarjetasUso datos={datos} filtro={filtro} />

                    {cargando && !datos ? (
                        <section className="tabla-caja">
                            <div className="tabla-carga" role="status" aria-label="Cargando estadísticas">
                                {Array.from({ length: 5 }, (_, i) => <span key={i} />)}
                            </div>
                        </section>
                    ) : (
                        datos && (
                            <div className={`estadisticas-paneles ${cargando ? 'actualizando' : ''}`}>
                                <section className="tabla-caja panel">
                                    <header className="panel-cabecera">
                                        <h2>Traducciones por seña</h2>
                                        <span className="texto-suave">
                                            Top 10 · % del total · {textoPeriodo(filtro, datos.rango)}
                                        </span>
                                    </header>
                                    <RankingSenas top={datos.top} />
                                </section>
                                <section className="tabla-caja panel">
                                    <header className="panel-cabecera">
                                        <h2>Usuarios activos e inactivos</h2>
                                        <span className="texto-suave">{plural(datos.usuarios.total)}</span>
                                    </header>
                                    <UsuariosEstado usuarios={datos.usuarios} />
                                </section>
                            </div>
                        )
                    )}
                </>
            )}
        </>
    );
}
