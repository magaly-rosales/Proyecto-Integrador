const OPCIONES = [
    { valor: '7', texto: '7 días' },
    { valor: '30', texto: '30 días' },
    { valor: 'todo', texto: 'Todo' },
];

// Elige el periodo: últimos 7 o 30 días, todo, o un rango propio de fechas
export default function Periodo({ filtro, onCambiar }) {
    const { dias, desde, hasta } = filtro;

    return (
        <div className="periodo">
            <div className="periodo-botones" role="group" aria-label="Periodo">
                {OPCIONES.map((o) => (
                    <button
                        key={o.valor}
                        type="button"
                        className={`periodo-boton ${dias === o.valor ? 'activo' : ''}`}
                        aria-pressed={dias === o.valor}
                        onClick={() => onCambiar({ dias: o.valor, desde: '', hasta: '' })}
                    >
                        {o.texto}
                    </button>
                ))}
            </div>

            <label className="periodo-fecha">
                <span>Desde</span>
                <input
                    type="date"
                    value={desde}
                    max={hasta || undefined}
                    onChange={(e) => onCambiar({ dias: '', desde: e.target.value, hasta })}
                />
            </label>

            <label className="periodo-fecha">
                <span>Hasta</span>
                <input
                    type="date"
                    value={hasta}
                    min={desde || undefined}
                    onChange={(e) => onCambiar({ dias: '', desde, hasta: e.target.value })}
                />
            </label>
        </div>
    );
}
