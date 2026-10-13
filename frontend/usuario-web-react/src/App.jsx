import { NavLink, Navigate, Route, Routes } from "react-router-dom";
import Reconocer from "./pages/Reconocer";
import Entrenamiento from "./pages/Entrenamiento";
import "./App.css";

// Clase del enlace del menú según si es la ruta activa
const claseEnlace = ({ isActive }) => (isActive ? "enlace activo" : "enlace");

export default function App() {
  return (
    <main>
      <h1>SeñasApp</h1>

      <nav className="menu">
        <NavLink to="/reconocer" className={claseEnlace}>
          Reconocer
        </NavLink>
        <NavLink to="/entrenamiento" className={claseEnlace}>
          Entrenamiento
        </NavLink>
      </nav>

      <Routes>
        <Route path="/" element={<Navigate to="/reconocer" replace />} />
        <Route path="/reconocer" element={<Reconocer />} />
        <Route path="/entrenamiento" element={<Entrenamiento />} />
        {/* Cualquier otra ruta vuelve a la pantalla principal.
            Aquí se agregarán /login y /registro más adelante. */}
        <Route path="*" element={<Navigate to="/reconocer" replace />} />
      </Routes>
    </main>
  );
}