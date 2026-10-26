import { useAuth } from "../context/useAuth";
import { usePreferencias } from "../context/usePreferencias";
import { IconoAjustes, IconoCamara, IconoCampana } from "../components/Iconos";

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
      className={activo ? "sa-switch naranja activo" : "sa-switch naranja"}
      onClick={() => onCambiar(!activo)}
    />
  );
}

export default function Configuracion() {
  const { usuario } = useAuth();
  const { prefs, cambiar } = usePreferencias();

  return (
    <section className="sa-pagina">
      <header className="sa-titulo-pagina con-icono">
        <span className="sa-icono-caja naranja">
          <IconoAjustes tamano={30} />
        </span>
        <div>
          <h1>Configuración</h1>
          <p>Personaliza tu experiencia en SeñasApp. Los cambios se guardan solos.</p>
        </div>
      </header>

      <div className="sa-config">
        <section className="tarjeta">
          <h3>Información de la cuenta</h3>
          <div className="sa-cuenta">
            <span className="sa-avatar sa-avatar-xl">{usuario.nombre.charAt(0).toUpperCase()}</span>
            <div>
              <strong>{usuario.nombre}</strong>
              <small>{usuario.email}</small>
            </div>
            <div className="sa-dato">
              <small>Rol</small>
              <strong>{usuario.rol === "oyente" ? "Usuario" : usuario.rol}</strong>
            </div>
          </div>
        </section>

        <section className="tarjeta">
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

        <div className="sa-dos">
          <section className="tarjeta sa-interruptor-tarjeta">
            <span className="sa-icono-caja azul">
              <IconoCampana tamano={22} />
            </span>
            <div>
              <strong>Notificaciones</strong>
              <small>Alertas sobre nuevas señas y actualizaciones.</small>
            </div>
            <Interruptor activo={prefs.notificaciones} onCambiar={(v) => cambiar({ notificaciones: v })} nombre="Notificaciones" />
          </section>
          <section className="tarjeta sa-interruptor-tarjeta">
            <span className="sa-icono-caja verde">
              <IconoCamara tamano={22} />
            </span>
            <div>
              <strong>Cámara y micrófono</strong>
              <small>Acceso para la traducción en tiempo real.</small>
            </div>
            <Interruptor activo={prefs.camara} onCambiar={(v) => cambiar({ camara: v })} nombre="Cámara y micrófono" />
          </section>
        </div>
      </div>
    </section>
  );
}
