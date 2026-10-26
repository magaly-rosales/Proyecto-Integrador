// Preferencias del usuario guardadas en el navegador (localStorage).
const CLAVE = "senasapp_preferencias";

export const POR_DEFECTO = {
  modo: "texto", // texto | voz | ambos
  tema: "claro", // claro | oscuro | auto
  tamano: "medio", // pequeno | medio | grande
  notificaciones: true,
  camara: true,
};

export function leerPreferencias() {
  try {
    return { ...POR_DEFECTO, ...JSON.parse(localStorage.getItem(CLAVE)) };
  } catch {
    return { ...POR_DEFECTO };
  }
}

export function guardarPreferencias(prefs) {
  try {
    localStorage.setItem(CLAVE, JSON.stringify(prefs));
  } catch {
    /* si no se puede guardar, las preferencias duran hasta cerrar la pestaña */
  }
}
