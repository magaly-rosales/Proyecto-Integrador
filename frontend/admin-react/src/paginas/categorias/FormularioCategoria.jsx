import { useEffect, useId, useMemo, useState } from 'react';
import { actualizarCategoria, crearCategoria } from '../../api/categoriasApi';
import { mensajeError } from '../../api/errores';
import { IconoImagen, IconoSubir } from '../../components/ui/IconosUi';
import Modal from '../../components/ui/Modal';
import '../../estilos/Formulario.css';
import '../../estilos/Categorias.css';

const COLOR_INICIAL = '#2BBCC9';
const MAX_IMAGEN = 2 * 1024 * 1024;
const CAMPOS = ['nombre', 'color', 'imagen'];
const HEX = /^#[0-9A-Fa-f]{6}$/;
const extension = (archivo) => archivo.name.split('.').pop().toLowerCase();

// Sirve para crear (categoria = null) y para editar
export default function FormularioCategoria({ categoria, onCerrar, onGuardada }) {
    const id = useId();
    const editando = Boolean(categoria);

    const [nombre, setNombre] = useState(categoria?.nombre ?? '');
    const [color, setColor] = useState(categoria?.color ?? COLOR_INICIAL);
    const [imagen, setImagen] = useState(null);
    const [errores, setErrores] = useState({});
    const [enviando, setEnviando] = useState(false);

    // Vista previa de la imagen elegida (o la que ya tiene la categoría)
    const vistaNueva = useMemo(() => (imagen ? URL.createObjectURL(imagen) : null), [imagen]);
    useEffect(() => () => vistaNueva && URL.revokeObjectURL(vistaNueva), [vistaNueva]);
    const vista = vistaNueva || categoria?.imagen || null;

    const elegirImagen = (e) => {
        const archivo = e.target.files[0];
        e.target.value = '';
        if (!archivo) return;
        let error = '';
        if (!['jpg', 'jpeg', 'png'].includes(extension(archivo))) error = 'La imagen debe ser JPG o PNG.';
        else if (archivo.size > MAX_IMAGEN) error = 'La imagen supera el máximo de 2 MB.';
        setErrores((a) => ({ ...a, imagen: error }));
        if (!error) setImagen(archivo);
    };

    const validar = () => {
        const e = {};
        if (!nombre.trim()) e.nombre = 'Escribe el nombre de la categoría.';
        else if (nombre.trim().length > 50) e.nombre = 'El nombre admite hasta 50 caracteres.';
        if (!HEX.test(color)) e.color = 'Usa el formato #RRGGBB, por ejemplo #2BBCC9.';
        if (!editando && !imagen) e.imagen = 'La imagen es obligatoria.';
        return e;
    };

    const guardar = async (ev) => {
        ev.preventDefault();
        const locales = validar();
        if (Object.keys(locales).length) {
            setErrores(locales);
            return;
        }

        const datos = new FormData();
        datos.append('nombre', nombre.trim());
        datos.append('color', color.toUpperCase());
        if (imagen) datos.append('imagen', imagen);

        setEnviando(true);
        setErrores({});
        try {
            if (editando) await actualizarCategoria(categoria.id, datos);
            else await crearCategoria(datos);
            onGuardada();
        } catch (err) {
            const delServidor = err.response?.data?.errores;
            const porCampo = {};
            if (delServidor && typeof delServidor === 'object') {
                CAMPOS.forEach((c) => {
                    if (delServidor[c]) porCampo[c] = [].concat(delServidor[c])[0];
                });
            }
            setErrores(
                Object.keys(porCampo).length
                    ? porCampo
                    : { general: mensajeError(err, 'No se pudo guardar la categoría.') }
            );
            setEnviando(false);
        }
    };

    const colorValido = HEX.test(color) ? color : COLOR_INICIAL;

    return (
        <Modal titulo={editando ? `Editar «${categoria.nombre}»` : 'Nueva categoría'} onCerrar={onCerrar} ancho={520}>
            <form className="formulario" onSubmit={guardar} noValidate>
                {errores.general && <p className="modal-error" role="alert">{errores.general}</p>}

                <div className="campo">
                    <label className="campo-etiqueta" htmlFor={`${id}-nombre`}>Nombre <b aria-hidden="true">*</b></label>
                    <input
                        id={`${id}-nombre`}
                        type="text"
                        value={nombre}
                        onChange={(e) => setNombre(e.target.value)}
                        placeholder="Ej. Saludos"
                        maxLength={50}
                        aria-invalid={Boolean(errores.nombre)}
                        autoFocus
                    />
                    {errores.nombre && <p className="campo-error" role="alert">{errores.nombre}</p>}
                </div>

                <div className="campo">
                    <span className="campo-etiqueta">
                        Imagen de la categoría {!editando && <b aria-hidden="true">*</b>}
                    </span>
                    <div className="categoria-subida">
                        <span className="categoria-subida-vista" style={{ borderColor: colorValido }}>
                            {vista ? <img src={vista} alt="Vista previa de la categoría" /> : <IconoImagen tam={34} />}
                        </span>
                        <div className="categoria-subida-texto">
                            <label className="boton-contorno boton-archivo">
                                <IconoSubir /> {vista ? 'Cambiar imagen' : 'Elegir imagen'}
                                <input
                                    className="solo-lector"
                                    type="file"
                                    accept=".jpg,.jpeg,.png,image/jpeg,image/png"
                                    onChange={elegirImagen}
                                />
                            </label>
                            <p className="campo-ayuda">
                                Es la imagen que verá el usuario en la web y la app. JPG o PNG, máximo 2 MB.
                            </p>
                        </div>
                    </div>
                    {errores.imagen && <p className="campo-error" role="alert">{errores.imagen}</p>}
                </div>

                <div className="campo">
                    <label className="campo-etiqueta" htmlFor={`${id}-color`}>Color de acento <b aria-hidden="true">*</b></label>
                    <div className="campo-color">
                        <input
                            type="color"
                            aria-label="Elegir color"
                            value={colorValido.toLowerCase()}
                            onChange={(e) => setColor(e.target.value.toUpperCase())}
                        />
                        <input
                            id={`${id}-color`}
                            type="text"
                            value={color}
                            onChange={(e) => setColor(e.target.value)}
                            maxLength={7}
                            aria-invalid={Boolean(errores.color)}
                        />
                    </div>
                    {errores.color && <p className="campo-error" role="alert">{errores.color}</p>}
                </div>

                <div className="modal-acciones">
                    <button type="button" className="boton-contorno" onClick={onCerrar} disabled={enviando}>
                        Cancelar
                    </button>
                    <button type="submit" className="boton-principal" disabled={enviando}>
                        {enviando ? 'Guardando…' : editando ? 'Guardar cambios' : 'Guardar categoría'}
                    </button>
                </div>
            </form>
        </Modal>
    );
}