// src/App.jsx
import Sidebar from "./components/Sidebar";
import Header from "./components/Header";
import Reconocer from "./pages/Reconocer";
import "./App.css";

export default function App() {
  return (
    <div className="app-layout">
      {/* Columna Izquierda: Menú de Navegación Fijo */}
      <Sidebar />

      {/* Columna Derecha: Cabecera y Contenido Dinámico */}
      <div className="app-main-content">
        <Header />
        <Reconocer />
      </div>
    </div>
  );
}