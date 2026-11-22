import { IconoLuna, IconoMenu, IconoSol } from './Iconos';

export default function Cabecera({ seccion, tema, onAlternarTema, onMenu }) {
    const oscuro = tema === 'dark';

    return (
        <header className="cabecera">
            <button type="button" className="cabecera-menu" onClick={onMenu} aria-label="Abrir menú">
                <IconoMenu />
            </button>

            <div className="cabecera-textos">
                <h1>{seccion.titulo}</h1>
                <p>{seccion.subtitulo}</p>
            </div>

            <button
                type="button"
                role="switch"
                aria-checked={oscuro}
                className="tema"
                onClick={onAlternarTema}
                aria-label="Cambiar entre modo claro y oscuro"
            >
                {oscuro ? <IconoLuna tam={16} /> : <IconoSol tam={16} />}
                <span className="tema-texto">{oscuro ? 'Oscuro' : 'Claro'}</span>
                <span className="tema-interruptor" aria-hidden="true"><i /></span>
            </button>
        </header>
    );
}