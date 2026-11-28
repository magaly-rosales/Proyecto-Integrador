package com.tecsup.usuario_kotlin.data.recognition

import com.tecsup.usuario_kotlin.data.model.PuntoManoDto
import com.tecsup.usuario_kotlin.data.model.ReconocimientoResponse
import com.tecsup.usuario_kotlin.data.repository.SenasRepository
import com.tecsup.usuario_kotlin.utils.RecognitionUtils
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

class StaticSignRecognizer(
    private val repository: SenasRepository
) {

    companion object {
        private val ROTATION_ANGLES = listOf(
            -15.0, 15.0, -30.0, 30.0
        )

        private const val ACCEPTABLE_DISTANCE = 0.45
    }

    // Reconocer una seña estática
    suspend fun reconocer(
        puntos: List<PuntoManoDto>
    ): ReconocimientoResponse {

        require(RecognitionUtils.puntosValidos(puntos)) {
            "Se requieren 21 o 42 puntos válidos"
        }

        // Reconocimiento con los puntos originales
        var resultado = repository
            .enviarPuntosMano(puntos)
            .getOrThrow()

        // Si la seña ya fue reconocida, devolver el resultado
        if (RecognitionUtils.esSenaReconocida(resultado)) {
            return resultado
        }

        // Aplicar rotaciones únicamente para una mano
        if (puntos.size != 21) {
            return resultado
        }

        // Buscar alternativas mediante rotaciones
        for (angulo in ROTATION_ANGLES) {

            currentCoroutineContext().ensureActive()

            val puntosRotados = RecognitionUtils.rotarPuntos(
                puntos = puntos,
                grados = angulo
            )

            val alternativa = repository
                .enviarPuntosMano(puntosRotados)
                .getOrNull() ?: continue

            if (!RecognitionUtils.esSenaReconocida(alternativa)) {
                continue
            }

            val distanciaActual =
                resultado.distancia ?: Double.MAX_VALUE

            val nuevaDistancia =
                alternativa.distancia ?: Double.MAX_VALUE

            // Conservar el mejor resultado encontrado
            if (
                !RecognitionUtils.esSenaReconocida(resultado) ||
                nuevaDistancia < distanciaActual
            ) {
                resultado = alternativa
            }

            // Finalizar si se obtiene una distancia aceptable
            if (nuevaDistancia <= ACCEPTABLE_DISTANCE) {
                break
            }
        }

        return resultado
    }
}