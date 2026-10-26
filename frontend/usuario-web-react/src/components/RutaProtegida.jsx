import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/useAuth";

// Si no hay sesión, manda a /login y recuerda a dónde quería ir.
export default function RutaProtegida({ children }) {
  const { usuario } = useAuth();
  const ubicacion = useLocation();
  if (!usuario) {
    return <Navigate to="/login" replace state={{ desde: ubicacion.pathname }} />;
  }
  return children;
}
