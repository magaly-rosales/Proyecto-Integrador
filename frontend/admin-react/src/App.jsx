import { useState } from 'react';
import { GestionSenas } from './components/GestionSenas';

function App() {
    const [isAuthenticated, setIsAuthenticated] = useState(() => {
        return localStorage.getItem('admin_token') === 'true';
    });
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');

    const handleLogin = (e) => {
        e.preventDefault();
        // Validación de credenciales del administrador
        if (username === 'admin' && password === 'admin123') {
            localStorage.setItem('admin_token', 'true');
            setIsAuthenticated(true);
            setError('');
        } else {
            setError('Credenciales incorrectas. (Credenciales demo: admin / admin123)');
        }
    };

    const handleLogout = () => {
        localStorage.removeItem('admin_token');
        setIsAuthenticated(false);
    };

    if (!isAuthenticated) {
        return (
            <div style={{
                minHeight: '100vh',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                background: '#0f172a',
                color: '#f8fafc',
                fontFamily: 'system-ui, sans-serif'
            }}>
                <form onSubmit={handleLogin} style={{
                    background: '#1e293b',
                    padding: '2.5rem',
                    borderRadius: '12px',
                    border: '1px solid #334155',
                    width: '100%',
                    maxWidth: '400px',
                    boxShadow: '0 10px 25px -5px rgba(0, 0, 0, 0.5)'
                }}>
                    <div style={{ textAlign: 'center', marginBottom: '1.5rem' }}>
                        <span style={{ fontSize: '3rem' }}>🤟</span>
                        <h2 style={{ margin: '0.5rem 0 0.2rem 0' }}>LSB Admin Login</h2>
                        <p style={{ color: '#94a3b8', fontSize: '14px', margin: 0 }}>Acceso protegido para administradores</p>
                    </div>

                    {error && (
                        <div style={{
                            background: '#7f1d1d',
                            color: '#fca5a5',
                            padding: '0.75rem',
                            borderRadius: '6px',
                            marginBottom: '1rem',
                            fontSize: '13px',
                            textAlign: 'center'
                        }}>
                            {error}
                        </div>
                    )}

                    <div style={{ marginBottom: '1rem' }}>
                        <label style={{ display: 'block', marginBottom: '0.4rem', fontSize: '14px', color: '#cbd5e1' }}>Usuario</label>
                        <input
                            type="text"
                            placeholder="admin"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            style={{
                                width: '100%',
                                padding: '0.75rem',
                                borderRadius: '6px',
                                border: '1px solid #475569',
                                background: '#0f172a',
                                color: '#fff',
                                boxSizing: 'border-box'
                            }}
                            required
                        />
                    </div>

                    <div style={{ marginBottom: '1.5rem' }}>
                        <label style={{ display: 'block', marginBottom: '0.4rem', fontSize: '14px', color: '#cbd5e1' }}>Contraseña</label>
                        <input
                            type="password"
                            placeholder="••••••••"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            style={{
                                width: '100%',
                                padding: '0.75rem',
                                borderRadius: '6px',
                                border: '1px solid #475569',
                                background: '#0f172a',
                                color: '#fff',
                                boxSizing: 'border-box'
                            }}
                            required
                        />
                    </div>

                    <button
                        type="submit"
                        style={{
                            width: '100%',
                            padding: '0.8rem',
                            borderRadius: '6px',
                            border: 'none',
                            background: '#2563eb',
                            color: '#fff',
                            fontWeight: 'bold',
                            cursor: 'pointer',
                            fontSize: '15px'
                        }}
                    >
                        Ingresar al Panel
                    </button>
                </form>
            </div>
        );
    }

    return (
        <div>
            <GestionSenas onLogout={handleLogout} />
        </div>
    );
}

export default App;