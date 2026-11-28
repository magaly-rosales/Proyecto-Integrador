package com.tecsup.usuario_kotlin.utils

import com.tecsup.usuario_kotlin.data.model.PuntoManoDto
import com.tecsup.usuario_kotlin.data.model.ReconocimientoResponse
import org.junit.Assert.*
import org.junit.Test

class RecognitionUtilsTest {

    private fun crearPuntos(
        cantidad: Int
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
    fun aceptaUnaODosManos() {
        assertTrue(
            RecognitionUtils.puntosValidos(
                crearPuntos(21)
            )
        )

        assertTrue(
            RecognitionUtils.puntosValidos(
                crearPuntos(42)
            )
        )
    }

    @Test
    fun rechazaCantidadIncorrectaDePuntos() {
        assertFalse(
            RecognitionUtils.puntosValidos(
                emptyList()
            )
        )

        assertFalse(
            RecognitionUtils.puntosValidos(
                crearPuntos(20)
            )
        )
    }

    @Test
    fun rechazaCoordenadasInvalidas() {
        val puntos = crearPuntos(21).toMutableList()

        puntos[0] = PuntoManoDto(
            x = Float.NaN,
            y = 0.4f,
            z = 0.1f
        )

        assertFalse(
            RecognitionUtils.puntosValidos(puntos)
        )

        puntos[0] = PuntoManoDto(
            x = 0.5f,
            y = Float.POSITIVE_INFINITY,
            z = 0.1f
        )

        assertFalse(
            RecognitionUtils.puntosValidos(puntos)
        )
    }

    @Test
    fun validaNombresDeSenas() {
        assertTrue(
            RecognitionUtils.esNombreValido("Hola")
        )

        assertFalse(
            RecognitionUtils.esNombreValido(null)
        )

        assertFalse(
            RecognitionUtils.esNombreValido(" ")
        )

        assertFalse(
            RecognitionUtils.esNombreValido("desconocida")
        )

        assertFalse(
            RecognitionUtils.esNombreValido(" DUDOSA ")
        )

        assertFalse(
            RecognitionUtils.esNombreValido("sin_muestras")
        )
    }

    @Test
    fun obtieneNombreYComparaSenas() {
        val resultado = ReconocimientoResponse(
            sena = " ",
            nombre = " Hola "
        )

        assertEquals(
            "Hola",
            RecognitionUtils.obtenerNombre(resultado)
        )

        assertTrue(
            RecognitionUtils.coincidenSenas(
                detectada = " HOLA ",
                objetivo = "Hola"
            )
        )

        assertFalse(
            RecognitionUtils.coincidenSenas(
                detectada = "Gracias",
                objetivo = "Hola"
            )
        )
    }

    @Test
    fun convierteConfianzaCorrectamente() {
        assertEquals(
            85,
            RecognitionUtils.obtenerConfianza(
                ReconocimientoResponse(
                    confianza = 0.85
                )
            )
        )

        assertEquals(
            90,
            RecognitionUtils.obtenerConfianza(
                ReconocimientoResponse(
                    confianza = 90.0
                )
            )
        )

        assertEquals(
            100,
            RecognitionUtils.obtenerConfianza(
                ReconocimientoResponse(
                    confianza = 150.0
                )
            )
        )

        assertNull(
            RecognitionUtils.obtenerConfianza(
                ReconocimientoResponse(
                    confianza = Double.NaN
                )
            )
        )
    }

    @Test
    fun rotaPuntosConservandoElOrigen() {
        val puntos = listOf(
            PuntoManoDto(1f, 1f, 0f),
            PuntoManoDto(2f, 1f, 0.5f)
        )

        val resultado = RecognitionUtils.rotarPuntos(
            puntos = puntos,
            grados = 90.0
        )

        assertEquals(puntos[0], resultado[0])

        assertEquals(
            1f,
            resultado[1].x,
            0.0001f
        )

        assertEquals(
            2f,
            resultado[1].y,
            0.0001f
        )

        assertEquals(
            0.5f,
            resultado[1].z,
            0.0001f
        )
    }
}