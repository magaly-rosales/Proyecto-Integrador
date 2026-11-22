// Marcador temporal de las pantallas que se construyen en los siguientes pasos
export default function EnConstruccion({ nombre }) {
    return (
        <div className="en-construccion">
            <strong>{nombre}</strong>
            <p>Esta pantalla se construye en uno de los siguientes pasos.</p>
        </div>
    );
}