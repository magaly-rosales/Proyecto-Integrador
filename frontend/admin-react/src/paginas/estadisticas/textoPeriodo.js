// Texto que describe el periodo consultado (las fechas las devuelve el backend)
export function textoPeriodo(filtro, rango) {
    if (filtro.dias === '7') return 'últimos 7 días';
    if (filtro.dias === '30') return 'últimos 30 días';
    if (filtro.dias === 'todo') return 'desde el inicio';
    const f = (iso) => (iso ? iso.split('-').reverse().join('/') : '…');
    return `del ${f(rango?.desde)} al ${f(rango?.hasta)}`;
}
