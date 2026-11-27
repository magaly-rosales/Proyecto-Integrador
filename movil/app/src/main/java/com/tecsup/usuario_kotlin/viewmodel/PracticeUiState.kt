package com.tecsup.usuario_kotlin.viewmodel

import com.tecsup.usuario_kotlin.data.model.PuntoManoDto

// Estados posibles durante la práctica
enum class PracticeStatus {
    IDLE,
    WAITING_HAND,
    ANALYZING,
    NOT_RECOGNIZED,
    DIFFERENT_SIGN,
    CONFIRMING,
    SUCCESS,
    ERROR
}

// Estado completo de la pantalla de práctica
data class PracticeUiState(

    // Seña que debe realizar el usuario
    val targetSign: String = "",

    // Seña reconocida por el servidor
    val detectedSign: String? = null,

    // Puntos detectados por MediaPipe
    val handLandmarks: List<PuntoManoDto> = emptyList(),

    // Estado actual de la práctica
    val status: PracticeStatus = PracticeStatus.IDLE,

    // Control de la sesión de práctica
    val isActive: Boolean = false,

    // Control de solicitudes al servidor
    val isAnalyzing: Boolean = false,

    // Confirmaciones consecutivas de la seña
    val confirmations: Int = 0,

    // Mensaje de error
    val errorMessage: String? = null

) {

    val handDetected: Boolean
        get() = handLandmarks.isNotEmpty()

    val practiceCompleted: Boolean
        get() = status == PracticeStatus.SUCCESS

    val handCount: Int
        get() = handLandmarks.size / 21
}