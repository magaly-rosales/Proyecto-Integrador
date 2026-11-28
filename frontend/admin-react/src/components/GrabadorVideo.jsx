import { useEffect, useRef, useState } from 'react';
import '../estilos/Grabador.css';

const MAX_SEGUNDOS = 10;

// Chrome y Edge recientes pueden grabar directamente en MP4
export const puedeGrabarMp4 = () =>
    typeof MediaRecorder !== 'undefined' && MediaRecorder.isTypeSupported('video/mp4');

// Graba el video de referencia con la cámara, lo revisa y lo entrega como archivo MP4.
// onListo(archivo) recibe el File; onCancelar() cierra el grabador.
export default function GrabadorVideo({ onListo, onCancelar }) {
    const videoRef = useRef(null);
    const streamRef = useRef(null);
    const grabadorRef = useRef(null);
    const trozosRef = useRef([]);
    const archivoRef = useRef(null);
    const temporizadorRef = useRef(null);

    const [fase, setFase] = useState('abriendo'); // abriendo | listo | grabando | revisar | error
    const [segundos, setSegundos] = useState(0);
    const [error, setError] = useState('');
    const [urlVista, setUrlVista] = useState(null);

    // Abre la cámara al mostrarse y la apaga al cerrarse
    useEffect(() => {
        let cancelado = false;
        (async () => {
            if (!navigator.mediaDevices?.getUserMedia) {
                setError('Tu navegador no permite acceder a la cámara.');
                setFase('error');
                return;
            }
            try {
                const stream = await navigator.mediaDevices.getUserMedia({
                    video: { width: 640, height: 480 },
                    audio: false,
                });
                if (cancelado) {
                    stream.getTracks().forEach((t) => t.stop());
                    return;
                }
                streamRef.current = stream;
                videoRef.current.srcObject = stream;
                await videoRef.current.play();
                setFase('listo');
            } catch (e) {
                setError(
                    e.name === 'NotAllowedError'
                        ? 'Permiso de cámara denegado. Actívalo en el navegador.'
                        : e.name === 'NotFoundError'
                            ? 'No se encontró ninguna cámara en este equipo.'
                            : 'No se pudo abrir la cámara.'
                );
                setFase('error');
            }
        })();
        return () => {
            cancelado = true;
            clearInterval(temporizadorRef.current);
            if (grabadorRef.current?.state === 'recording') grabadorRef.current.stop();
            streamRef.current?.getTracks().forEach((t) => t.stop());
            streamRef.current = null;
        };
    }, []);

    // Libera la vista previa anterior
    useEffect(() => () => urlVista && URL.revokeObjectURL(urlVista), [urlVista]);

    const detener = () => {
        clearInterval(temporizadorRef.current);
        if (grabadorRef.current?.state === 'recording') grabadorRef.current.stop();
    };

    // Se detiene solo al llegar al máximo
    useEffect(() => {
        if (fase === 'grabando' && segundos >= MAX_SEGUNDOS) detener();
    }, [fase, segundos]);

    const grabar = () => {
        const mime = MediaRecorder.isTypeSupported('video/mp4;codecs=avc1')
            ? 'video/mp4;codecs=avc1'
            : 'video/mp4';
        const grabador = new MediaRecorder(streamRef.current, { mimeType: mime });
        trozosRef.current = [];
        grabador.ondataavailable = (e) => e.data.size && trozosRef.current.push(e.data);
        grabador.onstop = () => {
            const blob = new Blob(trozosRef.current, { type: 'video/mp4' });
            archivoRef.current = new File([blob], 'referencia.mp4', { type: 'video/mp4' });
            setUrlVista(URL.createObjectURL(blob));
            setFase('revisar');
        };
        grabadorRef.current = grabador;
        grabador.start();
        setSegundos(0);
        setFase('grabando');
        temporizadorRef.current = setInterval(() => setSegundos((s) => s + 1), 1000);
    };

    const repetir = () => {
        setUrlVista(null);
        archivoRef.current = null;
        setFase('listo');
    };

    const camaraVisible = fase !== 'revisar';

    return (
        <div className="grabador">
            {fase === 'error' && <p className="campo-error" role="alert">{error}</p>}
            {fase === 'abriendo' && <p className="campo-ayuda">Abriendo cámara…</p>}

            <video
                ref={videoRef}
                className="grabador-video"
                playsInline
                muted
                style={{ display: camaraVisible && fase !== 'error' ? 'block' : 'none' }}
            />
            {fase === 'revisar' && urlVista && (
                <video className="grabador-video" src={urlVista} controls playsInline />
            )}

            {fase === 'grabando' && (
                <p className="grabador-tiempo" role="status">
                    <i aria-hidden="true" /> Grabando… {segundos} / {MAX_SEGUNDOS} s
                </p>
            )}

            <div className="grabador-acciones">
                {fase === 'listo' && (
                    <button type="button" className="boton-principal" onClick={grabar}>Empezar a grabar</button>
                )}
                {fase === 'grabando' && (
                    <button type="button" className="boton-principal" onClick={detener}>Detener</button>
                )}
                {fase === 'revisar' && (
                    <>
                        <button type="button" className="boton-principal" onClick={() => onListo(archivoRef.current)}>
                            Usar este video
                        </button>
                        <button type="button" className="boton-contorno" onClick={repetir}>Repetir</button>
                    </>
                )}
                <button type="button" className="boton-contorno" onClick={onCancelar}>Cancelar</button>
            </div>
        </div>
    );
}
