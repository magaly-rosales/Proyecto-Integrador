package com.tecsup.usuario_kotlin.data.remote

import com.tecsup.usuario_kotlin.data.model.CatalogoResponse
import com.tecsup.usuario_kotlin.data.model.ForgotPasswordRequest
import com.tecsup.usuario_kotlin.data.model.ForgotPasswordResponse
import com.tecsup.usuario_kotlin.data.model.LoginRequest
import com.tecsup.usuario_kotlin.data.model.LoginResponse
import com.tecsup.usuario_kotlin.data.model.PuntoManoDto
import com.tecsup.usuario_kotlin.data.model.ReconocimientoResponse
import com.tecsup.usuario_kotlin.data.model.RegisterRequest
import com.tecsup.usuario_kotlin.data.model.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/registro")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ForgotPasswordResponse>

    @GET("api/catalogo")
    suspend fun obtenerCatalogo(): Response<CatalogoResponse>

    @POST("api/reconocimiento/puntos")
    suspend fun reconocerPuntos(@Body puntos: List<PuntoManoDto>): Response<ReconocimientoResponse>

    @POST("api/reconocimiento/secuencia")
    suspend fun reconocerSecuencia(@Body cuadros: List<List<PuntoManoDto>>): Response<ReconocimientoResponse>
}