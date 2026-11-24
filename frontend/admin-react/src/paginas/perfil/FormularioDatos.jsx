import FotoPerfil from './FotoPerfil';

// Datos de la cuenta: foto editable; el nombre solo se muestra
export default function FormularioDatos({ perfil, onGuardado }) {
    return (
        <section className="tabla-caja perfil-tarjeta">
            <header className="perfil-cabecera">
                <h2>Datos de la cuenta</h2>
            </header>
            <div className="perfil-cuerpo">
                <FotoPerfil perfil={perfil} onCambio={onGuardado} />
                <div className="perfil-identidad">
                    <strong>{perfil.nombre}</strong>
                </div>
            </div>
        </section>
    );
}
