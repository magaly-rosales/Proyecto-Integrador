package com.tecsup.usuario_kotlin.data.model


data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val idUsuario: Int? = null,
    val nombre: String? = null,
    val email: String? = null,
    val rol: String? = null,
    val foto: String? = null,
    val tokenAdmin: String? = null
)

data class RegisterRequest(
    val nombre: String,
    val email: String,
    val password: String
)

data class RegisterResponse(
    val idUsuario: Int? = null,
    val nombre: String? = null,
    val email: String? = null,
    val error: String? = null
)

data class ForgotPasswordRequest(
    val email: String
)

data class ForgotPasswordResponse(
    val message: String? = null,
    val success: Boolean = true
)