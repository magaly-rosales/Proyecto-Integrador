// Iconos de línea (estilo Feather) como componentes pequeños.
function Icono({ children, tamano = 22 }) {
  return (
    <svg
      width={tamano}
      height={tamano}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      {children}
    </svg>
  );
}

export const IconoTexto = (p) => (
  <Icono {...p}>
    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
  </Icono>
);

export const IconoVoz = (p) => (
  <Icono {...p}>
    <path d="M4 10v4h4l5 4V6L8 10H4zM16.5 8.5a5 5 0 0 1 0 7" />
  </Icono>
);

export const IconoAmbos = (p) => (
  <Icono {...p}>
    <path d="M3 4h12a2 2 0 0 1 2 2v6a2 2 0 0 1-2 2H8l-4 3V6a2 2 0 0 1 2-2zM20 8a4 4 0 0 1 0 6" />
  </Icono>
);

export const IconoCamara = (p) => (
  <Icono {...p}>
    <path d="M4 8h3l2-3h6l2 3h3v11H4z" />
    <circle cx="12" cy="13" r="3.5" />
  </Icono>
);

export const IconoLibro = (p) => (
  <Icono {...p}>
    <path d="M3 5h7a2 2 0 0 1 2 2v13a2 2 0 0 0-2-2H3zM21 5h-7a2 2 0 0 0-2 2v13a2 2 0 0 1 2-2h7z" />
  </Icono>
);

export const IconoAjustes = (p) => (
  <Icono {...p}>
    <circle cx="12" cy="12" r="3" />
    <path d="M12 2v3M12 19v3M2 12h3M19 12h3M5 5l2 2M17 17l2 2M19 5l-2 2M7 17l-2 2" />
  </Icono>
);

export const IconoCheck = (p) => (
  <Icono {...p}>
    <path d="M5 12l5 5L20 7" />
  </Icono>
);

export const IconoInicio = (p) => (
  <Icono {...p}>
    <path d="M3 11l9-7 9 7v9a1 1 0 0 1-1 1h-5v-6h-6v6H4a1 1 0 0 1-1-1z" />
  </Icono>
);

export const IconoTraducir = (p) => (
  <Icono {...p}>
    <path d="M4 8V5a1 1 0 0 1 1-1h3M16 4h3a1 1 0 0 1 1 1v3M20 16v3a1 1 0 0 1-1 1h-3M8 20H5a1 1 0 0 1-1-1v-3" />
    <circle cx="12" cy="12" r="3" />
  </Icono>
);

export const IconoCampana = (p) => (
  <Icono {...p}>
    <path d="M6 9a6 6 0 0 1 12 0c0 6 2 7 2 7H4s2-1 2-7zM10 20a2 2 0 0 0 4 0" />
  </Icono>
);

export const IconoPerfil = (p) => (
  <Icono {...p}>
    <circle cx="12" cy="8" r="4" />
    <path d="M4 21a8 8 0 0 1 16 0" />
  </Icono>
);

export const IconoLuna = (p) => (
  <Icono {...p}>
    <path d="M21 13A9 9 0 1 1 11 3a7 7 0 0 0 10 10z" />
  </Icono>
);

export const IconoSalir = (p) => (
  <Icono {...p}>
    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9" />
  </Icono>
);

export const IconoChevron = (p) => (
  <Icono {...p}>
    <path d="M6 9l6 6 6-6" />
  </Icono>
);

export const IconoFlecha = (p) => (
  <Icono {...p}>
    <path d="M5 12h14M13 6l6 6-6 6" />
  </Icono>
);

export const IconoMano = (p) => (
  <Icono {...p}>
    <path d="M7 11V5.5a1.5 1.5 0 0 1 3 0V10M10 9.5V4a1.5 1.5 0 0 1 3 0v6M13 10V5.5a1.5 1.5 0 0 1 3 0V12M16 11.5a1.5 1.5 0 0 1 3 0V15a7 7 0 0 1-7 7h-1a6 6 0 0 1-5-3l-2.5-4.5a1.5 1.5 0 0 1 2.5-1.5L7 14" />
  </Icono>
);

export const IconoPlay = (p) => (
  <Icono {...p}>
    <path d="M8 5v14l11-7z" fill="currentColor" />
  </Icono>
);
