package com.tecsup.usuario_kotlin.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PhraseBuilder {

    companion object {
        private const val STATIC_CONFIRMATIONS = 3
        private const val RELEASE_INTERVAL_MS = 1000L
    }

    // Palabras acumuladas en la frase
    private val _words = MutableStateFlow<List<String>>(emptyList())
    val words = _words.asStateFlow()

    // Control de confirmaciones estáticas
    private var candidate: String? = null
    private var confirmations = 0

    // Evitar repetir una seña mantenida
    private var lastAccepted: String? = null
    private var absenceStartedAt: Long? = null

    // Procesar una seña estática
    fun registrarEstatica(nombre: String?): String? {
        if (!RecognitionUtils.esNombreValido(nombre)) {
            limpiarCandidato()
            return null
        }

        val sena = nombre?.trim() ?: return null

        if (candidate.equals(sena, ignoreCase = true)) {
            confirmations++
        } else {
            candidate = sena
            confirmations = 1
        }

        confirmations = confirmations
            .coerceAtMost(STATIC_CONFIRMATIONS)

        if (confirmations < STATIC_CONFIRMATIONS) {
            return null
        }

        return agregarSena(sena)
    }

    // Procesar una seña dinámica previamente confirmada
    fun registrarDinamica(nombre: String?): String? {
        if (!RecognitionUtils.esNombreValido(nombre)) {
            return null
        }

        limpiarCandidato()

        return agregarSena(nombre?.trim() ?: return null)
    }

    // Agregar una seña evitando duplicados consecutivos
    private fun agregarSena(nombre: String): String? {
        if (lastAccepted.equals(nombre, ignoreCase = true)) {
            return null
        }

        lastAccepted = nombre
        _words.value = _words.value + nombre

        return nombre
    }

    // Registrar que existen manos visibles
    fun registrarPresencia() {
        absenceStartedAt = null
    }

    // Permitir repetir una seña después de retirar las manos
    fun registrarAusencia(timestampMs: Long) {
        limpiarCandidato()

        val inicio = absenceStartedAt ?: timestampMs.also {
            absenceStartedAt = it
        }

        if (timestampMs - inicio >= RELEASE_INTERVAL_MS) {
            lastAccepted = null
        }
    }

    // Obtener la frase completa
    fun obtenerFrase(): String {
        return _words.value.joinToString(" ")
    }

    // Eliminar la última seña
    fun borrarUltima() {
        _words.value = _words.value.dropLast(1)
        limpiarCandidato()
    }

    // Limpiar la frase sin repetir inmediatamente
    // la seña que todavía está frente a la cámara
    fun limpiarFrase() {
        _words.value = emptyList()
        limpiarCandidato()
    }

    // Recuperar una frase reciente
    fun cargarFrase(palabras: List<String>) {
        _words.value = palabras
            .map { it.trim() }
            .filter { RecognitionUtils.esNombreValido(it) }

        limpiarCandidato()
    }

    // Reiniciar confirmaciones pendientes
    fun limpiarCandidato() {
        candidate = null
        confirmations = 0
    }

    // Reiniciar detección sin eliminar la frase
    fun reiniciarDeteccion() {
        limpiarCandidato()
        absenceStartedAt = null
    }

    // Limpiar completamente la sesión
    fun reiniciarSesion() {
        limpiarFrase()
        lastAccepted = null
        absenceStartedAt = null
    }
}