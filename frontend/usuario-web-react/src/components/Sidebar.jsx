// src/components/Sidebar.jsx
import Logo from './Logo';
import './Sidebar.css';

export default function Sidebar() {
  return (
    <aside className="sidebar-container">
      {/* Logo corporativo en la parte superior del menú */}
      <div className="sidebar-logo-wrapper">
        <Logo />
      </div>

      {/* Opciones de navegación */}
      <nav className="sidebar-nav">
        <a href="#inicio" className="nav-item">
          <span className="nav-icon">🏠</span> Inicio
        </a>
        <a href="#traducir" className="nav-item activo">
          <span className="nav-icon">📷</span> Traducir
        </a>
        <a href="#diccionario" className="nav-item">
          <span className="nav-icon">📖</span> Diccionario
        </a>
        <a href="#historial" className="nav-item">
          <span className="nav-icon">⏱️</span> Historial
        </a>
        <a href="#perfil" className="nav-item">
          <span className="nav-icon">👤</span> Perfil
        </a>
      </nav>

      {/* Tarjeta de SeñasApp Premium en la parte inferior */}
      <div className="sidebar-premium-card">
        <div className="premium-icon">👑</div>
        <div className="premium-content">
          <h4>SeñasApp Premium</h4>
          <p>Accede a funciones exclusivas y una mejor experiencia.</p>
          <button className="btn-premium">Conocer más</button>
        </div>
      </div>
    </aside>
  );
}