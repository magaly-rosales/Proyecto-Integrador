import { Navigate, Route, Routes, useLocation } from "react-router-dom";
import Acceso from "./auth/Acceso";
import Footer from "./components/Footer";
import Navbar from "./components/Navbar";
import RutaProtegida from "./components/RutaProtegida";
import { useAuth } from "./context/useAuth";
import Configuracion from "./pages/Configuracion";

import Entrenamiento from "./pages/Entrenamiento";
import Inicio from "./pages/Inicio";
import Traducir from "./pages/Traducir";
import "./App.css";
import Perfil from "./pages/Perfil";
export default function App() {
  const { usuario } = useAuth();
  const { pathname } = useLocation();

  // Sin sesión, la raíz muestra solo el acceso (sin menú ni pie de página)
  if (!usuario && pathname === "/") return <Acceso />;

  return (
    <div className="sa-app">
      <Navbar />

      <main className="sa-contenido">
        <Routes>
          {/* Con sesión, la raíz lleva a traducir */}
          <Route path="/" element={<Navigate to="/traducir" replace />} />
          <Route path="/inicio" element={<Inicio />} />

          {/* Rutas antiguas: ahora todo está en la pantalla de acceso */}
          <Route path="/login" element={<Navigate to="/" replace />} />
          <Route path="/registro" element={<Navigate to="/?registro" replace />} />

          {/* Solo con sesión */}
          <Route path="/traducir" element={<RutaProtegida><Traducir /></RutaProtegida>} />
          <Route path="/configuracion" element={<RutaProtegida><Configuracion /></RutaProtegida>} />
          <Route path="/perfil" element={<RutaProtegida><Perfil /></RutaProtegida>} />
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