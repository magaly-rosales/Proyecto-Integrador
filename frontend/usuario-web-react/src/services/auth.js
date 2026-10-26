// Llamadas de registro e inicio de sesión al backend (Spring Boot).
const API_URL = import.meta.env.VITE_API_URL;

async function enviar(ruta, datos) {
  let respuesta;
  try {
    respuesta = await fetch(`${API_URL}${ruta}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(datos),
    });
  } catch {
    throw new Error("No se pudo conectar con el servidor. Verifica que el backend esté encendido.");
  }
  const cuerpo = await respuesta.json().catch(() => ({}));
  if (!respuesta.ok) {
    // el backend responde { "error": "mensaje" } en 400, 401 y 409
    throw new Error(cuerpo.error || `Error del servidor (código ${respuesta.status}).`);
  }
  return cuerpo; // { idUsuario, nombre, email, rol }
}

export const registrarUsuario = (datos) => enviar("/api/auth/registro", datos);
export const iniciarSesion = (datos) => enviar("/api/auth/login", datos);
