import { useEffect, useState } from "react";

// Devuelve un elemento de la lista y pasa al siguiente cada cierto tiempo.
export default function useRotar(lista, cadaMs = 3200) {
  const [indice, setIndice] = useState(0);

  useEffect(() => {
    const id = setInterval(() => setIndice((i) => (i + 1) % lista.length), cadaMs);
    return () => clearInterval(id);
  }, [lista.length, cadaMs]);

  return lista[indice];
}
