package com.tecsup.usuario_kotlin.data.model

enum class OutputMode(
    val label: String,
    val showsText: Boolean,
    val usesVoice: Boolean
) {
    TEXT(
        label = "Texto",
        showsText = true,
        usesVoice = false
    ),

    VOICE(
        label = "Voz",
        showsText = false,
        usesVoice = true
    ),

    BOTH(
        label = "Ambos",
        showsText = true,
        usesVoice = true
    );

    companion object {
        fun fromName(value: String?): OutputMode {
            return values().firstOrNull {
                it.name == value
            } ?: BOTH
        }
    }
}