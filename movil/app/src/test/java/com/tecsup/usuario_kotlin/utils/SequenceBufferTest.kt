package com.tecsup.usuario_kotlin.utils

import com.tecsup.usuario_kotlin.data.model.PuntoManoDto
import org.junit.Assert.*
import org.junit.Test

class SequenceBufferTest {

    private fun crearPuntos(
        cantidad: Int = 21
    ): List<PuntoManoDto> {
        return List(cantidad) {
            PuntoManoDto(
                x = 0.5f,
                y = 0.4f,
                z = 0.1f
            )
        }
    }

    @Test
    fun bufferInicialmenteVacio() {
        val buffer = SequenceBuffer()

        assertEquals(0, buffer.frameCount)
        assertEquals(0, buffer.handCount)
        assertEquals(0f, buffer.progress, 0.001f)
        assertFalse(buffer.isReady)
    }

    @Test
    fun completaVentanaDeVeinteCuadros() {
        val buffer = SequenceBuffer()

        for (i in 0 until 19) {
            val resultado = buffer.registrar(
                puntos = crearPuntos(),
                timestampMs = i * 100L
            )

            assertNull(resultado)
        }

        val resultado = buffer.registrar(
            puntos = crearPuntos(),
            timestampMs = 1900L
        )

        assertNotNull(resultado)
        assertEquals(20, resultado!!.size)

        assertEquals(20, buffer.frameCount)
        assertEquals(1, buffer.handCount)
        assertEquals(1f, buffer.progress, 0.001f)
        assertTrue(buffer.isReady)
    }

    @Test
    fun respetaIntervaloMinimoDeCaptura() {
        val buffer = SequenceBuffer()

        buffer.registrar(
            crearPuntos(),
            timestampMs = 0L
        )

        buffer.registrar(
            crearPuntos(),
            timestampMs = 50L
        )

        assertEquals(1, buffer.frameCount)

        buffer.registrar(
            crearPuntos(),
            timestampMs = 100L
        )

        assertEquals(2, buffer.frameCount)
    }

    @Test
    fun reiniciaCuandoCambiaCantidadDeManos() {
        val buffer = SequenceBuffer()

        buffer.registrar(
            crearPuntos(21),
            timestampMs = 0L
        )

        buffer.registrar(
            crearPuntos(21),
            timestampMs = 100L
        )

        val generationAnterior = buffer.generation

        buffer.registrar(
            crearPuntos(42),
            timestampMs = 200L
        )

        assertEquals(1, buffer.frameCount)
        assertEquals(2, buffer.handCount)

        assertTrue(
            buffer.generation > generationAnterior
        )
    }

    @Test
    fun reiniciaAnteInterrupcionProlongada() {
        val buffer = SequenceBuffer()

        buffer.registrar(
            crearPuntos(),
            timestampMs = 0L
        )

        buffer.registrar(
            crearPuntos(),
            timestampMs = 100L
        )

        buffer.registrar(
            crearPuntos(),
            timestampMs = 600L
        )

        assertEquals(1, buffer.frameCount)
        assertFalse(buffer.isReady)
    }

    @Test
    fun limpiaBufferConPuntosInvalidos() {
        val buffer = SequenceBuffer()

        buffer.registrar(
            crearPuntos(),
            timestampMs = 0L
        )

        buffer.registrar(
            emptyList(),
            timestampMs = 100L
        )

        assertEquals(0, buffer.frameCount)
        assertEquals(0, buffer.handCount)
        assertFalse(buffer.isReady)
    }

    @Test
    fun generaNuevaSecuenciaCadaTresCuadros() {
        val buffer = SequenceBuffer()

        repeat(20) { index ->
            buffer.registrar(
                crearPuntos(),
                timestampMs = index * 100L
            )
        }

        assertNull(
            buffer.registrar(
                crearPuntos(),
                timestampMs = 2000L
            )
        )

        assertNull(
            buffer.registrar(
                crearPuntos(),
                timestampMs = 2100L
            )
        )

        val resultado = buffer.registrar(
            crearPuntos(),
            timestampMs = 2200L
        )

        assertNotNull(resultado)
        assertEquals(20, resultado!!.size)
        assertEquals(20, buffer.frameCount)
    }

    @Test
    fun conservaCopiaIndependienteDeLosPuntos() {
        val buffer = SequenceBuffer()

        val puntos = crearPuntos().toMutableList()

        buffer.registrar(
            puntos,
            timestampMs = 0L
        )

        puntos[0] = PuntoManoDto(
            9f,
            9f,
            9f
        )

        var resultado: List<List<PuntoManoDto>>? = null

        for (i in 1 until 20) {
            resultado = buffer.registrar(
                crearPuntos(),
                timestampMs = i * 100L
            )
        }

        assertNotNull(resultado)

        assertEquals(
            PuntoManoDto(0.5f, 0.4f, 0.1f),
            resultado!![0][0]
        )
    }
}