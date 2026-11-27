package com.tecsup.usuario_kotlin.data.remote

import com.tecsup.usuario_kotlin.data.model.CatalogoResponse
import com.tecsup.usuario_kotlin.data.model.ForgotPasswordRequest
import com.tecsup.usuario_kotlin.data.model.ForgotPasswordResponse
import com.tecsup.usuario_kotlin.data.model.FotoRequest
import com.tecsup.usuario_kotlin.data.model.LoginRequest
import com.tecsup.usuario_kotlin.data.model.LoginResponse
import com.tecsup.usuario_kotlin.data.model.PasswordRequest
import com.tecsup.usuario_kotlin.data.model.PasswordResponse
import com.tecsup.usuario_kotlin.data.model.PerfilDatosRequest
import com.tecsup.usuario_kotlin.data.model.PerfilResponse
import com.tecsup.usuario_kotlin.data.model.PuntoManoDto
import com.tecsup.usuario_kotlin.data.model.ReconocimientoResponse
import com.tecsup.usuario_kotlin.data.model.RegisterRequest
import com.tecsup.usuario_kotlin.data.model.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    // AUTENTICACIÓN

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("api/auth/registro")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequest
    ): Response<ForgotPasswordResponse>


    // GESTIÓN DEL PERFIL

    @PUT("api/perfil/{id}/datos")
    suspend fun actualizarDatosPerfil(
        @Path("id") idUsuario: Int,
        @Body request: PerfilDatosRequest
    ): Response<PerfilResponse>

    @PUT("api/perfil/{id}/password")
    suspend fun cambiarPassword(
        @Path("id") idUsuario: Int,
        @Body request: PasswordRequest
    ): Response<PasswordResponse>

    @PUT("api/perfil/{id}/foto")
    suspend fun actualizarFotoPerfil(
        @Path("id") idUsuario: Int,
        @Body request: FotoRequest
    ): Response<PerfilResponse>


    // CATÁLOGO DE SEÑAS

    @GET("api/catalogo")
    suspend fun obtenerCatalogo(): Response<CatalogoResponse>


    // RECONOCIMIENTO DE SEÑAS

    @POST("api/reconocimiento/puntos")
    suspend fun reconocerPuntos(
        @Body puntos: List<PuntoManoDto>
    ): Response<ReconocimientoResponse>

    @POST("api/reconocimiento/secuencia")
    suspend fun reconocerSecuencia(
        @Body cuadros: List<List<PuntoManoDto>>
    ): Response<ReconocimientoResponse>


    // REGISTRO DE TRADUCCIONES

    @POST("api/traducciones")
    suspend fun registrarTraduccion(
        @Body datos: Map<String, String>
    ): Response<Map<String, String>>
}