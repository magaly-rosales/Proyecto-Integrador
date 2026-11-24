const plural = (n) => `${n} ${n === 1 ? 'usuario' : 'usuarios'}`;

// Gráfico circular de cuentas activas e inactivas (números y porcentajes del backend)
export default function UsuariosEstado({ usuarios }) {
    const { activos, inactivos, porcentaje_activos: pActivos, porcentaje_inactivos: pInactivos } = usuarios;

    return (
        <div className="usuarios-estado">
            <div
                className="dona"
                role="img"
                aria-label={`${pActivos}% de cuentas activas y ${pInactivos}% inactivas`}
            >
                <svg viewBox="0 0 42 42" aria-hidden="true">
                    {/* Círculo completo = inactivos; encima, el arco de activos (pathLength=100 => 1 unidad = 1%) */}
                    <circle className="dona-inactivos" cx="21" cy="21" r="15.9155" pathLength="100" />
                    <circle
                        className="dona-activos"
                        cx="21"
                        cy="21"
                        r="15.9155"
                        pathLength="100"
                        strokeDasharray={`${pActivos} 100`}
                    />
                </svg>
                <div className="dona-centro">
                    <strong>{pActivos}%</strong>
                    <span>activos</span>
                </div>
            </div>

            <ul className="dona-leyenda">
                <li>
                    <i className="cuadro cuadro-activos" aria-hidden="true" />
                    <div>
                        <strong>Activos</strong>
                        <span>{plural(activos)}</span>
                    </div>
                    <b>{pActivos}%</b>
                </li>
                <li>
                    <i className="cuadro cuadro-inactivos" aria-hidden="true" />
                    <div>
                        <strong>Inactivos</strong>
                        <span>{plural(inactivos)}</span>
                    </div>
                    <b>{pInactivos}%</b>
                </li>
            </ul>
        </div>
    );
}
