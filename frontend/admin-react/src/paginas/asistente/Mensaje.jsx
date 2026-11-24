// Un mensaje del chat; si la IA propuso activar o desactivar una cuenta, muestra los botones para confirmarlo
export default function Mensaje({ mensaje, onConfirmar, onCancelar }) {
    const { rol, texto, error, accion, estadoAccion } = mensaje;
    const clase = `asistente-mensaje asistente-${rol}${error ? ' asistente-error' : ''}`;

    return (
        <div className={clase}>
            <p>{texto}</p>

            {accion && (
                <div className="asistente-accion">
                    <strong>
                        {accion.activar ? 'Activar' : 'Desactivar'} la cuenta de {accion.nombre}
                    </strong>

                    {(estadoAccion === 'pendiente' || estadoAccion === 'enviando') && (
                        <div className="asistente-accion-botones">
                            <button
                                type="button"
                                className={accion.activar ? 'boton-principal' : 'boton-peligro'}
                                disabled={estadoAccion === 'enviando'}
                                onClick={() => onConfirmar(mensaje)}
                            >
                                {estadoAccion === 'enviando' ? 'Guardando…' : 'Confirmar'}
                            </button>
                            <button
                                type="button"
                                className="boton-contorno"
                                disabled={estadoAccion === 'enviando'}
                                onClick={() => onCancelar(mensaje)}
                            >
                                Cancelar
                            </button>
                        </div>
                    )}
                    {estadoAccion === 'confirmada' && <span className="texto-suave">Cambio confirmado.</span>}
                    {estadoAccion === 'cancelada' && <span className="texto-suave">Cancelado: no se cambió nada.</span>}
                </div>
            )}
        </div>
    );
}
