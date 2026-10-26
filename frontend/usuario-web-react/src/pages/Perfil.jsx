import { useState } from "react";
import Avatar from "../components/Avatar";
import { useAuth } from "../context/useAuth";
import { cambiarPassword, guardarDatos, guardarFoto } from "../services/perfil";

const TAM_FOTO = 256; // lado en píxeles de la foto que se guarda

// Recorta la imagen al centro, la reduce a 256 x 256 y la devuelve como texto (base64)
function reducirImagen(archivo) {
  return new Promise((resolve, reject) => {
    const lector = new FileReader();
    lector.onerror = () => reject(new Error("No se pudo leer la imagen."));
    lector.onload = () => {
      const img = new Image();
      img.onerror = () => reject(new Error("La imagen no es válida."));
      img.onload = () => {
        const lado = Math.min(img.width, img.height);
        const sx = (img.width - lado) / 2;
        const sy = (img.height - lado) / 2;
        const lienzo = document.createElement("canvas");
        lienzo.width = TAM_FOTO;
        lienzo.height = TAM_FOTO;
        lienzo.getContext("2d").drawImage(img, sx, sy, lado, lado, 0, 0, TAM_FOTO, TAM_FOTO);
        resolve(lienzo.toDataURL("image/jpeg", 0.85));
      };
      img.src = lector.result;
    };
    lector.readAsDataURL(archivo);
  });
}

function Mensaje({ msg }) {
  if (!msg) return null;
  return (
    <p className={`sa-msg ${msg.tipo}`} role={msg.tipo === "error" ? "alert" : "status"}>
      {msg.texto}
    </p>
  );
}

// Campo de contraseña con el ojito para ver u ocultar
function CampoClave({ etiqueta, valor, onCambio, autoComplete }) {
  const [ver, setVer] = useState(false);
  return (
    <label className="campo">
      {etiqueta}
      <div className="campo-password">
        <input
          type={ver ? "text" : "password"}
          value={valor}
          onChange={(e) => onCambio(e.target.value)}
          maxLength={30}
          required
          autoComplete={autoComplete}
        />
        <button
          type="button"
          className="ojo"
          onClick={() => setVer(!ver)}
          aria-label={ver ? "Ocultar contraseña" : "Mostrar contraseña"}
        >
          {ver ? (
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M17.9 17.9A10.6 10.6 0 0 1 12 20C5 20 1 12 1 12a18.5 18.5 0 0 1 5.1-5.9M9.9 4.2A9.8 9.8 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.2 3.2M1 1l22 22" /></svg>
          ) : (
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M1 12S5 4 12 4s11 8 11 8-4 8-11 8S1 12 1 12z" /><circle cx="12" cy="12" r="3" /></svg>
          )}
        </button>
      </div>
    </label>
  );
}

export default function Perfil() {
  const { usuario, actualizar } = useAuth();
  const [nombre, setNombre] = useState(usuario.nombre);
  const [email, setEmail] = useState(usuario.email);
  const [actual, setActual] = useState("");
  const [nueva, setNueva] = useState("");
  const [repetir, setRepetir] = useState("");
  const [msgFoto, setMsgFoto] = useState(null);
  const [msgDatos, setMsgDatos] = useState(null);
  const [msgClave, setMsgClave] = useState(null);
  const [ocupado, setOcupado] = useState(false);

  const rol = usuario.rol.charAt(0).toUpperCase() + usuario.rol.slice(1);

  async function subirFoto(e) {
    const archivo = e.target.files?.[0];
    e.target.value = ""; // permite elegir la misma foto otra vez
    if (!archivo) return;
    if (!archivo.type.startsWith("image/")) {
      setMsgFoto({ tipo: "error", texto: "Elige un archivo de imagen." });
      return;
    }
    setMsgFoto(null);
    setOcupado(true);
    try {
      const foto = await reducirImagen(archivo);
      actualizar(await guardarFoto(usuario.idUsuario, foto));
      setMsgFoto({ tipo: "ok", texto: "Foto actualizada." });
    } catch (err) {
      setMsgFoto({ tipo: "error", texto: err.message });
    } finally {
      setOcupado(false);
    }
  }

  async function quitarFoto() {
    setMsgFoto(null);
    setOcupado(true);
    try {
      actualizar(await guardarFoto(usuario.idUsuario, ""));
      setMsgFoto({ tipo: "ok", texto: "Foto eliminada." });
    } catch (err) {
      setMsgFoto({ tipo: "error", texto: err.message });
    } finally {
      setOcupado(false);
    }
  }

  async function guardar(e) {
    e.preventDefault();
    setMsgDatos(null);
    setOcupado(true);
    try {
      const nuevo = await guardarDatos(usuario.idUsuario, { nombre, email });
      actualizar(nuevo);
      setNombre(nuevo.nombre);
      setEmail(nuevo.email);
      setMsgDatos({ tipo: "ok", texto: "Datos guardados." });
    } catch (err) {
      setMsgDatos({ tipo: "error", texto: err.message });
    } finally {
      setOcupado(false);
    }
  }

  async function cambiarClave(e) {
    e.preventDefault();
    setMsgClave(null);
    if (nueva !== repetir) {
      setMsgClave({ tipo: "error", texto: "Las contraseñas nuevas no coinciden." });
      return;
    }
    setOcupado(true);
    try {
      await cambiarPassword(usuario.idUsuario, { actual, nueva });
      setActual("");
      setNueva("");
      setRepetir("");
      setMsgClave({ tipo: "ok", texto: "Contraseña actualizada." });
    } catch (err) {
      setMsgClave({ tipo: "error", texto: err.message });
    } finally {
      setOcupado(false);
    }
  }

  return (
    <section className="sa-pagina sa-perfil">
      <header className="sa-titulo-pagina">
        <h1>Mi perfil</h1>
        <p>Administra tu foto, tus datos y tu contraseña.</p>
      </header>

      <div className="sa-perfil-cabeza">
        <Avatar usuario={usuario} grande />
        <div className="sa-perfil-info">
          <h2>{usuario.nombre}</h2>
          <p>{usuario.email}</p>
          <span className="sa-rol">{rol}</span>
          <div className="sa-perfil-acciones">
            <label className="sa-pbtn">
              Cambiar foto
              <input type="file" accept="image/*" onChange={subirFoto} disabled={ocupado} hidden />
            </label>
            <button type="button" className="sa-pbtn sec" onClick={quitarFoto} disabled={ocupado || !usuario.foto}>
              Quitar foto
            </button>
          </div>
          <Mensaje msg={msgFoto} />
        </div>
      </div>

      <div className="sa-perfil-grid">
        <form className="sa-perfil-tarjeta" onSubmit={guardar}>
          <h3>Datos personales</h3>
          <label className="campo">
            Nombre
            <input value={nombre} onChange={(e) => setNombre(e.target.value)} maxLength={50} required autoComplete="name" />
          </label>
          <label className="campo">
            Correo
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required autoComplete="email" />
          </label>
          <Mensaje msg={msgDatos} />
          <button className="sa-pbtn" disabled={ocupado}>Guardar cambios</button>
        </form>

        <form className="sa-perfil-tarjeta" onSubmit={cambiarClave}>
          <h3>Cambiar contraseña</h3>
          <CampoClave etiqueta="Contraseña actual" valor={actual} onCambio={setActual} autoComplete="current-password" />
          <CampoClave etiqueta="Contraseña nueva" valor={nueva} onCambio={setNueva} autoComplete="new-password" />
          <CampoClave etiqueta="Repite la nueva" valor={repetir} onCambio={setRepetir} autoComplete="new-password" />
          <Mensaje msg={msgClave} />
          <button className="sa-pbtn" disabled={ocupado}>Actualizar contraseña</button>
        </form>
      </div>
    </section>
  );
}