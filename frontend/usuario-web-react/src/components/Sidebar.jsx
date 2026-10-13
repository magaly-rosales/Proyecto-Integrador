// src/components/Sidebar.jsx
import { NavLink } from "react-router-dom";
import Logo from './Logo';
import './Sidebar.css';

export default function Sidebar() {
  const claseItem = ({ isActive }) => (isActive ? "nav-item activo" : "nav-item");

  return (
    <aside className="sidebar-container">
      <div className="sidebar-logo-wrapper">
        <Logo />
      </div>

      <nav className="sidebar-nav">
        <NavLink to="/inicio" className={claseItem}>
          <span className="nav-icon">🏠</span> Inicio
        </NavLink>
        <NavLink to="/reconocer" className={claseItem}>
          <span className="nav-icon">📷</span> Traducir
        </NavLink>
        <NavLink to="/entrenamiento" className={claseItem}>
          <span className="nav-icon">🏋️‍♂️</span> Entrenamiento
        </NavLink>
        <NavLink to="/diccionario" className={claseItem}>
          <span className="nav-icon">📖</span> Diccionario
        </NavLink>
        <NavLink to="/historial" className={claseItem}>
          <span className="nav-icon">⏱️</span> Historial
        </NavLink>
        <NavLink to="/perfil" className={claseItem}>
          <span className="nav-icon">👤</span> Perfil
        </NavLink>
      </nav>

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