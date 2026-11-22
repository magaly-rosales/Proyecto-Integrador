package com.senasapp.usuariospringboot.dto;

// Respuesta del login: datos del usuario y, solo si es administrador, la sesión del panel (Django)
public record LoginRespuesta(Integer idUsuario, String nombre, String email, String rol, String foto,
                             String tokenAdmin) {

    public static LoginRespuesta de(UsuarioRespuesta u, String tokenAdmin) {
        return new LoginRespuesta(u.idUsuario(), u.nombre(), u.email(), u.rol(), u.foto(), tokenAdmin);
    }
}