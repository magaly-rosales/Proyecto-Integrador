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
        throw new Error("Los datos enviados no son válidos (se esperaban 21 o 42 puntos).");
    }
    throw new Error(`El servidor respondió con un error (código ${respuesta.status}).`);
  }

  return respuesta.json();
}

// Arma el arreglo que espera el backend a partir de las manos detectadas.
// Cada mano llega como { etiqueta: "Left" | "Right", puntos: [21 landmarks] }.
// - 1 mano  -> 21 puntos
// - 2 manos -> 42 puntos: primero los 21 de "Left" y luego los 21 de "Right"
// Este orden debe ser SIEMPRE el mismo al reconocer y al capturar muestras.
// Las coordenadas se envían tal como las da MediaPipe (sin invertir).
function aPuntos(manos) {
  const ordenadas =
    manos.length === 2
      ? [...manos].sort((a, b) => {
          if (a.etiqueta === b.etiqueta) return 0;
          return a.etiqueta === "Left" ? -1 : 1;
        })
      : manos;

  return ordenadas.flatMap((mano) =>
    mano.puntos.map(({ x, y, z }) => ({ x, y, z }))
  );
}

function opcionesPost(manos) {
  return {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(aPuntos(manos)),
  };
}

// POST /api/reconocimiento/puntos -> { sena, distancia }
export function enviarPuntos(manos) {
  return peticion("/api/reconocimiento/puntos", opcionesPost(manos));
}

// POST /api/reconocimiento/muestras/{idSena} -> { status }
export function guardarMuestra(idSena, manos) {
  return peticion(`/api/reconocimiento/muestras/${idSena}`, opcionesPost(manos));
}

// GET /api/reconocimiento/senas-disponibles -> [{ idSena, nombre, ... }]
export function obtenerSenas() {
  return peticion("/api/reconocimiento/senas-disponibles");
}