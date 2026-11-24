import { useId, useState } from 'react';

// Campo con su etiqueta, una ayuda opcional y el error que manda el backend
export default function CampoPerfil({ etiqueta, error, ayuda, ...entrada }) {
    const id = useId();
    return (
        <div className="perfil-campo">
            <label htmlFor={id}>{etiqueta}</label>
            <input id={id} aria-invalid={Boolean(error)} {...entrada} />
            {ayuda && <p className="campo-ayuda">{ayuda}</p>}
            {error && <p className="campo-error" role="alert">{error}</p>}
        </div>
    );
}

const Ojo = ({ tachado }) => (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"
        strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
        <path d="M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7S2 12 2 12z" />
        <circle cx="12" cy="12" r="3" />
        {tachado && <path d="M4 4l16 16" />}
    </svg>
);

// Campo de contraseña con el ojo para mostrar u ocultar lo escrito
export function CampoClave({ etiqueta, error, children, ...entrada }) {
    const id = useId();
    const [visible, setVisible] = useState(false);
    return (
        <div className="perfil-campo">
            <label htmlFor={id}>{etiqueta}</label>
            <div className="perfil-clave">
                <input id={id} type={visible ? 'text' : 'password'} aria-invalid={Boolean(error)} {...entrada} />
                <button
                    type="button"
                    onClick={() => setVisible((v) => !v)}
                    aria-label={visible ? 'Ocultar contraseña' : 'Mostrar contraseña'}
                    aria-pressed={visible}
                >
                    <Ojo tachado={visible} />
                </button>
            </div>
            {children}
            {error && <p className="campo-error" role="alert">{error}</p>}
        </div>
    );
}

// Lee el error de la API: { campo, texto }
export function errorDeApi(err, mensajeError, porDefecto) {
    return { campo: err.response?.data?.campo || '', texto: mensajeError(err, porDefecto) };
}
