package com.tecsup.usuario_kotlin.data.remote

import com.tecsup.usuario_kotlin.data.model.LoginRequest
import com.tecsup.usuario_kotlin.data.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/auth/login") // Ajusta la ruta según tu backend
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}