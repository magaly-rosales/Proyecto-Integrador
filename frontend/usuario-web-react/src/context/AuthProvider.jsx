import { useState } from "react";
import { AuthContext } from "./AuthContext";
import { iniciarSesion, registrarUsuario } from "../services/auth";

const CLAVE = "senasapp_usuario";

// Lee el usuario guardado: primero "recordado" (localStorage), luego el de esta pestaña (sessionStorage)
function leerGuardado() {
  try {
    return JSON.parse(localStorage.getItem(CLAVE) ?? sessionStorage.getItem(CLAVE));
  } catch {
    return null;
  }
}

export default function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(leerGuardado);

  // recordar = true  -> la sesión sigue al cerrar el navegador (localStorage)
  // recordar = false -> la sesión dura solo mientras la pestaña esté abierta (sessionStorage)
  // sin indicar     -> se mantiene donde ya estaba (así Perfil puede actualizar sin cambiarlo)
  const guardar = (datos, recordar) => {
    setUsuario(datos);
    try {
      const persistente = recordar ?? sessionStorage.getItem(CLAVE) === null;
      const destino = persistente ? localStorage : sessionStorage;
      const otro = persistente ? sessionStorage : localStorage;
      destino.setItem(CLAVE, JSON.stringify(datos));
      otro.removeItem(CLAVE);
    } catch {
      /* si no se puede guardar, la sesión dura hasta recargar la página */
    }
  };

  // Devuelve los datos del usuario. Si es administrador NO se guarda la sesión aquí:
  // el admin entra por su propio panel (Django).
  const login = async (email, password, recordar = true) => {
    const datos = await iniciarSesion({ email, password });
    if (!datos.tokenAdmin) guardar(datos, recordar);
    return datos;
  };

  // Al registrarse, entra directo a la app y la sesión se recuerda
  const registro = async (nombre, email, password) => {
    await registrarUsuario({ nombre, email, password });
    await login(email, password, true);
  };

  const logout = () => {
    setUsuario(null);
    try {
      localStorage.removeItem(CLAVE);
      sessionStorage.removeItem(CLAVE);
    } catch {
      /* nada que borrar */
    }
  };

  return (
    <AuthContext.Provider value={{ usuario, login, registro, logout, actualizar: guardar }}>
      {children}
    </AuthContext.Provider>
  );
}