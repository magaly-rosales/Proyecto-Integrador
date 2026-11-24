import { Link } from "react-router-dom";
import { useAuth } from "../context/useAuth";
import Logo from "./Logo";
import { IconoCamara } from "./Iconos";

export default function Footer() {
  const { usuario } = useAuth();

  return (
    <footer className="sa-pie">
      <div className="sa-pie-in">
        <div className="sa-pie-grid">
          <div className="sa-pie-marca">
            <Link to="/" className="sa-logo sa-logo-pie" aria-label="SeñasApp, ir al inicio">
              <Logo tamano={96} />
              <span>
                Señas<b>App</b>
              </span>
            </Link>
            <p className="sa-pie-frase">
              Traduce la Lengua de Señas Peruana a texto y voz en tiempo real, desde tu cámara.
            </p>
            <p className="sa-pie-hecho">Hecho con <i aria-hidden="true">♥</i> para una comunicación sin barreras</p>
          </div>

          <div className="sa-pie-col">
            <h4>Explorar</h4>
            {usuario ? (
              <>
                <Link to="/traducir">Traducir</Link>
                <Link to="/senas">Señas disponibles</Link>
                <Link to="/traducir#guia">Cómo usar</Link>
                <Link to="/configuracion">Configuración</Link>
              </>
            ) : (
              <>
                <Link to="/">Iniciar sesión</Link>
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

          <div className="sa-pie-cta">
            <h4>¿Listo para traducir?</h4>
            <p>Apunta la cámara a quien está señando y lee o escucha lo que dice.</p>
            <Link to="/traducir" className="sa-pie-boton">
              <IconoCamara tamano={20} /> Abrir cámara
            </Link>
          </div>
        </div>

        <div className="sa-pie-base">
          <p>© 2026 SeñasApp · Proyecto integrador, TECSUP</p>
        </div>
      </div>
    </footer>
  );
}
