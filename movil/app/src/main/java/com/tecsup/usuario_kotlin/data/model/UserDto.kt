package com.tecsup.usuario_kotlin.data.model

// Solicitud de inicio de sesión
data class LoginRequest(
    val email: String,
    val password: String
)

// Respuesta del inicio de sesión
data class LoginResponse(
    val idUsuario: Int? = null,
    val nombre: String? = null,
    val email: String? = null,
    val rol: String? = null,
    val foto: String? = null,
    val tokenAdmin: String? = null
)

// Solicitud de registro
data class RegisterRequest(
    val nombre: String,
    val email: String,
    val password: String
)

// Respuesta del registro
data class RegisterResponse(
    val idUsuario: Int? = null,
    val nombre: String? = null,
    val email: String? = null,
    val error: String? = null
)

// Solicitud de recuperación de contraseña
data class ForgotPasswordRequest(
    val email: String
)

// Respuesta de recuperación de contraseña
data class ForgotPasswordResponse(
    val message: String? = null,
    val success: Boolean = true
)

// Solicitud de actualización de datos personales
data class PerfilDatosRequest(
    val nombre: String,
    val email: String
)

// Respuesta de actualización del perfil
data class PerfilResponse(
    val idUsuario: Int? = null,
    val nombre: String? = null,
    val email: String? = null,
    val rol: String? = null,
    val foto: String? = null
)