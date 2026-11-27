package com.tecsup.usuario_kotlin.data.repository

import android.content.Context
import com.tecsup.usuario_kotlin.data.model.OutputMode
import com.tecsup.usuario_kotlin.data.model.TextSize
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TranslationPreferencesRepository(
    context: Context,
    userId: Int
) {
    companion object {
        private const val PREFS_NAME = "senasapp_preferences"
    }

    init {
        require(userId > 0) {
            "Se requiere un usuario autenticado"
        }
    }

    private val preferences = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val outputModeKey = "output_mode_$userId"
    private val darkModeKey = "dark_mode_$userId"
    private val textSizeKey = "text_size_$userId"

    // Modo de salida
    private val _outputMode = MutableStateFlow(
        OutputMode.fromName(
            preferences.getString(outputModeKey, null)
        )
    )

    val outputMode = _outputMode.asStateFlow()

    // Tema visual
    private val _darkMode = MutableStateFlow(
        preferences.getBoolean(darkModeKey, true)
    )

    val darkMode = _darkMode.asStateFlow()

    // Tamaño del texto
    private val _textSize = MutableStateFlow(
        TextSize.fromName(
            preferences.getString(textSizeKey, null)
        )
    )

    val textSize = _textSize.asStateFlow()

    // Actualizar modo de salida
    fun cambiarModoSalida(mode: OutputMode) {
        if (_outputMode.value == mode) return

        preferences.edit()
            .putString(outputModeKey, mode.name)
            .apply()

        _outputMode.value = mode
    }

    // Actualizar tema visual
    fun cambiarModoOscuro(enabled: Boolean) {
        if (_darkMode.value == enabled) return

        preferences.edit()
            .putBoolean(darkModeKey, enabled)
            .apply()

        _darkMode.value = enabled
    }

    // Actualizar tamaño del texto
    fun cambiarTamanoTexto(size: TextSize) {
        if (_textSize.value == size) return

        preferences.edit()
            .putString(textSizeKey, size.name)
            .apply()

        _textSize.value = size
    }

    // Restablecer únicamente las preferencias del usuario actual
    fun restablecerPreferencias() {
        preferences.edit()
            .remove(outputModeKey)
            .remove(darkModeKey)
            .remove(textSizeKey)
            .apply()

        _outputMode.value = OutputMode.BOTH
        _darkMode.value = true
        _textSize.value = TextSize.NORMAL
    }
}