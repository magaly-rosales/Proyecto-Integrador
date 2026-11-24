// Traducciones por seña: nombre, barra y porcentaje del total (todo viene calculado del backend)
export default function RankingSenas({ top }) {
    if (top.length === 0) {
        return (
            <div className="tabla-aviso">
                <strong>Sin traducciones en este periodo</strong>
                <p>Cuando los usuarios traduzcan señas, aparecerán aquí ordenadas de la más a la menos usada.</p>
            </div>
        );
    }

    return (
        <ol className="ranking">
            {top.map((t, i) => (
                <li key={t.nombre} className="ranking-fila" title={`${t.nombre}: ${t.total} traducciones`}>
                    <span className="ranking-nombre">{t.nombre}</span>
                    <div
                        className="barra"
                        role="progressbar"
                        aria-valuenow={t.porcentaje}
                        aria-valuemin={0}
                        aria-valuemax={100}
                        aria-label={`${t.nombre}: ${t.porcentaje}%`}
                    >
                        <span className={i === 0 ? 'barra-primera' : ''} style={{ width: `${t.porcentaje}%` }} />
                    </div>
                    <strong className="ranking-porcentaje">{t.porcentaje}%</strong>
                </li>
            ))}
        </ol>
    );
}
