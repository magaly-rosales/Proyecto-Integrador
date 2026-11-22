import Modal from '../../components/ui/Modal';

export default function VerVideo({ sena, onCerrar }) {
    return (
        <Modal titulo={`Video de «${sena.nombre}»`} onCerrar={onCerrar} ancho={720}>
            <video className="modal-video" src={sena.video} controls autoPlay playsInline />
        </Modal>
    );
}