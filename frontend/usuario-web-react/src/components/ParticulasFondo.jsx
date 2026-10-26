import { useEffect, useRef } from "react";

// Puntos suaves que flotan despacio detrás del contenido (efecto de fondo).
// No se anima si el usuario pidió menos movimiento.
export default function ParticulasFondo({ cantidad = 38 }) {
  const lienzo = useRef(null);

  useEffect(() => {
    const canvas = lienzo.current;
    if (!canvas) return;
    if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) return;

    const ctx = canvas.getContext("2d");
    let ancho = 0;
    let alto = 0;
    let animacion;

    const ajustar = () => {
      ancho = canvas.clientWidth;
      alto = canvas.clientHeight;
      canvas.width = ancho;
      canvas.height = alto;
    };
    ajustar();
    window.addEventListener("resize", ajustar);

    const puntos = Array.from({ length: cantidad }, () => ({
      x: Math.random() * ancho,
      y: Math.random() * alto,
      r: 1.5 + Math.random() * 2.5,
      vx: (Math.random() - 0.5) * 0.25,
      vy: -0.1 - Math.random() * 0.25,
      color: Math.random() < 0.3 ? "242,140,40" : "61,220,151",
      alfa: 0.25 + Math.random() * 0.35,
    }));

    function dibujar() {
      ctx.clearRect(0, 0, ancho, alto);
      for (const p of puntos) {
        p.x += p.vx;
        p.y += p.vy;
        if (p.y < -10) p.y = alto + 10;
        if (p.x < -10) p.x = ancho + 10;
        if (p.x > ancho + 10) p.x = -10;
        ctx.beginPath();
        ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2);
        ctx.fillStyle = `rgba(${p.color},${p.alfa})`;
        ctx.fill();
      }
      animacion = requestAnimationFrame(dibujar);
    }
    dibujar();

    return () => {
      cancelAnimationFrame(animacion);
      window.removeEventListener("resize", ajustar);
    };
  }, [cantidad]);

  return <canvas ref={lienzo} className="sa-particulas" aria-hidden="true" />;
}
