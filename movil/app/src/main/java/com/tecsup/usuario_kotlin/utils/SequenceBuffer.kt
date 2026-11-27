package com.tecsup.usuario_kotlin.utils

import android.os.SystemClock
import com.tecsup.usuario_kotlin.data.model.PuntoManoDto

class SequenceBuffer {

    companion object {

        // Cantidad de cuadros por secuencia
        const val WINDOW_SIZE = 20

        // Intervalo mínimo entre capturas
        const val FRAME_INTERVAL_MS = 100L

        // Enviar una secuencia cada tres cuadros nuevos
        private const val SEND_EVERY_FRAMES = 3

        // Reiniciar si existe una interrupción prolongada
        private const val MAX_FRAME_GAP_MS = 350L

        // Cantidad de puntos por mano
        private const val POINTS_PER_HAND = 21
    }

    // Ventana deslizante de fotogramas
    private val frames =
        ArrayDeque<List<PuntoManoDto>>()

    // Control de tiempo y capturas
    private var lastFrameTime: Long? = null

    private var framesSinceLastSequence = 0

    // Identificar cada secuencia de captura
    var generation = 0L
        private set

    // Cantidad de cuadros almacenados
    val frameCount: Int
        get() = frames.size

    // Cantidad de manos en la secuencia
    val handCount: Int
        get() {

            val puntos =
                frames.firstOrNull()?.size ?: 0

            return puntos / POINTS_PER_HAND
        }

    // Progreso de captura entre 0 y 1
    val progress: Float
        get() = (
                frameCount.toFloat() / WINDOW_SIZE
                ).coerceIn(0f, 1f)

    // Verificar si la ventana está completa
    val isReady: Boolean
        get() = frameCount == WINDOW_SIZE

    // Agregar un fotograma a la ventana
    fun registrar(
        puntos: List<PuntoManoDto>,
        timestampMs: Long = SystemClock.elapsedRealtime()
    ): List<List<PuntoManoDto>>? {

        // Validar los puntos recibidos
        val puntosValidos =
            puntos.size in setOf(21, 42) &&
                    puntos.all { punto ->

                        punto.x.isFinite() &&
                                punto.y.isFinite() &&
                                punto.z.isFinite()
                    }

        // No mezclar secuencias sin manos válidas
        if (!puntosValidos) {

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

        // Verificar interrupciones entre fotogramas
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

        // Limitar la frecuencia de captura
        val ultimaCaptura = lastFrameTime

        if (
            ultimaCaptura != null &&
            timestampMs - ultimaCaptura <
            FRAME_INTERVAL_MS
        ) {
            return null
        }

        // Registrar el tiempo de captura
        lastFrameTime = timestampMs

        // Almacenar una copia de los puntos
        frames.addLast(
            puntos.toList()
        )

        // Mantener solamente los últimos 20 cuadros
        while (frames.size > WINDOW_SIZE) {

            frames.removeFirst()
        }

        framesSinceLastSequence++

        // Esperar hasta completar la ventana
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

        // Devolver una copia independiente de la secuencia
        return frames.map { cuadro ->

            cuadro.toList()
        }
    }

    // Reiniciar la secuencia de captura
    fun limpiar() {

        frames.clear()

        lastFrameTime = null

        framesSinceLastSequence = 0

        generation++
    }
}