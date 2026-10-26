import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../context/useAuth";
import Logo from "../components/Logo";

export default function Login() {
  const { login } = useAuth();
  const navegar = useNavigate();
  const ubicacion = useLocation();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [cargando, setCargando] = useState(false);
  const [verPassword, setVerPassword] = useState(false);

  const enviar = async (e) => {
    e.preventDefault();
    setError("");
    setCargando(true);
    try {
      await login(email, password);
      navegar(ubicacion.state?.desde || "/traducir", { replace: true });
    } catch (err) {
      setError(err.message);
    } finally {
      setCargando(false);
    }
  };

  return (
    <section className="auth">
      <form className="tarjeta auth-tarjeta" onSubmit={enviar}>
        <div className="auth-logo" aria-hidden="true"><Logo tamano={110} /></div>
        <h1>Bienvenido de nuevo</h1>
        <p className="auth-sub">Inicia sesión para traducir señas.</p>

        <label className="campo">
          Correo
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)}
            placeholder="tucorreo@ejemplo.com" required autoComplete="email" />
        </label>
        <label className="campo">
          Contraseña
          <div className="campo-password">
            <input
              type={verPassword ? "text" : "password"}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Tu contraseña"
              required
              autoComplete="current-password"
            />
            <button
              type="button"
              className="ojo"
              onClick={() => setVerPassword(!verPassword)}
              aria-label={verPassword ? "Ocultar contraseña" : "Mostrar contraseña"}
              title={verPassword ? "Ocultar contraseña" : "Mostrar contraseña"}
            >
              {verPassword ? (
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M17.9 17.9A10.6 10.6 0 0 1 12 20C5 20 1 12 1 12a18.5 18.5 0 0 1 5.1-5.9M9.9 4.2A9.8 9.8 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.2 3.2M1 1l22 22" /></svg>
              ) : (
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M1 12S5 4 12 4s11 8 11 8-4 8-11 8S1 12 1 12z" /><circle cx="12" cy="12" r="3" /></svg>
              )}
            </button>
          </div>
        </label>

        {error && <p className="auth-error" role="alert">{error}</p>}

        <button className="boton auth-boton" disabled={cargando}>
          {cargando ? "Ingresando..." : "Iniciar sesión"}
        </button>

        <p className="auth-pie">
          ¿No tienes cuenta? <Link to="/registro">Regístrate</Link>
        </p>
        <p className="auth-pie">
          <a href={import.meta.env.VITE_ADMIN_URL}>Ingresar como administrador</a>
        </p>
      </form>
    </section>
  );
}
