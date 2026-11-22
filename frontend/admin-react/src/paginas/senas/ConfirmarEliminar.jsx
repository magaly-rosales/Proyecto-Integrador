import { useState } from 'react';
import { mensajeError } from '../../api/errores';
import { eliminarSena } from '../../api/senasApi';
import Modal from '../../components/ui/Modal';

export default function ConfirmarEliminar({ sena, onCerrar, onEliminada }) {
    const [enviando, setEnviando] = useState(false);
    const [error, setError] = useState('');

    const eliminar = async () => {
        setEnviando(true);
        setError('');
        try {
            await eliminarSena(sena.id);
            onEliminada();
        } catch (err) {
            setError(mensajeError(err, 'No se pudo eliminar la seña.'));
            setEnviando(false);
        }
    };

    return (
        <Modal titulo={`¿Eliminar la seña «${sena.nombre}»?`} onCerrar={onCerrar} ancho={440}>
            <p className="modal-texto">
                Esta acción no se puede deshacer. La seña dejará de verse en la web y en la app móvil.
            </p>
            {error && <p className="modal-error" role="alert">{error}</p>}
            <div className="modal-acciones">
                <button type="button" className="boton-contorno" onClick={onCerrar} disabled={enviando}>
                    Cancelar
                </button>
                <button type="button" className="boton-peligro" onClick={eliminar} disabled={enviando}>
                    {enviando ? 'Eliminando…' : 'Eliminar'}
                </button>
            </div>
        </Modal>
    );
}