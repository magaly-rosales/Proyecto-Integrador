package com.tecsup.usuario_kotlin.data.recognition

import com.tecsup.usuario_kotlin.data.model.PuntoManoDto
import com.tecsup.usuario_kotlin.data.model.ReconocimientoResponse
import com.tecsup.usuario_kotlin.data.repository.SenasRepository
import com.tecsup.usuario_kotlin.utils.RecognitionUtils
import com.tecsup.usuario_kotlin.utils.SequenceBuffer

class DynamicSignRecognizer(
    private val repository: SenasRepository
) {
    companion object {
        const val REQUIRED_CONFIRMATIONS = 2
    }

    private val buffer = SequenceBuffer()

    var secuenciaPendiente: List<List<PuntoManoDto>>? = null
        private set

    private var candidata: String? = null

    var confirmaciones = 0
        private set

    val frameCount: Int
        get() = buffer.frameCount

    // Registrar un fotograma y detectar reinicios del buffer
    fun registrar(
        puntos: List<PuntoManoDto>,
        timestampMs: Long
    ): Boolean {
        val generation = buffer.generation
        val secuencia = buffer.registrar(puntos, timestampMs)
        val reiniciado = generation != buffer.generation

        if (reiniciado) {
            secuenciaPendiente = null
            limpiarConfirmaciones()
        }

        if (secuencia != null) {
            secuenciaPendiente = secuencia
        }

        return reiniciado
    }

    // Obtener y consumir la última secuencia disponible
    fun tomarSecuencia(): List<List<PuntoManoDto>>? {
        val secuencia = secuenciaPendiente
        secuenciaPendiente = null
        return secuencia
    }

    // Reconocimiento mediante Spring Boot
    suspend fun reconocer(
        cuadros: List<List<PuntoManoDto>>
    ): ReconocimientoResponse {
        return repository.enviarSecuencia(cuadros).getOrThrow()
    }

    // Confirmar dos resultados consecutivos iguales
    fun confirmar(resultado: ReconocimientoResponse): Boolean {
        val nombre = RecognitionUtils.obtenerNombre(resultado)

        if (!RecognitionUtils.esNombreValido(nombre)) {
            limpiarConfirmaciones()
            return false
        }

        if (candidata.equals(nombre, ignoreCase = true)) {
            confirmaciones++
        } else {
            candidata = nombre
            confirmaciones = 1
        }

        confirmaciones = confirmaciones
            .coerceAtMost(REQUIRED_CONFIRMATIONS)

        return confirmaciones >= REQUIRED_CONFIRMATIONS
    }

    fun limpiarConfirmaciones() {
        candidata = null
        confirmaciones = 0
    }

    // Reiniciar toda la captura
    fun limpiar() {
        buffer.limpiar()
        secuenciaPendiente = null
        limpiarConfirmaciones()
    }
}