import { useState } from 'react';
import { mensajeError } from '../../api/errores';
import { borrarEntrenamiento } from '../../api/entrenamientoApi';
import Modal from '../../components/ui/Modal';

// Confirma el borrado de todo el entrenamiento de una seña
export default function ConfirmarBorrado({ sena, onCerrar, onBorrado }) {
    const [enviando, setEnviando] = useState(false);
    const [error, setError] = useState('');

    const borrar = async () => {
        setEnviando(true);
        setError('');
        try {
            await borrarEntrenamiento(sena.id, 'todas');
            onBorrado();
        } catch (err) {
            setError(mensajeError(err, 'No se pudo borrar el entrenamiento.'));
            setEnviando(false);
        }
    };

    return (
        <Modal titulo={`Borrar el entrenamiento de «${sena.nombre}»`} onCerrar={onCerrar} ancho={520}>
            {error && <p className="modal-error" role="alert">{error}</p>}
            <div className="modal-acciones">
                <button type="button" className="boton-contorno" onClick={onCerrar} disabled={enviando}>
                    Cancelar
                </button>
                <button type="button" className="boton-peligro" disabled={enviando} onClick={borrar}>
                    {enviando ? 'Borrando…' : 'Borrar'}
                </button>
            </div>
        </Modal>
    );
}
