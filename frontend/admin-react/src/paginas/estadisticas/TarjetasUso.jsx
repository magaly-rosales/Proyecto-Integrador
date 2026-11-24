import { textoPeriodo } from './textoPeriodo';

function Tarjeta({ titulo, valor, nota }) {
    return (
        <article className="estadistica-tarjeta">
            <h3>{titulo}</h3>
            <strong>{valor}</strong>
            {nota && <p>{nota}</p>}
        </article>
    );
}

// Tres tarjetas con los números que entrega /api/estadisticas/
export default function TarjetasUso({ datos, filtro }) {
    const d = datos;
    const masTraducida = d?.mas_traducida;
    const u = d?.usuarios;

    return (
        <section className="estadisticas-tarjetas" aria-label="Resumen de uso">
            <Tarjeta
                titulo="Total de traducciones"
                valor={d ? d.total_traducciones : '—'}
                nota={d && textoPeriodo(filtro, d.rango)}
            />
            <Tarjeta
                titulo="Seña más traducida"
                valor={d ? (masTraducida ? masTraducida.nombre : 'Sin datos') : '—'}
                nota={masTraducida
                    ? `${masTraducida.total} veces${masTraducida.siguiente ? ` · luego ${masTraducida.siguiente}` : ''}`
                    : d && 'Aún no hay traducciones en este periodo'}
            />
            <Tarjeta
                titulo="Usuarios activos"
                valor={u ? `${u.activos}/${u.total}` : '—'}
                nota={u && 'Con cuenta habilitada'}
            />
        </section>
    );
}
