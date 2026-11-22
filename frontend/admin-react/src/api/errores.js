// Convierte cualquier error de axios en un texto para mostrar al usuario
export function mensajeError(err, porDefecto = 'No se pudo completar la acción.') {
    if (!err.response) return 'No se pudo conectar con el servidor.';

    const datos = err.response.data;
    if (datos?.error) return datos.error;
    if (datos?.detail) return datos.detail;
    if (datos?.errores) {
        const primero = Object.values(datos.errores).flat()[0];
        if (primero) return String(primero);
    }
    return porDefecto;
}