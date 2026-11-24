import Conversacion from './asistente/Conversacion';
import '../estilos/Comun.css';
import '../estilos/Senas.css';
import '../estilos/Asistente.css';

// Sección Asistente: la misma conversación que la ventana flotante, a pantalla completa
export default function Asistente() {
    return (
        <section className="tabla-caja asistente">
            <Conversacion />
        </section>
    );
}
