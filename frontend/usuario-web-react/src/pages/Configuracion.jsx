import { Link } from "react-router-dom";
import Avatar from "../components/Avatar";
import { useAuth } from "../context/useAuth";
import { usePreferencias } from "../context/usePreferencias";

const MODOS = [
  { id: "texto", etiqueta: "Texto" },
  { id: "voz", etiqueta: "Voz" },
  { id: "ambos", etiqueta: "Ambos" },
];
const TEMAS = [
  { id: "claro", etiqueta: "Claro" },
  { id: "oscuro", etiqueta: "Oscuro" },
  { id: "auto", etiqueta: "Automático" },
];
const TAMANOS = [
  { id: "pequeno", etiqueta: "A", clase: "t-peq", nombre: "Pequeño" },
  { id: "medio", etiqueta: "A", clase: "t-med", nombre: "Mediano" },
  { id: "grande", etiqueta: "A", clase: "t-gra", nombre: "Grande" },
];

// Grupo de botones tipo "opción única"
function Opciones({ lista, valor, onElegir, nombre }) {
  return (
    <div className="sa-opciones" role="radiogroup" aria-label={nombre}>
      {lista.map((o) => (
        <button
          key={o.id}
          type="button"
          role="radio"
          aria-checked={valor === o.id}
          aria-label={o.nombre}
          className={`sa-opcion ${o.clase ?? ""} ${valor === o.id ? "activo" : ""}`}
          onClick={() => onElegir(o.id)}
        >
          {o.etiqueta}
        </button>
      ))}
    </div>
  );
}

function Interruptor({ activo, onCambiar, nombre }) {
  return (
    <button
      type="button"
      role="switch"
      aria-checked={activo}
      aria-label={nombre}
      className={activo ? "sa-switch activo" : "sa-switch"}
      onClick={() => onCambiar(!activo)}
    />
  );
}

export default function Configuracion() {
  const { usuario } = useAuth();
  const { prefs, cambiar } = usePreferencias();
  const rol = usuario.rol === "oyente" ? "Usuario" : usuario.rol;

  return (
    <section className="sa-pagina sa-perfil">
      <header className="sa-titulo-pagina">
        <h1>Configuración</h1>
        <p>Personaliza tu experiencia en SeñasApp. Los cambios se guardan solos.</p>
      </header>

      <section className="sa-perfil-tarjeta">
        <h3>Información de la cuenta</h3>
        <div className="sa-datos-cuenta">
          <Avatar usuario={usuario} grande />
          <dl>
            <div><dt>Nombre</dt><dd>{usuario.nombre}</dd></div>
            <div><dt>Correo</dt><dd>{usuario.email}</dd></div>
            <div><dt>Rol</dt><dd>{rol}</dd></div>
          </dl>
          <Link to="/perfil" className="sa-pbtn sec">Editar en Mi perfil</Link>
        </div>
      </section>

      <section className="sa-perfil-tarjeta">
        <h3>Notificaciones</h3>
        <div className="sa-fila">
          <div>
            <strong>Recibir notificaciones</strong>
            <small>Muestra la campana con avisos de nuevas señas y novedades del equipo.</small>
          </div>
          <Interruptor
            activo={prefs.notificaciones}
            onCambiar={(v) => cambiar({ notificaciones: v })}
            nombre="Recibir notificaciones"
          />
        </div>
      </section>

      <section className="sa-perfil-tarjeta">
        <h3>Preferencias generales</h3>
        <div className="sa-fila">
          <div>
            <strong>Modo de salida</strong>
            <small>Cómo quieres recibir cada seña traducida.</small>
          </div>
          <Opciones lista={MODOS} valor={prefs.modo} onElegir={(modo) => cambiar({ modo })} nombre="Modo de salida" />
        </div>
        <div className="sa-fila">
          <div>
            <strong>Tema visual</strong>
            <small>Elige el estilo de colores que más te guste.</small>
          </div>
          <Opciones lista={TEMAS} valor={prefs.tema} onElegir={(tema) => cambiar({ tema })} nombre="Tema visual" />
        </div>
        <div className="sa-fila">
          <div>
            <strong>Tamaño de texto</strong>
            <small>Ajusta el tamaño del texto en la app.</small>
          </div>
          <Opciones lista={TAMANOS} valor={prefs.tamano} onElegir={(tamano) => cambiar({ tamano })} nombre="Tamaño de texto" />
        </div>
      </section>
    </section>
  );
}