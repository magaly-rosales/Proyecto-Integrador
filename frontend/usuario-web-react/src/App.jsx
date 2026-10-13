// src/App.jsx
import { Navigate, Route, Routes } from "react-router-dom";
import Sidebar from "./components/Sidebar";
import Header from "./components/Header";
import Reconocer from "./pages/Reconocer";
import Entrenamiento from "./pages/Entrenamiento";
import "./App.css";

// Componentes temporales limpios para las nuevas secciones mientras se desarrollan
const Inicio = () => <div style={{ padding: '20px' }}><h2>Vista de Inicio (Dashboard)</h2><p>Bienvenido al panel principal de SeñasApp.</p></div>;
const Diccionario = () => <div style={{ padding: '20px' }}><h2>Diccionario de Señas</h2><p>Catálogo interactivo de gestos y señas.</p></div>;
const Historial = () => <div style={{ padding: '20px' }}><h2>Historial de Traducciones</h2><p>Registro de señas traducidas recientemente.</p></div>;
const Perfil = () => <div style={{ padding: '20px' }}><h2>Perfil de Usuario</h2><p>Configuración de cuenta y preferencias de Ana Merino.</p></div>;

export default function App() {
  return (
    <div className="app-layout">
      <Sidebar />

      <div className="app-main-content">
        <Header />

        <Routes>
          <Route path="/" element={<Navigate to="/inicio" replace />} />
          <Route path="/inicio" element={<Inicio />} />
          <Route path="/reconocer" element={<Reconocer />} />
          <Route path="/entrenamiento" element={<Entrenamiento />} />
          <Route path="/diccionario" element={<Diccionario />} />
          <Route path="/historial" element={<Historial />} />
          <Route path="/perfil" element={<Perfil />} />
          <Route path="*" element={<Navigate to="/inicio" replace />} />
        </Routes>
      </div>
    </div>
  );
}