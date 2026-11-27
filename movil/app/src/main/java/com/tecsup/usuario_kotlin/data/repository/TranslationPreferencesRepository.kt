package com.tecsup.usuario_kotlin.data.repository

import android.content.Context
import com.tecsup.usuario_kotlin.data.model.OutputMode
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
        .getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

    private val outputModeKey = "output_mode_$userId"

    // Modo de salida almacenado
    private val _outputMode = MutableStateFlow(
        OutputMode.fromName(
            preferences.getString(outputModeKey, null)
        )
    )

    val outputMode = _outputMode.asStateFlow()

    // Actualizar y guardar la preferencia
    fun cambiarModoSalida(mode: OutputMode) {
        if (_outputMode.value == mode) return

        preferences.edit()
            .putString(outputModeKey, mode.name)
            .apply()

        _outputMode.value = mode
    }
}