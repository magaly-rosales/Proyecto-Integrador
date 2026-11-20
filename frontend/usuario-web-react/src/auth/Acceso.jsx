import { useState } from "react";
import { useLocation, useNavigate, useSearchParams } from "react-router-dom";
import { useAuth } from "../context/useAuth";
import useEscribir from "../hooks/useEscribir";
import "./acceso.css";

const TEXTOS = {
  registro: {
    titulo: "Regístrate y\nempieza a\ntraducir hoy.",
    detalle: "Crea tu cuenta y entiende la lengua de señas peruana en texto y voz, al instante.",
  },
  login: {
    titulo: "Bienvenido\na SeñasApp.",
    detalle: "Ingresa a tu cuenta y sigue conversando sin barreras.",
  },
  recuperar: {
    titulo: "¿Olvidaste\ntu clave?",
    detalle: "Escribe tu correo y te ayudaremos a recuperar el acceso.",
  },
};

function IconoOjo({ visible }) {
  return visible ? (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M17.9 17.9A10.6 10.6 0 0 1 12 20C5 20 1 12 1 12a18.5 18.5 0 0 1 5.1-5.9M9.9 4.2A9.8 9.8 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.2 3.2M1 1l22 22" />
    </svg>
  ) : (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12z" />
      <circle cx="12" cy="12" r="3" />
    </svg>
  );
}

export default function Acceso() {
  const { login, registro } = useAuth();
  const navegar = useNavigate();
  const ubicacion = useLocation();
  const [params] = useSearchParams();

  const [modo, setModo] = useState(params.has("registro") ? "registro" : "login");
  const [nombre, setNombre] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [acepto, setAcepto] = useState(false);
  const [recordar, setRecordar] = useState(false);
  const [vista, setVista] = useState("login"); // "login" | "recuperar"
  const [aviso, setAviso] = useState("");
  const [verPassword, setVerPassword] = useState(false);
  const [error, setError] = useState("");
  const [cargando, setCargando] = useState(false);

  const clave = modo === "registro" ? "registro" : vista === "recuperar" ? "recuperar" : "login";
  const textos = TEXTOS[clave];
  const titulo = useEscribir(textos.titulo, 80);

  const cambiarModo = (nuevo) => {
    setModo(nuevo);
    setVista("login");
    setAviso("");
    setError("");
    setPassword("");
    setVerPassword(false);
  };

  const enviarLogin = async (e) => {
    e.preventDefault();
    setError("");
    setCargando(true);
    try {
      const datos = await login(email, password, recordar);
      if (String(datos.rol).toLowerCase() === "admin") {
        // El administrador usa su propio panel (solo habla con Django)
        window.location.href = `${import.meta.env.VITE_ADMIN_URL}?email=${encodeURIComponent(datos.email)}`;
        return;
      }
      navegar(ubicacion.state?.desde || "/traducir", { replace: true });
    } catch (err) {
      setError(err.message);
    } finally {
      setCargando(false);
    }
  };

  const enviarRegistro = async (e) => {
    e.preventDefault();
    setError("");
    setCargando(true);
    try {
      await registro(nombre, email, password);
      navegar("/traducir", { replace: true });
    } catch (err) {
      setError(err.message);
    } finally {
      setCargando(false);
    }
  };

  const irRecuperar = (ver) => {
    setVista(ver ? "recuperar" : "login");
    setError("");
    setAviso("");
  };

  const enviarRecuperacion = (e) => {
    e.preventDefault();
    // Todavía no existe el servicio de correo en el backend: se avisa con honestidad.
    setAviso("La recuperación por correo todavía no está disponible. Por ahora, pide ayuda al administrador.");
  };

  const campoPassword = (id, autoComplete, placeholder, extra = {}) => (
    <span className="acceso-pass">
      <input
        id={id}
        type={verPassword ? "text" : "password"}
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        placeholder={placeholder}
        autoComplete={autoComplete}
        required
        {...extra}
      />
      <button
        type="button"
        className="acceso-ojo"
        onClick={() => setVerPassword(!verPassword)}
        aria-label={verPassword ? "Ocultar contraseña" : "Mostrar contraseña"}
      >
        <IconoOjo visible={verPassword} />
      </button>
    </span>
  );

  return (
    <div className="acceso">
      <div className={`acceso-tarjeta modo-${modo}`}>
        {/* Registro (izquierda) */}
        <form className={`acceso-form acceso-form-registro ${modo === "registro" ? "activo" : ""}`} onSubmit={enviarRegistro}>
          <div className="acceso-logo-movil"><img src="/logo.png" alt="" /> <span>Señas<b>App</b></span></div>
          <h1>Crea tu cuenta</h1>
          <p className="acceso-sub">Completa tus datos para empezar.</p>

          <label>
            Nombre
            <input value={nombre} onChange={(e) => setNombre(e.target.value)} placeholder="Tu nombre"
              maxLength={50} required autoComplete="name" />
          </label>
          <label>
            Correo
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)}
              placeholder="nombre@correo.com" required autoComplete="email" />
          </label>
          <label>
            Contraseña
            {modo === "registro" && campoPassword("pass-registro", "new-password", "Entre 6 y 30 caracteres", { minLength: 6, maxLength: 30 })}
          </label>
          <label className="acceso-check">
            <input type="checkbox" checked={acepto} onChange={(e) => setAcepto(e.target.checked)} required />
            <span>Acepto los <strong className="acceso-enlace">términos y condiciones</strong> y la <strong className="acceso-enlace">política de privacidad</strong></span>
          </label>

          {modo === "registro" && error && <p className="acceso-error" role="alert">{error}</p>}

          <button className="acceso-boton" disabled={cargando}>
            {cargando ? "Creando cuenta..." : "Registrarse"}
          </button>
          <p className="acceso-pie">
            ¿Ya tienes cuenta? <button type="button" onClick={() => cambiarModo("login")}>Iniciar sesión</button>
          </p>
        </form>

        {/* Inicio de sesión / recuperar (derecha) */}
        {vista === "login" ? (
          <form className={`acceso-form acceso-form-login ${modo === "login" ? "activo" : ""}`} onSubmit={enviarLogin}>
            <div className="acceso-logo-movil"><img src="/logo.png" alt="" /> <span>Señas<b>App</b></span></div>
            <h1>Iniciar sesión</h1>
            <p className="acceso-sub">Ingresa con tu cuenta.</p>

            <label>
              Correo
              <input type="email" value={email} onChange={(e) => setEmail(e.target.value)}
                placeholder="nombre@correo.com" required autoComplete="email" />
            </label>
            <label>
              Contraseña
              {modo === "login" && campoPassword("pass-login", "current-password", "••••••••")}
            </label>

            <div className="acceso-opciones">
              <label className="acceso-check acceso-recordar">
                <input type="checkbox" checked={recordar} onChange={(e) => setRecordar(e.target.checked)} />
                <span>Recordarme</span>
              </label>
              <button type="button" className="acceso-enlace-boton" onClick={() => irRecuperar(true)}>
                ¿Olvidaste tu contraseña?
              </button>
            </div>

            {modo === "login" && error && <p className="acceso-error" role="alert">{error}</p>}

            <button className="acceso-boton" disabled={cargando}>
              {cargando ? "Ingresando..." : "Iniciar sesión"}
            </button>
            <p className="acceso-pie">
              ¿No tienes cuenta? <button type="button" onClick={() => cambiarModo("registro")}>Regístrate</button>
            </p>
          </form>
        ) : (
          <form className="acceso-form acceso-form-login activo" onSubmit={enviarRecuperacion}>
            <div className="acceso-logo-movil"><img src="/logo.png" alt="" /> <span>Señas<b>App</b></span></div>
            <h1>Recupera tu acceso</h1>
            <p className="acceso-sub">Escribe el correo de tu cuenta.</p>

            <label>
              Correo
              <input type="email" value={email} onChange={(e) => setEmail(e.target.value)}
                placeholder="nombre@correo.com" required autoComplete="email" />
            </label>

            {aviso && <p className="acceso-aviso" role="status">{aviso}</p>}

            <button className="acceso-boton">Enviar enlace</button>
            <p className="acceso-pie">
              <button type="button" onClick={() => irRecuperar(false)}>Volver a iniciar sesión</button>
            </p>
          </form>
        )}

        {/* Panel deslizante con la foto */}
        <aside className="acceso-panel" aria-hidden="true">
          <img className="acceso-foto" src="/acceso-foto.jpg" alt="" />
          <span className="acceso-velo" />
          <div className="acceso-marca">
            <img src="/logo.png" alt="" />
            <span>Señas<b>App</b></span>
          </div>
          <div className="acceso-mensaje">
            <span className="acceso-barra" />
            <h2>{titulo}</h2>
            <p>{textos.detalle}</p>
          </div>
        </aside>
      </div>
    </div>
  );
}