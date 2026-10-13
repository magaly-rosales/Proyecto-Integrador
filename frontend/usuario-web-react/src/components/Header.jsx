// src/components/Header.jsx
import './Header.css';

export default function Header() {
  return (
    <header className="header-container">
      {/* Barra de búsqueda central */}
      <div className="header-search-wrapper">
        <span className="search-icon">🔍</span>
        <input
          type="text"
          placeholder="Buscar una seña, categoría o palabra..."
          className="header-search-input"
        />
      </div>

      {/* Zona de usuario y notificaciones */}
      <div className="header-user-section">
        <button className="header-notification-btn" title="Notificaciones">
          🔔
          <span className="notification-badge"></span>
        </button>

        <div className="header-user-profile">
          <div className="user-avatar">AM</div>
          <span className="user-name">Ana Merino</span>
          <span className="user-arrow">▼</span>
        </div>
      </div>
    </header>
  );
}