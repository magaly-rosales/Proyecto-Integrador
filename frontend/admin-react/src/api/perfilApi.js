import cliente from './cliente';

// Datos del administrador con sesión: { nombre, email, rol, iniciales, foto }
export const obtenerPerfil = () => cliente.get('/perfil/');

// Agrega o cambia la foto de perfil (archivo JPG o PNG)
export const subirFoto = (archivo) => {
    const datos = new FormData();
    datos.append('foto', archivo);
    return cliente.post('/perfil/foto/', datos);
};

// Quita la foto de perfil
export const quitarFoto = () => cliente.delete('/perfil/foto/');

// Cambia la contraseña: { actual, nueva, confirmar }; el backend valida y guarda
export const cambiarPassword = (datos) => cliente.post('/perfil/password/', datos);
