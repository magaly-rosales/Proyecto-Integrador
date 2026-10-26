// Texto a voz con la API nativa del navegador (no necesita librerías ni backend).
// Nota: Chrome solo permite hablar después de que el usuario hizo clic en la página.
// alCambiar(true) al empezar a hablar y alCambiar(false) al terminar (opcional).
export function hablar(texto, alCambiar) {
  if (!texto || !("speechSynthesis" in window)) return;
  window.speechSynthesis.cancel(); // corta lo que se estaba diciendo
  const mensaje = new SpeechSynthesisUtterance(texto);
  mensaje.lang = "es-ES";
  if (alCambiar) {
    mensaje.onstart = () => alCambiar(true);
    mensaje.onend = () => alCambiar(false);
    mensaje.onerror = () => alCambiar(false);
  }
  window.speechSynthesis.speak(mensaje);
}
