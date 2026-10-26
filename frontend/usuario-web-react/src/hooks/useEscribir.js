import { useEffect, useState } from "react";

// Efecto "máquina de escribir": devuelve el texto letra por letra.
// Si el usuario prefiere menos movimiento, devuelve el texto completo.
export default function useEscribir(texto, velocidad = 70) {
  const [avance, setAvance] = useState({ origen: "", n: 0 });
  const completo = texto ?? "";
  const sinMovimiento =
    typeof window !== "undefined" &&
    window.matchMedia("(prefers-reduced-motion: reduce)").matches;

  useEffect(() => {
    if (sinMovimiento || !completo) return;
    const id = setInterval(() => {
      setAvance((previo) => {
        const n = previo.origen === completo ? previo.n + 1 : 1;
        if (n >= completo.length) clearInterval(id);
        return { origen: completo, n: Math.min(n, completo.length) };
      });
    }, velocidad);
    return () => clearInterval(id);
  }, [completo, velocidad, sinMovimiento]);

  if (sinMovimiento) return completo;
  const n = avance.origen === completo ? avance.n : 0;
  return completo.slice(0, n);
}
