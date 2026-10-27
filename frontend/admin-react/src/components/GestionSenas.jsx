import { useState, useEffect, useCallback } from 'react';
import { getSenas, createSena, deleteSena } from '../services/senasService';
import './GestionSenas.css';

export const GestionSenas = () => {
    const [senas, setSenas] = useState([]);
    const [nombre, setNombre] = useState('');
    const [categoria, setCategoria] = useState('');
    const [videoFile, setVideoFile] = useState(null);
    const [loading, setLoading] = useState(false);

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

    useEffect(() => {
        // eslint-disable-next-line react-hooks/set-state-in-effect
        cargarSenas();
    }, [cargarSenas]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!nombre.trim() || !categoria.trim()) return;

        // Construcción de FormData para enviar campos de texto y archivo de video
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
            console.error("Error al guardar la seña con video:", error);
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

    return (
        <div className="admin-layout">
            {/* Sidebar Lateral */}
            <aside className="admin-sidebar">
                <div className="brand">
                    <span className="brand-icon">🤟</span>
                    <h2>LSB Admin</h2>
                </div>
                <nav className="admin-nav">
                    <a href="#senas" className="nav-item active">
                        📁 Gestión de Señas
                    </a>
                    <a href="#categorias" className="nav-item">
                        🏷️ Categorías
                    </a>
                    <a href="#usuarios" className="nav-item">
                        👥 Usuarios
                    </a>
                </nav>
                <div className="system-status">
                    <span className="status-dot"></span> Backend Django Online
                </div>
            </aside>

            {/* Contenido Principal */}
            <main className="admin-main">
                <header className="admin-header">
                    <h1 className="admin-title">Módulo de Señas LSB</h1>
                    <p className="admin-subtitle">Administra las señas y videos de referencia en Django</p>
                </header>

                {/* KPIs */}
                <div className="kpi-grid">
                    <div className="kpi-card">
                        <span className="kpi-number">{senas.length}</span>
                        <span className="kpi-label">Total de Señas Registradas</span>
                    </div>
                    <div className="kpi-card">
                        <span className="kpi-number">
                            {new Set(senas.map(s => s.categoria)).size}
                        </span>
                        <span className="kpi-label">Categorías Activas</span>
                    </div>
                    <div className="kpi-card">
                        <span className="kpi-number">Django REST</span>
                        <span className="kpi-label">API Status 200 OK</span>
                    </div>
                </div>

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
                                    placeholder="Ej: Saludos, Familia"
                                    value={categoria}
                                    onChange={(e) => setCategoria(e.target.value)}
                                    className="admin-input"
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label>Video de Referencia (MP4, máx 50MB)</label>
                                <input
                                    id="video-input"
                                    type="file"
                                    accept="video/mp4,video/*"
                                    onChange={(e) => setVideoFile(e.target.files[0])}
                                    className="admin-input"
                                />
                            </div>

                            <button type="submit" className="btn-primary">
                                Guardar en BD
                            </button>
                        </form>
                    </section>

                    <section className="admin-card">
                        <div className="card-header">
                            <h3 className="card-title">📋 Listado de Señas</h3>
                            <button onClick={cargarSenas} className="btn-refresh">
                                🔄 Actualizar
                            </button>
                        </div>

                        {loading ? (
                            <p style={{ color: '#94a3b8' }}>Cargando datos desde Django...</p>
                        ) : (
                            <div className="table-wrapper">
                                <table className="admin-table">
                                    <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Nombre</th>
                                        <th>Categoría</th>
                                        <th>Video MP4</th>
                                        <th style={{ textAlign: 'center' }}>Acciones</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {senas.length === 0 ? (
                                        <tr>
                                            <td colSpan="5" style={{ textAlign: 'center', color: '#64748b' }}>
                                                No hay señas registradas.
                                            </td>
                                        </tr>
                                    ) : (
                                        senas.map((sena) => (
                                            <tr key={sena.id}>
                                                <td className="td-muted">#{sena.id}</td>
                                                <td className="td-bold">{sena.nombre}</td>
                                                <td>
                                                    <span className="badge">{sena.categoria}</span>
                                                </td>
                                                <td>
                                                    {sena.video ? (
                                                        <video
                                                            src={sena.video}
                                                            width="110"
                                                            height="70"
                                                            controls
                                                            style={{ borderRadius: '6px', objectFit: 'cover' }}
                                                        />
                                                    ) : (
                                                        <span style={{ color: '#64748b', fontSize: '12px' }}>Sin video</span>
                                                    )}
                                                </td>
                                                <td style={{ textAlign: 'center' }}>
                                                    <button
                                                        onClick={() => handleDelete(sena.id)}
                                                        className="btn-danger"
                                                    >
                                                        🗑️ Eliminar
                                                    </button>
                                                </td>
                                            </tr>
                                        ))
                                    )}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </section>
                </div>
            </main>
        </div>
    );
};