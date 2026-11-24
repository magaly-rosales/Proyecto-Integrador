import { useState } from 'react';
import { mensajeError } from '../../api/errores';
import { cambiarPassword } from '../../api/perfilApi';
import { CampoClave, errorDeApi } from './CampoPerfil';

const VACIO = { actual: '', nueva: '', confirmar: '' };

// Cambia la contraseña del administrador; el backend comprueba la actual y las reglas de la nueva
export default function FormularioPassword() {
    const [valores, setValores] = useState(VACIO);
    const [enviando, setEnviando] = useState(false);
    const [error, setError] = useState({ campo: '', texto: '' });
    const [guardado, setGuardado] = useState(false);

    const cambiar = (campo) => (e) => setValores((v) => ({ ...v, [campo]: e.target.value }));

    const enviar = async (evento) => {
        evento.preventDefault();
        setEnviando(true);
        setError({ campo: '', texto: '' });
        setGuardado(false);
        try {
            await cambiarPassword(valores);
            setValores(VACIO);
            setGuardado(true);
        } catch (err) {
            setError(errorDeApi(err, mensajeError, 'No se pudo cambiar la contraseña.'));
        } finally {
            setEnviando(false);
        }
    };

    const errorEn = (campo) => (error.campo === campo ? error.texto : '');
    const errorGeneral = error.texto && !['actual', 'nueva', 'confirmar'].includes(error.campo);

    return (
        <section className="tabla-caja perfil-tarjeta">
            <header className="perfil-cabecera">
                <h2>Cambiar contraseña</h2>
                <p>Por seguridad, te pedimos la actual.</p>
            </header>
            <form className="perfil-cuerpo" onSubmit={enviar} noValidate>
                <CampoClave
                    etiqueta="Contraseña actual"
                    value={valores.actual}
                    onChange={cambiar('actual')}
                    error={errorEn('actual')}
                    autoComplete="current-password"
                />
                <CampoClave
                    etiqueta="Nueva contraseña"
                    value={valores.nueva}
                    onChange={cambiar('nueva')}
                    error={errorEn('nueva')}
                    placeholder="Mínimo 8 caracteres"
                    autoComplete="new-password"
                />
                <CampoClave
                    etiqueta="Repite la nueva contraseña"
                    value={valores.confirmar}
                    onChange={cambiar('confirmar')}
                    error={errorEn('confirmar')}
                    placeholder="Escríbela otra vez"
                    autoComplete="new-password"
                />
                {errorGeneral && <p className="campo-error" role="alert">{error.texto}</p>}
                {guardado && <p className="perfil-exito" role="status">Contraseña actualizada correctamente.</p>}
                <div className="perfil-acciones">
                    <button type="submit" className="boton-principal" disabled={enviando}>
                        {enviando ? 'Guardando…' : 'Actualizar contraseña'}
                    </button>
                </div>
            </form>
        </section>
    );
}
