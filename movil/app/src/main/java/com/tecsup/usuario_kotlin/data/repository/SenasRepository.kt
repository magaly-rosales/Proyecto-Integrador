package com.tecsup.usuario_kotlin.data.repository

import com.tecsup.usuario_kotlin.data.model.CatalogoResponse
import com.tecsup.usuario_kotlin.data.model.PuntoManoDto
import com.tecsup.usuario_kotlin.data.model.ReconocimientoResponse
import com.tecsup.usuario_kotlin.data.remote.RetrofitClient

class SenasRepository {
    private val api = RetrofitClient.apiService

    suspend fun getCatalogo(): Result<CatalogoResponse> {
        return try {
            val response = api.obtenerCatalogo()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun enviarPuntosMano(puntos: List<PuntoManoDto>): Result<ReconocimientoResponse> {
        return try {
            val response = api.reconocerPuntos(puntos)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error reconocimiento: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}