import { useEffect, useRef, useState } from "react";

// Hace que su contenido aparezca suavemente al entrar en pantalla (al hacer scroll).
export default function Aparecer({ children, retraso = 0, className = "" }) {
  const ref = useRef(null);
  // Si el navegador no soporta IntersectionObserver, se muestra de una vez
  const [visible, setVisible] = useState(() => typeof IntersectionObserver === "undefined");

  useEffect(() => {
    if (visible || !ref.current) return;
    const observador = new IntersectionObserver(
      ([entrada]) => {
        if (entrada.isIntersecting) {
          setVisible(true);
          observador.disconnect();
        }
      },
      { threshold: 0.15 }
    );
    observador.observe(ref.current);
    return () => observador.disconnect();
  }, [visible]);

  return (
    <div
      ref={ref}
      className={`aparecer ${visible ? "visible" : ""} ${className}`}
      style={{ transitionDelay: `${retraso}ms` }}
    >
      {children}
    </div>
  );
}
