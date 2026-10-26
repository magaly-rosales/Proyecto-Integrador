import { Navigate, Route, Routes } from "react-router-dom";
import Footer from "./components/Footer";
import Navbar from "./components/Navbar";
import RutaProtegida from "./components/RutaProtegida";
import { useAuth } from "./context/useAuth";
import Configuracion from "./pages/Configuracion";
import Entrenamiento from "./pages/Entrenamiento";
import Inicio from "./pages/Inicio";
import Login from "./pages/Login";
import Registro from "./pages/Registro";
import Traducir from "./pages/Traducir";
import "./App.css";

export default function App() {
  const { usuario } = useAuth();

  return (
    <div className="sa-app">
      <Navbar />

      <main className="sa-contenido">
        <Routes>
          {/* Públicas */}
          <Route path="/" element={<Inicio />} />
          <Route path="/login" element={usuario ? <Navigate to="/traducir" replace /> : <Login />} />
          <Route path="/registro" element={usuario ? <Navigate to="/traducir" replace /> : <Registro />} />

          {/* Solo con sesión */}
          <Route path="/traducir" element={<RutaProtegida><Traducir /></RutaProtegida>} />
          <Route path="/configuracion" element={<RutaProtegida><Configuracion /></RutaProtegida>} />
          {/* Temporal: sin enlace en el menú. Pasará al panel de administrador. */}
          <Route path="/entrenamiento" element={<RutaProtegida><Entrenamiento /></RutaProtegida>} />

          <Route path="/diccionario" element={<Navigate to="/traducir" replace />} />
          <Route path="/reconocer" element={<Navigate to="/traducir" replace />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>

      <Footer />
    </div>
  );
}
