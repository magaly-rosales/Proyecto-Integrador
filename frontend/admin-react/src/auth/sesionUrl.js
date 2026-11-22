import { sesion } from '../api/cliente';

// Lee la sesión que envía la pantalla de acceso: /#token=...&nombre=...&email=...
// Se guarda y se borra de la barra de direcciones.
export function tomarSesionDeLaUrl() {
    const datos = new URLSearchParams(window.location.hash.replace(/^#/, ''));
    const token = datos.get('token');
    if (!token) return;

    sesion.guardar(token, {
        nombre: datos.get('nombre') || 'Administrador',
        email: datos.get('email') || '',
    });
    window.history.replaceState(null, '', window.location.pathname + window.location.search);
}