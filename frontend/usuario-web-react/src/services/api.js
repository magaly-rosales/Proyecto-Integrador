// Servicio que concentra todas las llamadas al backend (Spring Boot).
// La URL base viene del archivo .env (variable VITE_API_URL).
const API_URL = import.meta.env.VITE_API_URL;

// Función auxiliar: hace la petición y convierte errores en mensajes claros.
async function peticion(ruta, opciones = {}) {
  let respuesta;
  try {
    respuesta = await fetch(`${API_URL}${ruta}`, opciones);
  } catch {
    // fetch solo falla aquí cuando no hay conexión con el backend
    throw new Error(
      "No se pudo conectar con el servidor. Verifica que el backend esté encendido."
    );
  }

  if (!respuesta.ok) {
    if (respuesta.status === 400) {
      throw new Error("Los datos enviados no son válidos (se esperaban 21 puntos).");
    }
    throw new Error(`El servidor respondió con un error (código ${respuesta.status}).`);
  }

  return respuesta.json();
}

// Convierte los landmarks de MediaPipe a {x, y, z}, sin invertirlos ni modificarlos.
function aPuntos(landmarks) {
  return landmarks.map(({ x, y, z }) => ({ x, y, z }));
}

function opcionesPost(landmarks) {
  return {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(aPuntos(landmarks)),
  };
}

// POST /api/reconocimiento/puntos -> { sena, distancia }
export function enviarPuntos(landmarks) {
  return peticion("/api/reconocimiento/puntos", opcionesPost(landmarks));
}

// POST /api/reconocimiento/muestras/{idSena} -> { status }
export function guardarMuestra(idSena, landmarks) {
  return peticion(
    `/api/reconocimiento/muestras/${idSena}`,
    opcionesPost(landmarks)
  );
}

// GET /api/reconocimiento/senas-disponibles -> [{ idSena, nombre, ... }]
export function obtenerSenas() {
  return peticion("/api/reconocimiento/senas-disponibles");
}