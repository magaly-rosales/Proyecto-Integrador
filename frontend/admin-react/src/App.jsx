import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import AuthProvider from './auth/AuthProvider';
import RutaProtegida from './auth/RutaProtegida';
import Layout from './layout/Layout';
import EnConstruccion from './paginas/EnConstruccion';
import Senas from './paginas/Senas';
import './estilos/Escala.css';
import Categorias from './paginas/Categorias';

export default function App() {
    return (
        <AuthProvider>
            <BrowserRouter>
                <RutaProtegida>
                    <Routes>
                        <Route element={<Layout />}>
                            <Route index element={<Navigate to="/senas" replace />} />
                            <Route path="senas" element={<Senas />} />
                            <Route path="categorias" element={<Categorias />} />
                            <Route path="usuarios" element={<EnConstruccion nombre="Usuarios" />} />
                            <Route path="estadisticas" element={<EnConstruccion nombre="Estadísticas" />} />
                            <Route path="entrenamiento" element={<EnConstruccion nombre="Entrenamiento" />} />
                            <Route path="perfil" element={<EnConstruccion nombre="Mi perfil" />} />
                            <Route path="*" element={<Navigate to="/senas" replace />} />
                        </Route>
                    </Routes>
                </RutaProtegida>
            </BrowserRouter>
        </AuthProvider>
    );
}