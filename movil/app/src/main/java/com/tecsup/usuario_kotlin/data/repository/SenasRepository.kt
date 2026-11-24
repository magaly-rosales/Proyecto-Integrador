package com.tecsup.usuario_kotlin.data.repository

import com.tecsup.usuario_kotlin.data.model.SenaDto
import com.tecsup.usuario_kotlin.data.remote.RetrofitClient

class SenasRepository {
    private val api = RetrofitClient.apiService

    suspend fun getSenasDisponibles(): Result<List<SenaDto>> {
        return try {
            val response = api.obtenerSenas()
            if (response.isSuccessful) {
                val lista = response.body().orEmpty()
                Result.success(lista)
            } else {
                Result.failure(Exception("Error HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}