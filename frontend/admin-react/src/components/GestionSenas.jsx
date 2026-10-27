import { useState, useEffect, useCallback } from 'react';
import {
    getSenas,
    createSena,
    deleteSena,
    predecirSena,
    getUsuarios,
    toggleEstadoUsuario
} from '../services/senasService';
import './GestionSenas.css';

export const GestionSenas = () => {
    const [senas, setSenas] = useState([]);
    const [usuarios, setUsuarios] = useState([]);
    const [nombre, setNombre] = useState('');
    const [categoria, setCategoria] = useState('');
    const [videoFile, setVideoFile] = useState(null);
    const [loading, setLoading] = useState(false);

    // Navegación de secciones (senas / usuarios)
    const [seccionActiva, setSeccionActiva] = useState('senas');

    // Estados para IA
    const [resultadoIA, setResultadoIA] = useState(null);
    const [probandoIA, setProbandoIA] = useState(false);

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

    useEffect(() => {
        // eslint-disable-next-line react-hooks/set-state-in-effect
        cargarSenas();
        cargarUsuarios();
    }, [cargarSenas, cargarUsuarios]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!nombre.trim() || !categoria.trim()) return;

        const formData = new FormData();
        formData.append('nombre', nombre);
        formData.append('categoria', categoria);
        if (videoFile) {
            formData.append('video', videoFile);
        }

        try {
            await createSena(formData);
            setNombre('');
            setCategoria('');
            setVideoFile(null);
            const inputVideo = document.getElementById('video-input');
            if (inputVideo) inputVideo.value = '';
            cargarSenas();
        } catch (error) {
            console.error("Error al guardar la seña:", error);
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm('¿Estás seguro de eliminar esta seña?')) {
            try {
                await deleteSena(id);
                cargarSenas();
            } catch (error) {
                console.error("Error al eliminar la seña:", error);
            }
        }
    };

    const handleProbarIA = async () => {
        setProbandoIA(true);
        try {
            const mockLandmarks = Array.from({ length: 21 }, (_, i) => ({
                x: parseFloat((0.1 + i * 0.02).toFixed(3)),
                y: parseFloat((0.2 + i * 0.01).toFixed(3)),
                z: 0.005
            }));

            const response = await predecirSena(mockLandmarks);
            setResultadoIA(response.data.data);
        } catch (error) {
            console.error("Error al ejecutar inferencia:", error);
        } finally {
            setProbandoIA(false);
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
                        style={{ background: 'transparent', border: 'none', cursor: 'pointer', textAlign: 'left', width: '100%' }}
                    >
                        📁 Gestión de Señas
                    </button>
                    <button
                        onClick={() => setSeccionActiva('usuarios')}
                        className={`nav-item ${seccionActiva === 'usuarios' ? 'active' : ''}`}
                        style={{ background: 'transparent', border: 'none', cursor: 'pointer', textAlign: 'left', width: '100%' }}
                    >
                        👥 Gestión de Usuarios
                    </button>
                </nav>
                <div className="system-status">
                    <span className="status-dot"></span> Backend Django Online
                </div>
            </aside>

            {/* Contenido Principal */}
            <main className="admin-main">
                <header className="admin-header">
                    <h1 className="admin-title">
                        {seccionActiva === 'senas' ? 'Módulo de Señas LSB' : 'Gestión de Usuarios & Cuentas'}
                    </h1>
                    <p className="admin-subtitle">
                        {seccionActiva === 'senas'
                            ? 'Administra las señas, videos e inferencia IA'
                            : 'Monitorea y activa/desactiva cuentas de usuarios en el sistema'}
                    </p>
                </header>

                {/* KPIs */}
                <div className="kpi-grid">
                    <div className="kpi-card">
                        <span className="kpi-number">{senas.length}</span>
                        <span className="kpi-label">Señas Registradas</span>
                    </div>
                    <div className="kpi-card">
                        <span className="kpi-number">{usuarios.length}</span>
                        <span className="kpi-label">Usuarios Totales</span>
                    </div>
                    <div className="kpi-card">
                        <span className="kpi-number">
                            {usuarios.filter(u => u.is_active).length}
                        </span>
                        <span className="kpi-label">Cuentas Activas</span>
                    </div>
                </div>

                {seccionActiva === 'senas' ? (
                    <>
                        {/* Panel IA */}
                        <section className="admin-card" style={{ marginBottom: '1.5rem' }}>
                            <div className="card-header">
                                <h3 className="card-title">🧠 Inferencia IA / MediaPipe</h3>
                                <button onClick={handleProbarIA} className="btn-primary" disabled={probandoIA}>
                                    {probandoIA ? 'Procesando...' : '⚡ Probar Normalización'}
                                </button>
                            </div>
                            {resultadoIA && (
                                <div style={{ marginTop: '1rem', padding: '1rem', background: '#0f172a', borderRadius: '8px', border: '1px solid #334155' }}>
                                    <p style={{ color: '#38bdf8', margin: '0 0 0.5rem 0', fontWeight: 'bold' }}>✨ Resultado:</p>
                                    <div style={{ display: 'flex', gap: '1.5rem', color: '#f8fafc', fontSize: '14px' }}>
                                        <span><strong>Seña:</strong> {resultadoIA.sena_detectada}</span>
                                        <span><strong>Confianza:</strong> {(resultadoIA.confianza * 100).toFixed(1)}%</span>
                                    </div>
                                </div>
                            )}
                        </section>

                        {/* Formulario + Tabla */}
                        <div className="content-grid">
                            <section className="admin-card">
                                <h3 className="card-title">➕ Registrar Seña</h3>
                                <form onSubmit={handleSubmit} className="admin-form">
                                    <div className="form-group">
                                        <label>Nombre de la Seña</label>
                                        <input
                                            type="text"
                                            placeholder="Ej: Hola, Gracias"
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
                                            placeholder="Ej: Saludos"
                                            value={categoria}
                                            onChange={(e) => setCategoria(e.target.value)}
                                            className="admin-input"
                                            required
                                        />
                                    </div>
                                    <div className="form-group">
                                        <label>Video MP4 (máx 50MB)</label>
                                        <input
                                            id="video-input"
                                            type="file"
                                            accept="video/mp4,video/*"
                                            onChange={(e) => setVideoFile(e.target.files[0])}
                                            className="admin-input"
                                        />
                                    </div>
                                    <button type="submit" className="btn-primary">Guardar en BD</button>
                                </form>
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
                    </>
                ) : (
                    /* Vista de Gestión de Usuarios */
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
                                {usuarios.length === 0 ? (
                                    <tr>
                                        <td colSpan="6" style={{ textAlign: 'center', color: '#64748b' }}>
                                            No hay usuarios registrados.
                                        </td>
                                    </tr>
                                ) : (
                                    usuarios.map((usr) => (
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
                                                    <span style={{
                                                        color: usr.is_active ? '#4ade80' : '#f87171',
                                                        fontWeight: '600'
                                                    }}>
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
                                    ))
                                )}
                                </tbody>
                            </table>
                        </div>
                    </section>
                )}
            </main>
        </div>
    );
};