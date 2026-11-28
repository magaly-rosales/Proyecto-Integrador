package com.tecsup.usuario_kotlin.utils

import com.tecsup.usuario_kotlin.data.model.PuntoManoDto
import com.tecsup.usuario_kotlin.data.model.ReconocimientoResponse
import kotlin.math.cos
import kotlin.math.sin

object RecognitionUtils {

    private val INVALID_RESULTS = setOf(
        "desconocida",
        "dudosa",
        "sin_muestras"
    )

    // Validar puntos detectados por MediaPipe
    fun puntosValidos(
        puntos: List<PuntoManoDto>
    ): Boolean {
        val cantidad = puntos.size

        if (cantidad != 21 && cantidad != 42) {
            return false
        }

        return puntos.all { punto ->
            punto.x.isFinite() &&
                    punto.y.isFinite() &&
                    punto.z.isFinite()
        }
    }

    // Obtener el nombre devuelto por el servidor
    fun obtenerNombre(
        resultado: ReconocimientoResponse
    ): String? {
        return resultado.sena
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: resultado.nombre
                ?.trim()
                ?.takeIf { it.isNotBlank() }
    }

    // Validar el nombre de una seña
    fun esNombreValido(nombre: String?): Boolean {
        return !nombre.isNullOrBlank() &&
                nombre.trim().lowercase() !in INVALID_RESULTS
    }

    // Validar el resultado del reconocimiento
    fun esSenaReconocida(
        resultado: ReconocimientoResponse
    ): Boolean {
        return esNombreValido(
            obtenerNombre(resultado)
        )
    }

    // Comparar la seña detectada con el objetivo
    fun coincidenSenas(
        detectada: String?,
        objetivo: String
    ): Boolean {
        return esNombreValido(detectada) &&
                objetivo.isNotBlank() &&
                detectada?.trim().equals(
                    objetivo.trim(),
                    ignoreCase = true
                )
    }

    // Convertir la confianza a porcentaje
    fun obtenerConfianza(
        resultado: ReconocimientoResponse
    ): Int? {
        return resultado.confianza
            ?.takeIf { it.isFinite() }
            ?.let { valor ->

                val porcentaje = if (valor <= 1.0) {
                    valor * 100
                } else {
                    valor
                }

                porcentaje.toInt().coerceIn(0, 100)
            }
    }

    // Rotar puntos tomando la muñeca como origen
    fun rotarPuntos(
        puntos: List<PuntoManoDto>,
        grados: Double
    ): List<PuntoManoDto> {
        if (puntos.isEmpty()) return emptyList()

        val radianes = Math.toRadians(grados)
        val coseno = cos(radianes).toFloat()
        val seno = sin(radianes).toFloat()

        val origen = puntos.first()

        return puntos.map { punto ->
            val dx = punto.x - origen.x
            val dy = punto.y - origen.y

            PuntoManoDto(
                x = origen.x + dx * coseno - dy * seno,
                y = origen.y + dx * seno + dy * coseno,
                z = punto.z
            )
        }
    }
}