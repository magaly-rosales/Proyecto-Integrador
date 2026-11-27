package com.tecsup.usuario_kotlin.data.repository

import com.tecsup.usuario_kotlin.data.model.CatalogoResponse
import com.tecsup.usuario_kotlin.data.model.PuntoManoDto
import com.tecsup.usuario_kotlin.data.model.ReconocimientoResponse
import com.tecsup.usuario_kotlin.data.remote.RetrofitClient
import kotlinx.coroutines.CancellationException

class SenasRepository {

    private val api = RetrofitClient.apiService

    // Obtener catálogo de señas
    suspend fun getCatalogo(): Result<CatalogoResponse> {

        return try {

            val response = api.obtenerCatalogo()
            val body = response.body()

            if (response.isSuccessful && body != null) {

                Result.success(body)

            } else {

                Result.failure(
                    Exception(
                        "Error HTTP ${response.code()}: " +
                                response.message()
                    )
                )
            }

        } catch (e: CancellationException) {

            throw e

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    // Reconocimiento de señas estáticas
    suspend fun enviarPuntosMano(
        puntos: List<PuntoManoDto>
    ): Result<ReconocimientoResponse> {

        return try {

            val response = api.reconocerPuntos(puntos)
            val body = response.body()

            if (response.isSuccessful && body != null) {

                Result.success(body)

            } else {

                Result.failure(
                    Exception(
                        "Error reconocimiento: ${response.code()}"
                    )
                )
            }

        } catch (e: CancellationException) {

            throw e

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    // Reconocimiento de señas dinámicas
    suspend fun enviarSecuencia(
        cuadros: List<List<PuntoManoDto>>
    ): Result<ReconocimientoResponse> {

        // Validar la cantidad de fotogramas
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

        // Verificar que todos los cuadros tengan
        // la misma cantidad de puntos
        if (
            cuadros.any { cuadro ->
                cuadro.size != cantidadPuntos
            }
        ) {

            return Result.failure(
                IllegalArgumentException(
                    "Todos los cuadros deben tener la misma cantidad de puntos"
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
        return try {

            val response =
                api.reconocerSecuencia(cuadros)

            val body = response.body()

            if (response.isSuccessful && body != null) {

                Result.success(body)

            } else {

                Result.failure(
                    Exception(
                        "Error reconocimiento dinámico: " +
                                response.code()
                    )
                )
            }

        } catch (e: CancellationException) {

            throw e

        } catch (e: Exception) {

            Result.failure(e)
        }
    }
}