package com.tecsup.usuario_kotlin.data.model

enum class TextSize(
    val label: String,
    val scale: Float
) {
    NORMAL(
        label = "Normal",
        scale = 1.0f
    ),

    LARGE(
        label = "Grande",
        scale = 1.15f
    ),

    EXTRA_LARGE(
        label = "Extra grande",
        scale = 1.30f
    );

    companion object {
        fun fromName(value: String?): TextSize {
            return values().firstOrNull {
                it.name == value
            } ?: NORMAL
        }
    }
}