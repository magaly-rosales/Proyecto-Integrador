import cliente from './cliente';

// Envía la conversación [{ rol: 'usuario' | 'asistente', texto }]; el backend consulta los datos y responde la IA
export const preguntarAsistente = (mensajes) => cliente.post('/asistente/', { mensajes });

// El administrador confirmó activar o desactivar una cuenta que propuso la IA
export const confirmarAccion = (idUsuario, activar) =>
    cliente.post('/asistente/accion/', { id_usuario: idUsuario, activar });
