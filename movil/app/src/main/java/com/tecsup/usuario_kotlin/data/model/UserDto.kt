package com.tecsup.usuario_kotlin.data.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val username: String
)