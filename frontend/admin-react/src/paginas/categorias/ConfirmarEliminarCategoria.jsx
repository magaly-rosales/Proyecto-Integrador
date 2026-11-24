import { useState } from 'react';
import { eliminarCategoria } from '../../api/categoriasApi';
import { mensajeError } from '../../api/errores';
import Modal from '../../components/ui/Modal';

export default function ConfirmarEliminarCategoria({ categoria, onCerrar, onEliminada }) {
    const [enviando, setEnviando] = useState(false);
    const [error, setError] = useState('');
    const total = categoria.total_senas;

    const eliminar = async () => {
        setEnviando(true);
        setError('');
        try {
            await eliminarCategoria(categoria.id);
            onEliminada();
        } catch (err) {
            setError(mensajeError(err, 'No se pudo eliminar la categoría.'));
            setEnviando(false);
        }
    };

    return (
        <Modal titulo={`¿Eliminar la categoría «${categoria.nombre}»?`} onCerrar={onCerrar} ancho={460}>
            {error && <p className="modal-error" role="alert">{error}</p>}
            <div className="modal-acciones">
                <button type="button" className="boton-contorno" onClick={onCerrar} disabled={enviando}>
                    Cancelar
                </button>
                <button type="button" className="boton-peligro" onClick={eliminar} disabled={enviando}>
                    {enviando ? 'Eliminando…' : total > 0 ? `Eliminar con sus ${total} seña${total === 1 ? '' : 's'}` : 'Eliminar'}
                </button>
            </div>
        </Modal>
    );
}
