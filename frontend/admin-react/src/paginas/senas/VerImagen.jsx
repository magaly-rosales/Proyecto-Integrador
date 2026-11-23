import Modal from '../../components/ui/Modal';

// Imagen de la seña a tamaño completo
export default function VerImagen({ sena, onCerrar }) {
    return (
        <Modal titulo={sena.nombre} onCerrar={onCerrar} ancho={640}>
            <img
                src={sena.imagen}
                alt={`Imagen de la seña ${sena.nombre}`}
                style={{ width: '100%', maxHeight: '70vh', objectFit: 'contain', borderRadius: 8 }}
            />
        </Modal>
    );
}