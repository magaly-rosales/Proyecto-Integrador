import { useEffect, useId, useMemo, useState } from 'react';
import { actualizarSena, crearSena } from '../../api/senasApi';
import { mensajeError } from '../../api/errores';
import { IconoImagen, IconoSubir } from '../../components/ui/IconosUi';
import Modal from '../../components/ui/Modal';
import '../../estilos/Formulario.css';

const MAX_IMAGEN = 2 * 1024 * 1024;
const MAX_VIDEO = 50 * 1024 * 1024;
const extension = (archivo) => archivo.name.split('.').pop().toLowerCase();

// Campos del servidor que tienen su propio lugar en el formulario
const CAMPOS = ['nombre', 'categoria_id', 'descripcion', 'imagen', 'video'];

function Campo({ id, etiqueta, obligatorio, error, ayuda, children }) {
    return (
        <div className="campo">
            <label className="campo-etiqueta" htmlFor={id}>
                {etiqueta} {obligatorio && <b aria-hidden="true">*</b>}
            </label>
            {children}
            {ayuda && <p className="campo-ayuda">{ayuda}</p>}
            {error && <p className="campo-error" id={`${id}-error`} role="alert">{error}</p>}
        </div>
    );
}

// Sirve para crear (sena = null) y para editar una seña
export default function FormularioSena({ sena, categorias, onCerrar, onGuardada }) {
    const id = useId();
    const editando = Boolean(sena);

    const [nombre, setNombre] = useState(sena?.nombre ?? '');
    const [categoriaId, setCategoriaId] = useState(() => {
        if (sena?.categoria_id) return String(sena.categoria_id);
        const igual = categorias.find((c) => c.nombre === sena?.categoria);
        return igual ? String(igual.id) : '';
    });
    const [descripcion, setDescripcion] = useState(sena?.descripcion ?? '');
    const [imagen, setImagen] = useState(null);
    const [video, setVideo] = useState(null);
    const [quitarVideo, setQuitarVideo] = useState(false);
    const [errores, setErrores] = useState({});
    const [enviando, setEnviando] = useState(false);

    // Vista previa de la imagen elegida (o la que ya tiene la seña)
    const vistaNueva = useMemo(() => (imagen ? URL.createObjectURL(imagen) : null), [imagen]);
    useEffect(() => () => vistaNueva && URL.revokeObjectURL(vistaNueva), [vistaNueva]);
    const vista = vistaNueva || sena?.imagen || null;

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

    const elegirVideo = (e) => {
        const archivo = e.target.files[0];
        e.target.value = '';
        if (!archivo) return;
        let error = '';
        if (extension(archivo) !== 'mp4') error = 'El video debe ser MP4.';
        else if (archivo.size > MAX_VIDEO) error = 'El video supera el máximo de 50 MB.';
        setErrores((a) => ({ ...a, video: error }));
        if (!error) {
            setVideo(archivo);
            setQuitarVideo(false);
        }
    };

    const validar = () => {
        const e = {};
        if (!nombre.trim()) e.nombre = 'Escribe el nombre de la seña.';
        else if (nombre.trim().length > 100) e.nombre = 'El nombre admite hasta 100 caracteres.';
        if (!categoriaId) e.categoria_id = 'Elige una categoría.';
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
        datos.append('categoria_id', categoriaId);
        datos.append('descripcion', descripcion.trim());
        if (imagen) datos.append('imagen', imagen);
        if (video) datos.append('video', video);
        else if (quitarVideo) datos.append('quitar_video', 'true');

        setEnviando(true);
        setErrores({});
        try {
            if (editando) await actualizarSena(sena.id, datos);
            else await crearSena(datos);
            onGuardada();
        } catch (err) {
            // Errores del servidor: cada uno bajo su campo; el resto arriba, como mensaje general
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
                    : { general: mensajeError(err, 'No se pudo guardar la seña.') }
            );
            setEnviando(false);
        }
    };

    return (
        <Modal titulo={editando ? `Editar «${sena.nombre}»` : 'Nueva seña'} onCerrar={onCerrar} ancho={560}>
            <form className="formulario" onSubmit={guardar} noValidate>
                {errores.general && <p className="modal-error" role="alert">{errores.general}</p>}

                <Campo id={`${id}-nombre`} etiqueta="Nombre" obligatorio error={errores.nombre}>
                    <input
                        id={`${id}-nombre`}
                        type="text"
                        value={nombre}
                        onChange={(e) => setNombre(e.target.value)}
                        placeholder="Ej. Buenos días"
                        maxLength={100}
                        aria-invalid={Boolean(errores.nombre)}
                        autoFocus
                    />
                </Campo>

                <Campo id={`${id}-categoria`} etiqueta="Categoría" obligatorio error={errores.categoria_id}
                    ayuda={categorias.length === 0 ? 'Aún no hay categorías: créalas primero en la sección Categorías.' : ''}>
                    <select
                        id={`${id}-categoria`}
                        value={categoriaId}
                        onChange={(e) => setCategoriaId(e.target.value)}
                        aria-invalid={Boolean(errores.categoria_id)}
                    >
                        <option value="">Elige una categoría</option>
                        {categorias.map((c) => (
                            <option key={c.id} value={c.id}>{c.nombre}</option>
                        ))}
                    </select>
                </Campo>

                <Campo id={`${id}-descripcion`} etiqueta="Descripción" error={errores.descripcion}
                    ayuda="Explica cómo se hace la seña (posición de la mano y movimiento).">
                    <textarea
                        id={`${id}-descripcion`}
                        rows={3}
                        value={descripcion}
                        onChange={(e) => setDescripcion(e.target.value)}
                    />
                </Campo>

                <Campo id={`${id}-imagen`} etiqueta="Imagen de referencia" obligatorio={!editando}
                    error={errores.imagen} ayuda="JPG o PNG, máximo 2 MB.">
                    <div className="campo-archivo">
                        <span className="vista-previa">
                            {vista ? <img src={vista} alt="Vista previa de la imagen" /> : <IconoImagen tam={26} />}
                        </span>
                        <label className="boton-contorno boton-archivo">
                            <IconoSubir /> {vista ? 'Cambiar imagen' : 'Elegir imagen'}
                            <input
                                id={`${id}-imagen`}
                                className="solo-lector"
                                type="file"
                                accept=".jpg,.jpeg,.png,image/jpeg,image/png"
                                onChange={elegirImagen}
                            />
                        </label>
                    </div>
                </Campo>

                <Campo id={`${id}-video`} etiqueta="Video (opcional)" error={errores.video}
                    ayuda="MP4, máximo 50 MB.">
                    <div className="campo-archivo">
                        <label className="boton-contorno boton-archivo">
                            <IconoSubir /> {video || (sena?.video && !quitarVideo) ? 'Cambiar video' : 'Elegir video'}
                            <input
                                id={`${id}-video`}
                                className="solo-lector"
                                type="file"
                                accept=".mp4,video/mp4"
                                onChange={elegirVideo}
                            />
                        </label>
                        {video && <span className="campo-nombre">{video.name}</span>}
                    </div>
                    {editando && sena.video && !video && (
                        <label className="campo-casilla">
                            <input
                                type="checkbox"
                                checked={quitarVideo}
                                onChange={(e) => setQuitarVideo(e.target.checked)}
                            />
                            Quitar el video actual
                        </label>
                    )}
                </Campo>

                <div className="modal-acciones">
                    <button type="button" className="boton-contorno" onClick={onCerrar} disabled={enviando}>
                        Cancelar
                    </button>
                    <button type="submit" className="boton-principal" disabled={enviando}>
                        {enviando ? 'Guardando…' : editando ? 'Guardar cambios' : 'Guardar seña'}
                    </button>
                </div>
            </form>
        </Modal>
    );
}