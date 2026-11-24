import { useState, useEffect, useCallback } from 'react';
import {
    getSenas,
    deleteSena,
    updateSena,
    getUsuarios,
    toggleEstadoUsuario,
    getEstadisticasUso,
    getRegistroUso
} from '../services/senasService';
import '../estilos/GestionSenas.css';
import { Entrenamiento } from './Entrenamiento';

export const GestionSenas = ({ onLogout }) => {
    const [senas, setSenas] = useState([]);
    const [usuarios, setUsuarios] = useState([]);
    const [estadisticas, setEstadisticas] = useState({ total_traducciones: 0, top_senas: [] });
    const [registrosUso, setRegistrosUso] = useState([]);

    const [nombre, setNombre] = useState('');
    const [categoria, setCategoria] = useState('');
    const [videoFile, setVideoFile] = useState(null);
    const [loading, setLoading] = useState(false);
    const [editando, setEditando] = useState(null);

    // Navegación de secciones (senas / usuarios / dashboard / entrenamiento)
    const [seccionActiva, setSeccionActiva] = useState('senas');

    const cargarSenas = useCallback(async () => {
        setLoading(true);
        try {
            const response = await getSenas();
            setSenas(response.data);
        } catch (error) {
            console.error("Error al cargar señas:", error);
        } finally {
            setLoading(false);
        }
    }, []);

    const cargarUsuarios = useCallback(async () => {
        try {
            const response = await getUsuarios();
            setUsuarios(response.data);
        } catch (error) {
            console.error("Error al cargar usuarios:", error);
        }
    }, []);

    const cargarEstadisticas = useCallback(async () => {
        try {
            const resStats = await getEstadisticasUso();
            const resLogs = await getRegistroUso();
            setEstadisticas(resStats.data);
            setRegistrosUso(resLogs.data);
        } catch (error) {
            console.error("Error al cargar estadísticas de uso:", error);
        }
    }, []);

    useEffect(() => {
        // eslint-disable-next-line react-hooks/set-state-in-effect
        cargarSenas();
        cargarUsuarios();
        cargarEstadisticas();
    }, [cargarSenas, cargarUsuarios, cargarEstadisticas]);

    const cancelarEdicion = () => {
        setEditando(null);
        setNombre('');
        setCategoria('');
        setVideoFile(null);
        const inputVideo = document.getElementById('video-input');
        if (inputVideo) inputVideo.value = '';
    };

    const empezarEdicion = (sena) => {
        setEditando(sena);
        setNombre(sena.nombre);
        setCategoria(sena.categoria);
        setVideoFile(null);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!editando || !nombre.trim() || !categoria.trim()) return;

        const formData = new FormData();
        formData.append('nombre', nombre);
        formData.append('categoria', categoria);
        if (videoFile) formData.append('video', videoFile);

        try {
            await updateSena(editando.id, formData);
            cancelarEdicion();
            cargarSenas();
        } catch (error) {
            console.error("Error al editar la seña:", error);
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm('¿Estás seguro de eliminar esta seña?')) {
            try {
                await deleteSena(id);
                if (editando?.id === id) cancelarEdicion();
                cargarSenas();
            } catch (error) {
                console.error("Error al eliminar la seña:", error);
            }
        }
    };

    const handleToggleUsuario = async (id, estadoActual) => {
        try {
            await toggleEstadoUsuario(id, !estadoActual);
            cargarUsuarios();
        } catch (error) {
            console.error("Error al cambiar estado del usuario:", error);
        }
    };

    const navStyle = { background: 'transparent', border: 'none', cursor: 'pointer', textAlign: 'left', width: '100%' };

    return (
        <div className="admin-layout">
            {/* Sidebar Lateral */}
            <aside className="admin-sidebar">
                <div className="brand">
                    <span className="brand-icon">🤟</span>
                    <h2>LSB Admin</h2>
                </div>
                <nav className="admin-nav">
                    <button
                        onClick={() => setSeccionActiva('senas')}
                        className={`nav-item ${seccionActiva === 'senas' ? 'active' : ''}`}
                        style={navStyle}
                    >
                        📁 Gestión de Señas
                    </button>
                    <button
                        onClick={() => setSeccionActiva('usuarios')}
                        className={`nav-item ${seccionActiva === 'usuarios' ? 'active' : ''}`}
                        style={navStyle}
                    >
                        👥 Gestión de Usuarios
                    </button>
                    <button
                        onClick={() => setSeccionActiva('dashboard')}
                        className={`nav-item ${seccionActiva === 'dashboard' ? 'active' : ''}`}
                        style={navStyle}
                    >
                        📊 Estadísticas de Uso
                    </button>
                    <button
                        onClick={() => setSeccionActiva('entrenamiento')}
                        className={`nav-item ${seccionActiva === 'entrenamiento' ? 'active' : ''}`}
                        style={navStyle}
                    >
                        🎥 Entrenamiento
                    </button>
                </nav>
                <div className="system-status" style={{ display: 'flex', flexDirection: 'column', gap: '0.6rem' }}>
                    <div><span className="status-dot"></span> Backend Django Online</div>
                    {onLogout && (
                        <button
                            onClick={onLogout}
                            style={{
                                background: '#dc2626',
                                color: '#ffffff',
                                border: 'none',
                                padding: '0.5rem 0.8rem',
                                borderRadius: '6px',
                                cursor: 'pointer',
                                fontWeight: 'bold',
                                fontSize: '13px',
                                width: '100%'
                            }}
                        >
                            🚪 Cerrar Sesión
                        </button>
                    )}
                </div>
            </aside>

            {/* Contenido Principal */}
            <main className="admin-main">
                <header className="admin-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                        <h1 className="admin-title">
                            {seccionActiva === 'senas' && 'Módulo de Señas LSB'}
                            {seccionActiva === 'usuarios' && 'Gestión de Usuarios & Cuentas'}
                            {seccionActiva === 'dashboard' && 'Dashboard de Estadísticas (RegistroUso)'}
                            {seccionActiva === 'entrenamiento' && 'Entrenamiento del reconocimiento'}
                        </h1>
                        <p className="admin-subtitle">
                            {seccionActiva === 'senas' && 'Edita o elimina las señas y sus videos de referencia'}
                            {seccionActiva === 'usuarios' && 'Monitorea y activa/desactiva cuentas de usuarios en el sistema'}
                            {seccionActiva === 'dashboard' && 'Métricas de traducciones realizadas y ranking de señas más consultadas'}
                            {seccionActiva === 'entrenamiento' && 'Crea señas y graba ejemplos, con y sin movimiento, para que la app las reconozca'}
                        </p>
                    </div>
                </header>

                {/* KPIs */}
                <div className="kpi-grid">
                    <div className="kpi-card">
                        <span className="kpi-number">{senas.length}</span>
                        <span className="kpi-label">Señas Registradas</span>
                    </div>
                    <div className="kpi-card">
                        <span className="kpi-number">{estadisticas.total_traducciones}</span>
                        <span className="kpi-label">Traducciones Totales</span>
                    </div>
                    <div className="kpi-card">
                        <span className="kpi-number">
                            {usuarios.filter(u => u.is_active).length} / {usuarios.length}
                        </span>
                        <span className="kpi-label">Usuarios Activos</span>
                    </div>
                </div>

                {seccionActiva === 'senas' && (
                    <div className="content-grid">
                        <section className="admin-card">
                            {!editando ? (
                                <>
                                    <h3 className="card-title">ℹ️ Cómo agregar una seña</h3>
                                    <p className="td-muted" style={{ marginTop: '1rem' }}>
                                        Las señas nuevas se crean en <strong>🎥 Entrenamiento</strong> (botón «➕ Nueva seña»),
                                        donde también se graban sus muestras. Aquí puedes editarlas o eliminarlas.
                                    </p>
                                </>
                            ) : (
                                <>
                                    <h3 className="card-title">✏️ Editar seña #{editando.id}</h3>
                                    <form onSubmit={handleSubmit} className="admin-form">
                                        <div className="form-group">
                                            <label>Nombre de la Seña</label>
                                            <input
                                                type="text"
                                                value={nombre}
                                                onChange={(e) => setNombre(e.target.value)}
                                                className="admin-input"
                                                required
                                            />
                                        </div>
                                        <div className="form-group">
                                            <label>Categoría</label>
                                            <input
                                                type="text"
                                                value={categoria}
                                                onChange={(e) => setCategoria(e.target.value)}
                                                className="admin-input"
                                                required
                                            />
                                        </div>
                                        <div className="form-group">
                                            <label>Cambiar video MP4 (opcional)</label>
                                            <input
                                                id="video-input"
                                                type="file"
                                                accept="video/mp4,video/*"
                                                onChange={(e) => setVideoFile(e.target.files[0])}
                                                className="admin-input"
                                            />
                                        </div>
                                        <div style={{ display: 'flex', gap: '0.5rem' }}>
                                            <button type="submit" className="btn-primary">Guardar cambios</button>
                                            <button type="button" className="btn-refresh" onClick={cancelarEdicion}>Cancelar</button>
                                        </div>
                                    </form>
                                </>
                            )}
                        </section>

                        <section className="admin-card">
                            <div className="card-header">
                                <h3 className="card-title">📋 Listado de Señas</h3>
                                <button onClick={cargarSenas} className="btn-refresh">🔄 Actualizar</button>
                            </div>
                            {loading ? (
                                <p style={{ color: '#94a3b8' }}>Cargando datos...</p>
                            ) : (
                                <div className="table-wrapper">
                                    <table className="admin-table">
                                        <thead>
                                            <tr>
                                                <th>ID</th>
                                                <th>Nombre</th>
                                                <th>Categoría</th>
                                                <th>Video</th>
                                                <th style={{ textAlign: 'center' }}>Acciones</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {senas.map((sena) => (
                                                <tr key={sena.id}>
                                                    <td className="td-muted">#{sena.id}</td>
                                                    <td className="td-bold">{sena.nombre}</td>
                                                    <td><span className="badge">{sena.categoria}</span></td>
                                                    <td>
                                                        {sena.video ? (
                                                            <video src={sena.video} width="110" height="70" controls style={{ borderRadius: '6px' }} />
                                                        ) : (
                                                            <span style={{ color: '#64748b', fontSize: '12px' }}>Sin video</span>
                                                        )}
                                                    </td>
                                                    <td style={{ textAlign: 'center' }}>
                                                        <button
                                                            onClick={() => empezarEdicion(sena)}
                                                            className="btn-primary"
                                                            style={{ marginRight: '0.4rem' }}
                                                        >
                                                            ✏️ Editar
                                                        </button>
                                                        <button onClick={() => handleDelete(sena.id)} className="btn-danger">🗑️ Eliminar</button>
                                                    </td>
                                                </tr>
                                            ))}
                                        </tbody>
                                    </table>
                                </div>
                            )}
                        </section>
                    </div>
                )}

                {seccionActiva === 'usuarios' && (
                    <section className="admin-card">
                        <div className="card-header">
                            <h3 className="card-title">👥 Listado de Usuarios Registrados</h3>
                            <button onClick={cargarUsuarios} className="btn-refresh">🔄 Actualizar</button>
                        </div>

                        <div className="table-wrapper">
                            <table className="admin-table">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Usuario</th>
                                        <th>Email</th>
                                        <th>Rol</th>
                                        <th>Estado</th>
                                        <th style={{ textAlign: 'center' }}>Acción</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {usuarios.map((usr) => (
                                        <tr key={usr.id}>
                                            <td className="td-muted">#{usr.id}</td>
                                            <td className="td-bold">{usr.username}</td>
                                            <td>{usr.email || 'Sin correo'}</td>
                                            <td>
                                                <span className="badge" style={{ background: usr.is_staff ? '#6366f1' : '#334155' }}>
                                                    {usr.is_staff ? 'Admin' : 'Usuario'}
                                                </span>
                                            </td>
                                            <td>
                                                <span style={{ color: usr.is_active ? '#4ade80' : '#f87171', fontWeight: '600' }}>
                                                    {usr.is_active ? 'Activo' : 'Inactivo'}
                                                </span>
                                            </td>
                                            <td style={{ textAlign: 'center' }}>
                                                <button
                                                    onClick={() => handleToggleUsuario(usr.id, usr.is_active)}
                                                    className={usr.is_active ? 'btn-danger' : 'btn-primary'}
                                                    style={{ fontSize: '12px', padding: '0.4rem 0.8rem' }}
                                                >
                                                    {usr.is_active ? '🚫 Desactivar' : '✅ Activar'}
                                                </button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    </section>
                )}

                {seccionActiva === 'dashboard' && (
                    <div className="content-grid">
                        {/* Top Señas Consultadas */}
                        <section className="admin-card">
                            <div className="card-header">
                                <h3 className="card-title">🏆 Señas Más Consultadas (Top 5)</h3>
                                <button onClick={cargarEstadisticas} className="btn-refresh">🔄 Actualizar</button>
                            </div>
                            <div className="table-wrapper">
                                <table className="admin-table">
                                    <thead>
                                        <tr>
                                            <th>Seña Consultada</th>
                                            <th style={{ textAlign: 'center' }}>Total Consultas</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {estadisticas.top_senas.length === 0 ? (
                                            <tr>
                                                <td colSpan="2" style={{ textAlign: 'center', color: '#64748b' }}>
                                                    No hay consultas registradas aún.
                                                </td>
                                            </tr>
                                        ) : (
                                            estadisticas.top_senas.map((item, idx) => (
                                                <tr key={idx}>
                                                    <td className="td-bold">⭐ {item.nombre_sena_consultada}</td>
                                                    <td style={{ textAlign: 'center' }}>
                                                        <span className="badge" style={{ background: '#3b82f6', fontSize: '14px' }}>
                                                            {item.total}
                                                        </span>
                                                    </td>
                                                </tr>
                                            ))
                                        )}
                                    </tbody>
                                </table>
                            </div>
                        </section>

                        {/* Registro de Uso Reciente */}
                        <section className="admin-card">
                            <div className="card-header">
                                <h3 className="card-title">📜 Historial de Uso (RegistroUso)</h3>
                                <button onClick={cargarEstadisticas} className="btn-refresh">🔄 Actualizar</button>
                            </div>
                            <div className="table-wrapper">
                                <table className="admin-table">
                                    <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>Seña</th>
                                            <th>Fecha y Hora</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {registrosUso.length === 0 ? (
                                            <tr>
                                                <td colSpan="3" style={{ textAlign: 'center', color: '#64748b' }}>
                                                    Sin registros de uso.
                                                </td>
                                            </tr>
                                        ) : (
                                            registrosUso.map((reg) => (
                                                <tr key={reg.id}>
                                                    <td className="td-muted">#{reg.id}</td>
                                                    <td className="td-bold">{reg.nombre_sena_consultada}</td>
                                                    <td style={{ color: '#94a3b8', fontSize: '12px' }}>
                                                        {new Date(reg.fecha_consulta).toLocaleString()}
                                                    </td>
                                                </tr>
                                            ))
                                        )}
                                    </tbody>
                                </table>
                            </div>
                        </section>
                    </div>
                )}

                {seccionActiva === 'entrenamiento' && <Entrenamiento onSenaCreada={cargarSenas} />}
            </main>
        </div>
    );
};