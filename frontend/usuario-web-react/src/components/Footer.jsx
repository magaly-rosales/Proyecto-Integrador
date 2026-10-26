import { Link } from "react-router-dom";
import { useAuth } from "../context/useAuth";
import Logo from "./Logo";

export default function Footer() {
  const { usuario } = useAuth();

  return (
    <footer className="sa-pie">
      <div className="sa-pie-in">
        <div className="sa-pie-grid">
          <div className="sa-pie-marca">
            <Link to="/" className="sa-logo sa-logo-pie" aria-label="SeñasApp, ir al inicio">
              <Logo tamano={80} />
              <span>
                Señas<b>App</b>
              </span>
            </Link>
            <p className="sa-pie-frase">Cuando las manos hablan, SeñasApp escucha.</p>
          </div>

          <div className="sa-pie-col">
            <h4>Explorar</h4>
            <Link to="/">Inicio</Link>
            <Link to="/traducir">Traducir</Link>
            {usuario ? (
              <Link to="/configuracion">Configuración</Link>
            ) : (
              <>
                <Link to="/login">Iniciar sesión</Link>
                <Link to="/registro">Registrarse</Link>
              </>
            )}
          </div>

          <div className="sa-pie-col">
            <h4>Privacidad</h4>
            <span>Tu video nunca se guarda.</span>
            <span>Solo se analizan los puntos de la mano.</span>
            <span>Funciona desde el navegador.</span>
          </div>      
        </div>

        <div className="sa-pie-base">
          <p>© 2026 SeñasApp · Proyecto integrador, TECSUP</p>
          
        </div>
      </div>
    </footer>
  );
}