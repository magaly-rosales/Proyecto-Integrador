import AuthProvider from './auth/AuthProvider';
import { useAuth } from './auth/AuthContext';
import RutaProtegida from './auth/RutaProtegida';
import { GestionSenas } from './components/GestionSenas';

// Por ahora el panel sigue siendo la pantalla de Señas; el layout nuevo viene en el paso 6b
function Panel() {
    const { cerrarSesion } = useAuth();
    return <GestionSenas onLogout={cerrarSesion} />;
}

export default function App() {
    return (
        <AuthProvider>
            <RutaProtegida>
                <Panel />
            </RutaProtegida>
        </AuthProvider>
    );
}