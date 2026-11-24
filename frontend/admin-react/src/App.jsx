import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import AuthProvider from './auth/AuthProvider';
import RutaProtegida from './auth/RutaProtegida';
import Layout from './layout/Layout';
import Senas from './paginas/Senas';
import './estilos/Escala.css';
import Categorias from './paginas/Categorias';
import Usuarios from './paginas/Usuarios';
import Estadisticas from './paginas/Estadisticas';
import Entrenamiento from './paginas/Entrenamiento';
import Perfil from './paginas/Perfil';
import Asistente from './paginas/Asistente';

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
                            <Route path="usuarios" element={<Usuarios />} />
                            <Route path="estadisticas" element={<Estadisticas />} />
                            <Route path="entrenamiento" element={<Entrenamiento />} />
                            <Route path="asistente" element={<Asistente />} />
                            <Route path="perfil" element={<Perfil />} />
                            <Route path="*" element={<Navigate to="/senas" replace />} />
                        </Route>
                    </Routes>
                </RutaProtegida>
            </BrowserRouter>
        </AuthProvider>
    );
}