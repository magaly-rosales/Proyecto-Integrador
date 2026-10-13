// src/App.jsx
import { NavLink, Navigate, Route, Routes } from "react-router-dom";
import Sidebar from "./components/Sidebar";
import Header from "./components/Header";
import Reconocer from "./pages/Reconocer";
import Entrenamiento from "./pages/Entrenamiento";
import "./App.css";

export default function App() {
  return (
    <div className="app-layout">
      {/* Columna Izquierda: Menú de Navegación Fijo de la App */}
      <Sidebar />

      {/* Columna Derecha: Cabecera superior y Sistema de Rutas Dinámicas */}
      <div className="app-main-content">
        <Header />

        <Routes>
          <Route path="/" element={<Navigate to="/reconocer" replace />} />
          <Route path="/reconocer" element={<Reconocer />} />
          <Route path="/entrenamiento" element={<Entrenamiento />} />
          <Route path="*" element={<Navigate to="/reconocer" replace />} />
        </Routes>
      </div>
    </div>
  );
}