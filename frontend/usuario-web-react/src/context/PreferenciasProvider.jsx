import { useEffect, useState, useSyncExternalStore } from "react";
import { PreferenciasContext } from "./PreferenciasContext";
import { guardarPreferencias, leerPreferencias } from "../services/preferencias";

// Escucha si el sistema operativo está en modo oscuro (para el tema "auto").
function suscribir(aviso) {
  const consulta = window.matchMedia("(prefers-color-scheme: dark)");
  consulta.addEventListener("change", aviso);
  return () => consulta.removeEventListener("change", aviso);
}
const sistemaOscuro = () => window.matchMedia("(prefers-color-scheme: dark)").matches;

export default function PreferenciasProvider({ children }) {
  const [prefs, setPrefs] = useState(leerPreferencias);
  const oscuroSistema = useSyncExternalStore(suscribir, sistemaOscuro, () => false);

  const temaFinal = prefs.tema === "auto" ? (oscuroSistema ? "oscuro" : "claro") : prefs.tema;

  // Aplica tema y tamaño de texto a toda la página
  useEffect(() => {
    document.documentElement.dataset.tema = temaFinal;
    document.documentElement.dataset.tamano = prefs.tamano;
  }, [temaFinal, prefs.tamano]);

  // Cambia una o varias preferencias y las guarda
  const cambiar = (parcial) => {
    setPrefs((anterior) => {
      const nuevo = { ...anterior, ...parcial };
      guardarPreferencias(nuevo);
      return nuevo;
    });
  };

  return (
    <PreferenciasContext.Provider value={{ prefs, cambiar, temaFinal }}>
      {children}
    </PreferenciasContext.Provider>
  );
}
