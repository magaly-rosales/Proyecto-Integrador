import { IconoCategorias, IconoMano, IconoUsuarios } from '../../layout/Iconos';

const plural = (n, uno, varios) => `${n} ${n === 1 ? uno : varios}`;

function Tarjeta({ icono, titulo, valor, nota }) {
    return (
        <article className="resumen-tarjeta">
            <span className="resumen-icono">{icono}</span>
            <div>
                <h3>{titulo}</h3>
                <strong>{valor}</strong>
                {nota && <p>{nota}</p>}
            </div>
        </article>
    );
}

// Tres tarjetas con los números que entrega /api/resumen/
export default function TarjetasResumen({ resumen }) {
    const r = resumen;
    return (
        <section className="resumen" aria-label="Resumen">
            <Tarjeta
                icono={<IconoMano />}
                titulo="Señas registradas"
                valor={r ? r.senas.total : '—'}
                nota={r && `${plural(r.senas.nuevas_semana, 'nueva', 'nuevas')} esta semana`}
            />
            <Tarjeta
                icono={<IconoCategorias />}
                titulo="Categorías"
                valor={r ? r.categorias.total : '—'}
            />
            <Tarjeta
                icono={<IconoUsuarios />}
                titulo="Usuarios activos"
                valor={r ? `${r.usuarios.activos}/${r.usuarios.total}` : '—'}
                nota={r && `${plural(r.usuarios.desactivados, 'cuenta desactivada', 'cuentas desactivadas')}`}
            />
        </section>
    );
}