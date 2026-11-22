// Íconos del panel (trazo simple, heredan el color del texto)
function Icono({ children, tam = 20 }) {
    return (
        <svg width={tam} height={tam} viewBox="0 0 24 24" fill="none" stroke="currentColor"
            strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
            {children}
        </svg>
    );
}

export const IconoMano = (p) => (
    <Icono {...p}>
        <path d="M18 11V6a2 2 0 0 0-4 0M14 10V4a2 2 0 0 0-4 0v2M10 10.5V6a2 2 0 0 0-4 0v8" />
        <path d="M18 8a2 2 0 1 1 4 0v6a8 8 0 0 1-8 8h-2c-2.8 0-4.5-.9-5.9-2.4L3.4 16a2 2 0 0 1 3.2-2.4L8 15" />
    </Icono>
);
export const IconoCategorias = (p) => (
    <Icono {...p}>
        <rect x="3" y="3" width="7" height="7" rx="1.5" /><rect x="14" y="3" width="7" height="7" rx="1.5" />
        <rect x="3" y="14" width="7" height="7" rx="1.5" /><rect x="14" y="14" width="7" height="7" rx="1.5" />
    </Icono>
);
export const IconoUsuarios = (p) => (
    <Icono {...p}>
        <circle cx="9" cy="8" r="3.5" /><path d="M2.5 20c0-3.6 2.9-6 6.5-6s6.5 2.4 6.5 6" />
        <path d="M16 4.6a3.5 3.5 0 0 1 0 6.8M18 14.3c2.2.7 3.5 2.6 3.5 5.7" />
    </Icono>
);
export const IconoGrafico = (p) => (
    <Icono {...p}><path d="M5 20V12M12 20V5M19 20v-9" /></Icono>
);
export const IconoObjetivo = (p) => (
    <Icono {...p}><circle cx="12" cy="12" r="9" /><circle cx="12" cy="12" r="5" /><circle cx="12" cy="12" r="1.5" /></Icono>
);
export const IconoSalir = (p) => (
    <Icono {...p}><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9" /></Icono>
);
export const IconoSol = (p) => (
    <Icono {...p}>
        <circle cx="12" cy="12" r="4" />
        <path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4" />
    </Icono>
);
export const IconoLuna = (p) => (
    <Icono {...p}><path d="M21 12.8A9 9 0 1 1 11.2 3a7 7 0 0 0 9.8 9.8z" /></Icono>
);
export const IconoMenu = (p) => (
    <Icono {...p}><path d="M4 6h16M4 12h16M4 18h16" /></Icono>
);

// Se elige por nombre desde secciones.js
export const ICONOS = {
    mano: IconoMano,
    categorias: IconoCategorias,
    usuarios: IconoUsuarios,
    grafico: IconoGrafico,
    objetivo: IconoObjetivo,
};