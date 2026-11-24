import { useEffect, useRef, useState } from 'react';
import { guardarMuestra, guardarSecuencia } from '../api/entrenamientoApi';
import { mensajeError } from '../api/errores';

// Ajustes de la captura con la cámara
export const MUESTRAS_RAFAGA = 20;
export const DURACION_MOV_S = 2;

const CUENTA_REGRESIVA_S = 3;
const INTERVALO_RAFAGA_MS = 300;
const INTERVALO_MOV_MS = 100;
const MIN_CUADROS = 8;
const SEGUNDOS_AVISO = 4;

const esperar = (ms) => new Promise((resolver) => setTimeout(resolver, ms));

// Voz del navegador: dice el resultado en voz alta (si el navegador no la tiene, no pasa nada)
const decir = (texto) => {
    try {
        if (!('speechSynthesis' in window)) return;
        window.speechSynthesis.cancel();
        const voz = new SpeechSynthesisUtterance(texto);
        voz.lang = 'es-PE';
        window.speechSynthesis.speak(voz);
    } catch {
        // la voz es opcional
    }
};

// Captura muestras y movimientos de la mano y los envía al backend.
// manosRef.current trae las manos detectadas por la cámara en este instante.
export default function useCaptura({ idSena, manosRef, onGuardado }) {
    const montadoRef = useRef(true);
    const [mensaje, setMensaje] = useState(null); // { tipo: 'ok' | 'aviso' | 'error', texto }
    const [ocupado, setOcupado] = useState(false);

    useEffect(() => {
        montadoRef.current = true;
        return () => {
            montadoRef.current = false;
            try {
                window.speechSynthesis?.cancel();
            } catch {
                // nada que cancelar
            }
        };
    }, []);

    // El aviso final se oculta solo
    useEffect(() => {
        if (!mensaje || ocupado) return undefined;
        const id = setTimeout(() => setMensaje(null), SEGUNDOS_AVISO * 1000);
        return () => clearTimeout(id);
    }, [mensaje, ocupado]);

    // Resultado final: se muestra arriba y se dice en voz alta
    const resultado = (tipo, texto) => {
        setMensaje({ tipo, texto });
        decir(texto);
    };

    const cuentaRegresiva = async () => {
        for (let s = CUENTA_REGRESIVA_S; s > 0; s -= 1) {
            setMensaje({ tipo: 'aviso', texto: `Prepara la seña... ${s}` });
            await esperar(1000);
            if (!montadoRef.current) return false;
        }
        return true;
    };

    const terminar = () => {
        if (montadoRef.current) setOcupado(false);
        onGuardado?.();
    };

    // Una sola muestra (señas quietas con una mano)
    const capturarUna = async () => {
        const manos = manosRef.current;
        if (manos.length === 0) {
            resultado('aviso', 'No se detecta tu mano. Inténtalo una vez más.');
            return;
        }
        setOcupado(true);
        try {
            await guardarMuestra(idSena, manos);
            resultado('ok', 'Muestra guardada.');
        } catch (error) {
            resultado('error', mensajeError(error, 'No se pudo guardar. Inténtalo una vez más.'));
        } finally {
            terminar();
        }
    };

    // Con las dos manos en la seña no se puede hacer clic: cuenta regresiva y varias muestras seguidas
    const capturarRafaga = async () => {
        setOcupado(true);
        try {
            if (!(await cuentaRegresiva())) return;

            // El número de manos se fija al empezar: no se mezclan muestras de 1 y 2 manos
            const manosInicio = manosRef.current.length;
            if (manosInicio === 0) {
                resultado('aviso', 'No se detecta tu mano. Inténtalo una vez más.');
                return;
            }

            let guardadas = 0;
            let intentos = 0;
            while (guardadas < MUESTRAS_RAFAGA && intentos < MUESTRAS_RAFAGA * 5) {
                intentos += 1;
                const manos = manosRef.current;
                if (manos.length === manosInicio) {
                    await guardarMuestra(idSena, manos);
                    guardadas += 1;
                    setMensaje({ tipo: 'aviso', texto: `Grabando... ${guardadas}/${MUESTRAS_RAFAGA}` });
                }
                await esperar(INTERVALO_RAFAGA_MS);
                if (!montadoRef.current) return;
            }

            if (guardadas === MUESTRAS_RAFAGA) {
                resultado('ok', 'Seña guardada correctamente.');
            } else if (guardadas > 0) {
                resultado('aviso', 'Se guardó una parte. Inténtalo una vez más.');
            } else {
                resultado('aviso', 'No se pudo guardar. Inténtalo una vez más.');
            }
        } catch (error) {
            resultado('error', mensajeError(error, 'No se pudo guardar. Inténtalo una vez más.'));
        } finally {
            terminar();
        }
    };

    // Graba unos segundos de la seña en movimiento y la guarda como un movimiento
    const grabarMovimiento = async () => {
        setOcupado(true);
        try {
            if (!(await cuentaRegresiva())) return;

            const cuadros = [];
            let numManos = 0;
            const fin = Date.now() + DURACION_MOV_S * 1000;
            setMensaje({ tipo: 'aviso', texto: '¡Haz la seña ahora!' });

            while (Date.now() < fin) {
                const manos = manosRef.current;
                if (manos.length > 0) {
                    if (numManos === 0) numManos = manos.length;
                    if (manos.length === numManos) {
                        cuadros.push(manos.map((m) => ({
                            etiqueta: m.etiqueta,
                            puntos: m.puntos.map(({ x, y, z }) => ({ x, y, z })),
                        })));
                    }
                }
                await esperar(INTERVALO_MOV_MS);
                if (!montadoRef.current) return;
            }

            if (cuadros.length < MIN_CUADROS) {
                resultado('aviso', 'No se pudo grabar la seña. Inténtalo una vez más.');
                return;
            }

            await guardarSecuencia(idSena, cuadros);
            resultado('ok', 'Seña grabada correctamente.');
        } catch (error) {
            resultado('error', mensajeError(error, 'No se pudo guardar. Inténtalo una vez más.'));
        } finally {
            terminar();
        }
    };

    return { mensaje, ocupado, capturarUna, capturarRafaga, grabarMovimiento, avisar: setMensaje };
}
