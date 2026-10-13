// src/components/Logo.jsx
import './Logo.css';

export default function Logo() {
  return (
    <div className="logo-container">
      {/* Contenedor del ícono con el fondo morado suave del prototipo */}
      <div className="logo-icon-wrapper">
        <svg
          width="24"
          height="24"
          viewBox="0 0 24 24"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            d="M12 21C15.866 21 19 17.866 19 14V8C19 6.89543 18.1046 6 17 6C15.8954 6 15 6.89543 15 8V11H13V4C13 2.89543 12.1046 2 11 2C9.89543 2 9 2.89543 9 4V11H7V5C7 3.89543 6.10457 3 5 3C3.89543 3 3 3.89543 3 5V14C3 17.866 6.13401 21 10 21H12Z"
            fill="#5A4BFA"
          />
        </svg>
      </div>

      {/* Grupo de texto de la marca */}
      <div className="logo-text-group">
        <span className="logo-title">SeñasApp</span>
        <span className="logo-subtitle">Lenguaje de señas, sin barreras</span>
      </div>
    </div>
  );
}