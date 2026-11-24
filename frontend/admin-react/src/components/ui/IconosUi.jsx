// Íconos de uso general en las pantallas (heredan el color del texto)
function Icono({ children, tam = 18 }) {
    return (
        <svg width={tam} height={tam} viewBox="0 0 24 24" fill="none" stroke="currentColor"
            strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
            {children}
        </svg>
    );
}

export const IconoBuscar = (p) => (
    <Icono {...p}><circle cx="11" cy="11" r="7" /><path d="M20 20l-3.5-3.5" /></Icono>
);
export const IconoPlay = (p) => (
    <Icono {...p}><path d="M7 5l12 7-12 7z" fill="currentColor" /></Icono>
);
export const IconoPuntos = (p) => (
    <Icono {...p}>
        <circle cx="12" cy="5" r="1.4" fill="currentColor" /><circle cx="12" cy="12" r="1.4" fill="currentColor" />
        <circle cx="12" cy="19" r="1.4" fill="currentColor" />
    </Icono>
);
export const IconoImagen = (p) => (
    <Icono {...p}>
        <rect x="3" y="4" width="18" height="16" rx="2" /><circle cx="9" cy="10" r="1.8" />
        <path d="M21 16l-5-5-9 9" />
    </Icono>
);
export const IconoPapelera = (p) => (
    <Icono {...p}>
        <path d="M4 7h16M10 11v6M14 11v6M6 7l1 13h10l1-13M9 7V4h6v3" />
    </Icono>
);
export const IconoCerrar = (p) => (
    <Icono {...p}><path d="M6 6l12 12M18 6L6 18" /></Icono>
);
export const IconoAlerta = (p) => (
    <Icono {...p}><path d="M12 3l10 18H2zM12 10v5M12 18v.5" /></Icono>
);
export const IconoMas = (p) => (
    <Icono {...p}><path d="M12 5v14M5 12h14" /></Icono>
);
export const IconoEditar = (p) => (
    <Icono {...p}><path d="M4 20h4L19 9l-4-4L4 16zM13.5 6.5l4 4" /></Icono>
);
export const IconoSubir = (p) => (
    <Icono {...p}><path d="M12 16V4M7 9l5-5 5 5M4 20h16" /></Icono>
);

export const IconoDerecha = (p) => (
    <Icono {...p}><path d="M9 6l6 6-6 6" /></Icono>
);