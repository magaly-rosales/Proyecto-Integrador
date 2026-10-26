import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/useAuth";
import Logo from "../components/Logo";

export default function Registro() {
  const { registro } = useAuth();
  const navegar = useNavigate();
  const [nombre, setNombre] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [cargando, setCargando] = useState(false);

  const enviar = async (e) => {
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

  return (
    <section className="auth">
      <form className="tarjeta auth-tarjeta" onSubmit={enviar}>
        <div className="auth-logo" aria-hidden="true"><Logo tamano={110} /></div>
        <h1>Crea tu cuenta</h1>
        <p className="auth-sub">Es gratis y toma menos de un minuto.</p>

        <label className="campo">
          Nombre
          <input value={nombre} onChange={(e) => setNombre(e.target.value)}
            placeholder="Tu nombre" maxLength={50} required autoComplete="name" />
        </label>
        <label className="campo">
          Correo
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)}
            placeholder="tucorreo@ejemplo.com" required autoComplete="email" />
        </label>
        <label className="campo">
          Contraseña
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)}
            placeholder="Entre 6 y 30 caracteres" minLength={6} maxLength={30} required autoComplete="new-password" />
        </label>

        {error && <p className="auth-error" role="alert">{error}</p>}

        <button className="boton auth-boton" disabled={cargando}>
          {cargando ? "Creando cuenta..." : "Registrarme"}
        </button>

        <p className="auth-pie">
          ¿Ya tienes cuenta? <Link to="/login">Inicia sesión</Link>
        </p>
      </form>
    </section>
  );
}
