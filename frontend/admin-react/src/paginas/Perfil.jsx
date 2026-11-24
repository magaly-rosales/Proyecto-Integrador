import { useCallback, useEffect, useState } from 'react';
import { useAuth } from '../auth/AuthContext';
import { mensajeError } from '../api/errores';
import { obtenerPerfil } from '../api/perfilApi';
import FormularioDatos from './perfil/FormularioDatos';
import FormularioPassword from './perfil/FormularioPassword';
import '../estilos/Comun.css';
import '../estilos/Senas.css';
import '../estilos/Formulario.css';
import '../estilos/Perfil.css';

export default function Perfil() {
    const { actualizarUsuario } = useAuth();
    const [perfil, setPerfil] = useState(null);
    const [error, setError] = useState('');

    // Guarda el perfil y deja actualizados el nombre y la foto del menú lateral
    const recibir = useCallback((datos) => {
        setPerfil(datos);
        actualizarUsuario({ nombre: datos.nombre, email: datos.email, foto: datos.foto });
    }, [actualizarUsuario]);

    const cargar = useCallback(() => {
        setError('');
        obtenerPerfil()
            .then(({ data }) => recibir(data))
            .catch((err) => setError(mensajeError(err, 'No se pudieron cargar tus datos.')));
    }, [recibir]);

    useEffect(() => {
        cargar();
    }, [cargar]);

    if (error) {
        return (
            <div className="tabla-aviso">
                <strong>No se pudo cargar el perfil</strong>
                <p>{error}</p>
                <button type="button" className="boton-contorno" onClick={cargar}>Reintentar</button>
            </div>
        );
    }

    if (!perfil) {
        return <div className="tabla-carga" role="status" aria-label="Cargando perfil"><span /><span /></div>;
    }

    return (
        <div className="perfil-grid">
            <FormularioDatos perfil={perfil} onGuardado={recibir} />
            <FormularioPassword />
        </div>
    );
}
