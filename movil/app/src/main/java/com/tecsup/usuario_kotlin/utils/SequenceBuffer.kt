package com.tecsup.usuario_kotlin.utils

import android.os.SystemClock
import com.tecsup.usuario_kotlin.data.model.PuntoManoDto

class SequenceBuffer {

    companion object {

        // Cantidad de cuadros por secuencia
        const val WINDOW_SIZE = 20

        // Intervalo mínimo entre capturas
        const val FRAME_INTERVAL_MS = 100L

        // Enviar cada tres cuadros nuevos
        private const val SEND_EVERY_FRAMES = 3

        // Interrupción máxima entre fotogramas
        private const val MAX_FRAME_GAP_MS = 350L

        // Cantidad de puntos por mano
        private const val POINTS_PER_HAND = 21
    }

    // Ventana deslizante de fotogramas
    private val frames =
        ArrayDeque<List<PuntoManoDto>>()

    private var lastFrameTime: Long? = null

    private var framesSinceLastSequence = 0

    // Identificador de cada secuencia
    var generation = 0L
        private set

    // Cantidad de cuadros almacenados
    val frameCount: Int
        get() = frames.size

    // Cantidad de manos
    val handCount: Int
        get() {
            val puntos =
                frames.firstOrNull()?.size ?: 0

            return puntos / POINTS_PER_HAND
        }

    // Progreso de la captura
    val progress: Float
        get() = (
                frameCount.toFloat() / WINDOW_SIZE
                ).coerceIn(0f, 1f)

    // Verificar ventana completa
    val isReady: Boolean
        get() = frameCount == WINDOW_SIZE

    // Agregar un fotograma
    fun registrar(
        puntos: List<PuntoManoDto>,
        timestampMs: Long = SystemClock.elapsedRealtime()
    ): List<List<PuntoManoDto>>? {

        // Reutilizar la validación centralizada
        if (!RecognitionUtils.puntosValidos(puntos)) {

            if (
                frames.isNotEmpty() ||
                lastFrameTime != null
            ) {
                limpiar()
            }

            return null
        }

        // Verificar cambios en la cantidad de manos
        val cantidadAnterior =
            frames.lastOrNull()?.size

        if (
            cantidadAnterior != null &&
            cantidadAnterior != puntos.size
        ) {
            limpiar()
        }

        // Verificar interrupciones de captura
        val tiempoAnterior = lastFrameTime

        if (
            tiempoAnterior != null &&
            (
                    timestampMs < tiempoAnterior ||
                            timestampMs - tiempoAnterior >
                            MAX_FRAME_GAP_MS
                    )
        ) {
            limpiar()
        }

        // Limitar frecuencia de captura
        val ultimaCaptura = lastFrameTime

        if (
            ultimaCaptura != null &&
            timestampMs - ultimaCaptura <
            FRAME_INTERVAL_MS
        ) {
            return null
        }

        lastFrameTime = timestampMs

        // Almacenar copia de los puntos
        frames.addLast(puntos.toList())

        // Mantener una ventana de 20 cuadros
        while (frames.size > WINDOW_SIZE) {
            frames.removeFirst()
        }

        framesSinceLastSequence++

        // Esperar una ventana completa
        if (!isReady) {
            return null
        }

        // Limitar la frecuencia de envío
        if (
            framesSinceLastSequence <
            SEND_EVERY_FRAMES
        ) {
            return null
        }

        framesSinceLastSequence = 0

        // Devolver una copia independiente
        return frames.map { cuadro ->
            cuadro.toList()
        }
    }

    // Reiniciar captura
    fun limpiar() {
        frames.clear()

        lastFrameTime = null

        framesSinceLastSequence = 0

        generation++
    }
}