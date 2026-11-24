package com.tecsup.usuario_kotlin.data.remote

import com.tecsup.usuario_kotlin.data.model.CategoriaDto
import com.tecsup.usuario_kotlin.data.model.SenaDto
import retrofit2.http.GET
import com.tecsup.usuario_kotlin.data.model.ForgotPasswordRequest
import com.tecsup.usuario_kotlin.data.model.ForgotPasswordResponse
import com.tecsup.usuario_kotlin.data.model.LoginRequest
import com.tecsup.usuario_kotlin.data.model.LoginResponse
import com.tecsup.usuario_kotlin.data.model.RegisterRequest
import com.tecsup.usuario_kotlin.data.model.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ForgotPasswordResponse>
    @GET("api/senas")
    suspend fun obtenerSenas(): Response<List<SenaDto>>

    @GET("api/categorias")
    suspend fun obtenerCategorias(): Response<List<CategoriaDto>>
}
