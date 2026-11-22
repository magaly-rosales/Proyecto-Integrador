import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'
import { tomarSesionDeLaUrl } from './auth/sesionUrl'

// Antes de pintar nada: si el admin viene del acceso general, se guarda su sesión
tomarSesionDeLaUrl()

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
)