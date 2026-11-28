package com.tecsup.usuario_kotlin.utils

import com.tecsup.usuario_kotlin.data.model.OutputMode
import com.tecsup.usuario_kotlin.data.model.TextSize
import kotlinx.coroutines.CancellationException
import org.junit.Assert.*
import org.junit.Test
import java.net.ConnectException
import java.net.SocketTimeoutException

class PreferencesAndErrorsTest {

    @Test
    fun compruebaModosDeSalida() {
        assertTrue(OutputMode.TEXT.showsText)
        assertFalse(OutputMode.TEXT.usesVoice)

        assertFalse(OutputMode.VOICE.showsText)
        assertTrue(OutputMode.VOICE.usesVoice)

        assertTrue(OutputMode.BOTH.showsText)
        assertTrue(OutputMode.BOTH.usesVoice)
    }

    @Test
    fun recuperaModoDeSalidaPredeterminado() {
        assertEquals(
            OutputMode.VOICE,
            OutputMode.fromName("VOICE")
        )

        assertEquals(
            OutputMode.BOTH,
            OutputMode.fromName(null)
        )

        assertEquals(
            OutputMode.BOTH,
            OutputMode.fromName("INVALIDO")
        )
    }

    @Test
    fun compruebaEscalasDeTexto() {
        assertEquals(
            1.0f,
            TextSize.NORMAL.scale,
            0.001f
        )

        assertEquals(
            1.15f,
            TextSize.LARGE.scale,
            0.001f
        )

        assertEquals(
            1.30f,
            TextSize.EXTRA_LARGE.scale,
            0.001f
        )

        assertEquals(
            TextSize.NORMAL,
            TextSize.fromName(null)
        )

        assertEquals(
            TextSize.EXTRA_LARGE,
            TextSize.fromName("EXTRA_LARGE")
        )

        assertEquals(
            TextSize.NORMAL,
            TextSize.fromName("INVALIDO")
        )
    }

    @Test
    fun interpretaErroresDeConexion() {
        val mensaje = NetworkErrorUtils.mensaje(
            ConnectException("Detalle técnico")
        )

        assertTrue(
            mensaje.contains(
                "No se pudo conectar"
            )
        )

        assertFalse(
            mensaje.contains("Detalle técnico")
        )

        val timeout = NetworkErrorUtils.mensaje(
            SocketTimeoutException()
        )

        assertTrue(
            timeout.contains("tardó demasiado")
        )
    }

    @Test
    fun interpretaCodigosHttp() {
        assertTrue(
            NetworkErrorUtils.mensajeHttp(403)
                .contains("permiso")
        )

        assertTrue(
            NetworkErrorUtils.mensajeHttp(429)
                .contains("Demasiadas solicitudes")
        )

        assertTrue(
            NetworkErrorUtils.mensajeHttp(503)
                .contains("problema temporal")
        )
    }

    @Test
    fun utilizaMensajeSeguroAnteErrorDesconocido() {
        val mensaje = NetworkErrorUtils.mensaje(
            error = IllegalStateException(
                "Información interna"
            ),
            predeterminado =
                "No se pudo completar la operación"
        )

        assertEquals(
            "No se pudo completar la operación",
            mensaje
        )
    }

    @Test(expected = CancellationException::class)
    fun respetaCancelacionDeCorrutinas() {
        NetworkErrorUtils.mensaje(
            CancellationException()
        )
    }
}