package com.tecsup.usuario_kotlin.data.model

data class TranslationHistoryDto(
    val id: Long = 0,
    val phrase: String,
    val timestamp: String,
    val confidence: Double = 0.92
)

data class SignTranslationRequest(
    val landmarks: List<Float> = emptyList(),
    val signKey: String? = null
)

data class SignTranslationResponse(
    val phrase: String,
    val confidence: Double,
    val handDetected: Boolean = true
)
