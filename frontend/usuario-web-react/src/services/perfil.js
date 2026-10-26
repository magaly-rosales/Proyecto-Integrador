const API_URL = import.meta.env.VITE_API_URL;

async function enviar(ruta, datos) {
  let respuesta;
  try {
    respuesta = await fetch(`${API_URL}${ruta}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(datos),
    });
  } catch {
    throw new Error("No se pudo conectar con el servidor. Verifica que el backend esté encendido.");
  }
  const cuerpo = await respuesta.json().catch(() => ({}));
  if (!respuesta.ok) {
    throw new Error(cuerpo.error || `Error del servidor (código ${respuesta.status}).`);
  }
  return cuerpo;
}

export const guardarDatos = (id, datos) => enviar(`/api/perfil/${id}/datos`, datos);
export const cambiarPassword = (id, datos) => enviar(`/api/perfil/${id}/password`, datos);
export const guardarFoto = (id, foto) => enviar(`/api/perfil/${id}/foto`, { foto });