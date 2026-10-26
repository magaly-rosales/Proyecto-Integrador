import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import AuthProvider from "./context/AuthProvider";
import PreferenciasProvider from "./context/PreferenciasProvider";
import "./index.css";
import App from "./App.jsx";
import "./estilos/tema.css"; // va al final para que sobrescriba los estilos anteriores

createRoot(document.getElementById("root")).render(
  <StrictMode>
    <BrowserRouter>
      <PreferenciasProvider>
        <AuthProvider>
          <App />
        </AuthProvider>
      </PreferenciasProvider>
    </BrowserRouter>
  </StrictMode>,
);
