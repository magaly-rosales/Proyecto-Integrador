import { useState } from 'react';
import { mensajeError } from '../../api/errores';
import { cambiarEstadoUsuario } from '../../api/usuariosApi';
import Modal from '../../components/ui/Modal';

// Confirma activar o desactivar la cuenta de un usuario
export default function ConfirmarEstado({ usuario, onCerrar, onCambiado }) {
    const [enviando, setEnviando] = useState(false);
    const [error, setError] = useState('');
    const activar = !usuario.is_active;

    const confirmar = async () => {
        setEnviando(true);
        setError('');
        try {
            await cambiarEstadoUsuario(usuario.id, activar);
            onCambiado();
        } catch (err) {
            setError(mensajeError(err, 'No se pudo cambiar el estado de la cuenta.'));
            setEnviando(false);
        }
    };

    return (
        <Modal
            titulo={activar ? `¿Activar la cuenta de ${usuario.nombre}?` : `¿Desactivar la cuenta de ${usuario.nombre}?`}
            onCerrar={onCerrar}
            ancho={440}
        >
            <p className="modal-texto">
                {activar
                    ? 'Podrá volver a iniciar sesión en la web y en la app móvil.'
                    : 'No podrá iniciar sesión en la web ni en la app móvil hasta que la actives de nuevo. Sus datos no se borran.'}
            </p>
            {error && <p className="modal-error" role="alert">{error}</p>}
            <div className="modal-acciones">
                <button type="button" className="boton-contorno" onClick={onCerrar} disabled={enviando}>
                    Cancelar
                </button>
                <button
                    type="button"
                    className={activar ? 'boton-principal' : 'boton-peligro'}
                    onClick={confirmar}
                    disabled={enviando}
                >
                    {enviando ? 'Guardando…' : activar ? 'Activar' : 'Desactivar'}
                </button>
            </div>
        </Modal>
    );
}
