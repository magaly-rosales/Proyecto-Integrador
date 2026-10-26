import { useContext } from "react";
import { PreferenciasContext } from "./PreferenciasContext";

export function usePreferencias() {
  return useContext(PreferenciasContext);
}
