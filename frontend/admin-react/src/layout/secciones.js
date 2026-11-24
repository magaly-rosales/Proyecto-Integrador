// Secciones del panel: ruta, textos de la cabecera y contador del menú lateral
export const SECCIONES = [
    {
        ruta: '/senas',
        etiqueta: 'Señas',
        titulo: 'Señas',
        subtitulo: 'Agrega, edita y elimina las señas que ven la web y la app móvil.',
        icono: 'mano',
        contador: (r) => r.senas.total,
    },
    {
        ruta: '/categorias',
        etiqueta: 'Categorías',
        titulo: 'Categorías',
                subtitulo: 'Ordena las señas por tema, con su imagen y su color.',
        icono: 'categorias',
        contador: (r) => r.categorias.total,
    },
    {
        ruta: '/usuarios',
        etiqueta: 'Usuarios',
        titulo: 'Usuarios',
        subtitulo: 'Revisa los usuarios de la web y la app, y activa o desactiva sus cuentas.',
        icono: 'usuarios',
        contador: (r) => r.usuarios.total,
    },
    {
        ruta: '/estadisticas',
        etiqueta: 'Estadísticas',
        titulo: 'Estadísticas de uso',
        subtitulo: 'Qué señas se traducen más y cómo usan la app.',
        icono: 'grafico',
    },
    {
        ruta: '/entrenamiento',
        etiqueta: 'Entrenamiento',
        titulo: 'Entrenamiento',
        subtitulo: 'Captura ejemplos para que el modelo reconozca cada seña.',
        icono: 'objetivo',
    },
    {
        ruta: '/asistente',
        etiqueta: 'Asistente',
        titulo: 'Asistente',
        subtitulo: 'Consulta usuarios, categorías, señas y estadísticas con ayuda de la IA.',
        icono: 'chat',
    },
];

export const PERFIL = {
    ruta: '/perfil',
    titulo: 'Mi perfil',
    subtitulo: 'Tu foto, tus datos y tu contraseña de administrador.',
};