package com.tecsup.usuario_kotlin.data.repository

import com.tecsup.usuario_kotlin.data.model.CatalogoResponse
import com.tecsup.usuario_kotlin.data.model.PuntoManoDto
import com.tecsup.usuario_kotlin.data.model.ReconocimientoResponse
import com.tecsup.usuario_kotlin.data.remote.RetrofitClient
import com.tecsup.usuario_kotlin.utils.RespuestaInvalidaException
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import retrofit2.Response

class SenasRepository {

    private val api = RetrofitClient.apiService

    // Obtener catálogo de señas
    suspend fun getCatalogo(): Result<CatalogoResponse> {
        return ejecutarPeticion {
            api.obtenerCatalogo()
        }
    }

    // Reconocimiento de señas estáticas
    suspend fun enviarPuntosMano(
        puntos: List<PuntoManoDto>
    ): Result<ReconocimientoResponse> {

        return ejecutarPeticion {
            api.reconocerPuntos(puntos)
        }
    }

    // Reconocimiento de señas dinámicas
    suspend fun enviarSecuencia(
        cuadros: List<List<PuntoManoDto>>
    ): Result<ReconocimientoResponse> {

        // Validar cantidad de fotogramas
        if (cuadros.size !in 8..80) {
            return Result.failure(
                IllegalArgumentException(
                    "La secuencia debe contener entre 8 y 80 cuadros"
                )
            )
        }

        val cantidadPuntos = cuadros.first().size

        // Validar una o dos manos
        if (cantidadPuntos !in setOf(21, 42)) {
            return Result.failure(
                IllegalArgumentException(
                    "Cada cuadro debe contener 21 o 42 puntos"
                )
            )
        }

        // Validar cantidad uniforme de puntos
        if (
            cuadros.any { cuadro ->
                cuadro.size != cantidadPuntos
            }
        ) {
            return Result.failure(
                IllegalArgumentException(
                    "Todos los cuadros deben tener " +
                            "la misma cantidad de puntos"
                )
            )
        }

        // Validar coordenadas
        if (
            cuadros.any { cuadro ->
                cuadro.any { punto ->
                    !punto.x.isFinite() ||
                            !punto.y.isFinite() ||
                            !punto.z.isFinite()
                }
            }
        ) {
            return Result.failure(
                IllegalArgumentException(
                    "La secuencia contiene puntos inválidos"
                )
            )
        }

        // Enviar secuencia al servidor
        return ejecutarPeticion {
            api.reconocerSecuencia(cuadros)
        }
    }

    // Procesamiento centralizado de respuestas
    private suspend fun <T : Any> ejecutarPeticion(
        peticion: suspend () -> Response<T>
    ): Result<T> {

        return try {
            val response = peticion()

            if (!response.isSuccessful) {
                response.errorBody()?.close()

                Result.failure(
                    HttpException(response)
                )
            } else {
                val body = response.body()

                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(
                        RespuestaInvalidaException()
                    )
                }
            }

        } catch (e: CancellationException) {
            // Respetar la cancelación de corrutinas
            throw e

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}