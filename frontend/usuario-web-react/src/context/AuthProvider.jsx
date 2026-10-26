import { useState } from "react";
import { AuthContext } from "./AuthContext";
import { iniciarSesion, registrarUsuario } from "../services/auth";

const CLAVE = "senasapp_usuario";

// Lee el usuario guardado (si el navegador lo permite)
function leerGuardado() {
  try {
    return JSON.parse(localStorage.getItem(CLAVE));
  } catch {
    return null;
  }
}

export default function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(leerGuardado);

  const guardar = (datos) => {
    setUsuario(datos);
    try {
      localStorage.setItem(CLAVE, JSON.stringify(datos));
    } catch {
      /* si no se puede guardar, la sesión dura hasta cerrar la pestaña */
    }
  };

  const login = async (email, password) => guardar(await iniciarSesion({ email, password }));

  // Al registrarse, entra directo a la app
  const registro = async (nombre, email, password) => {
    await registrarUsuario({ nombre, email, password });
    await login(email, password);
  };

  const logout = () => {
    setUsuario(null);
    try {
      localStorage.removeItem(CLAVE);
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
