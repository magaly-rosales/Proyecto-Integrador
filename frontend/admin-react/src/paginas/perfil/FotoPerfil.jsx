import { useState } from 'react';
import { mensajeError } from '../../api/errores';
import { quitarFoto, subirFoto } from '../../api/perfilApi';

const IconoCamara = () => (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"
        strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
        <path d="M4 8h3l1.6-2.4h6.8L17 8h3a1 1 0 0 1 1 1v9a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V9a1 1 0 0 1 1-1z" />
        <circle cx="12" cy="13" r="3.5" />
    </svg>
);

// Avatar con los botones para subir, cambiar o quitar la foto; el backend valida y guarda el archivo
export default function FotoPerfil({ perfil, onCambio }) {
    const [enviando, setEnviando] = useState(false);
    const [error, setError] = useState('');

    const elegir = async (e) => {
        const archivo = e.target.files[0];
        e.target.value = '';
        if (!archivo) return;
        setEnviando(true);
        setError('');
        try {
            const { data } = await subirFoto(archivo);
            onCambio(data.data);
        } catch (err) {
            setError(mensajeError(err, 'No se pudo guardar la foto.'));
        } finally {
            setEnviando(false);
        }
    };

    const quitar = async () => {
        setEnviando(true);
        setError('');
        try {
            const { data } = await quitarFoto();
            onCambio(data.data);
        } catch (err) {
            setError(mensajeError(err, 'No se pudo quitar la foto.'));
        } finally {
            setEnviando(false);
        }
    };

    return (
        <div className="perfil-foto">
            <span className="perfil-avatar">
                {perfil.foto
                    ? <img src={perfil.foto} alt="Tu foto de perfil" />
                    : <span aria-hidden="true">{perfil.iniciales}</span>}
            </span>
            <div className="perfil-foto-texto">
                <p className="campo-ayuda">JPG o PNG · máximo 2 MB</p>
                <div className="perfil-foto-botones">
                    <label className={`boton-contorno boton-archivo${enviando ? ' deshabilitado' : ''}`}>
                        <IconoCamara /> {perfil.foto ? 'Cambiar foto' : 'Subir foto'}
                        <input
                            className="solo-lector"
                            type="file"
                            accept=".jpg,.jpeg,.png,image/jpeg,image/png"
                            onChange={elegir}
                            disabled={enviando}
                        />
                    </label>
                    {perfil.foto && (
                        <button type="button" className="boton-estado boton-estado-rojo" onClick={quitar}
                            disabled={enviando}>
                            Quitar foto
                        </button>
                    )}
                </div>
                {error && <p className="campo-error" role="alert">{error}</p>}
            </div>
        </div>
    );
}
