package com.tecsup.usuario_kotlin.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import java.util.Locale
import java.util.UUID

class TextToSpeechHelper(context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var engine: TextToSpeech? = null
    private var closed = false

    var isReady = false
        private set

    init {
        engine = TextToSpeech(context.applicationContext) { status ->
            mainHandler.post {
                if (!closed && status == TextToSpeech.SUCCESS) {
                    configurarIdioma()
                }
            }
        }
    }

    // Configurar voz en español
    private fun configurarIdioma() {
        val tts = engine ?: return

        var resultado = tts.setLanguage(Locale("es", "PE"))

        // Alternativa si el español de Perú no está disponible
        if (resultado < TextToSpeech.LANG_AVAILABLE) {
            resultado = tts.setLanguage(Locale("es"))
        }

        isReady = resultado >= TextToSpeech.LANG_AVAILABLE
    }

    // Reproducir una frase
    fun speak(text: String): Boolean {
        if (closed || !isReady || text.isBlank()) return false

        val resultado = engine?.speak(
            text.trim(),
            TextToSpeech.QUEUE_FLUSH,
            null,
            UUID.randomUUID().toString()
        )

        return resultado == TextToSpeech.SUCCESS
    }

    // Detener la reproducción
    fun stop() {
        if (!closed) {
            engine?.stop()
        }
    }

    // Liberar los recursos del motor
    fun close() {
        if (closed) return

        closed = true
        isReady = false

        mainHandler.removeCallbacksAndMessages(null)

        engine?.stop()
        engine?.shutdown()
        engine = null
    }
}