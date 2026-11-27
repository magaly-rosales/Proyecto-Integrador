package com.tecsup.usuario_kotlin.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class NotificationType {
    WELCOME,
    PRACTICE
}

data class AppNotification(
    val id: String = UUID.randomUUID().toString(),
    val titulo: String,
    val mensaje: String,
    val tipo: NotificationType,
    val fechaMillis: Long = System.currentTimeMillis(),
    val leida: Boolean = false
) {
    val fechaFormateada: String
        get() = SimpleDateFormat(
            "dd/MM/yyyy · HH:mm",
            Locale("es", "PE")
        ).format(Date(fechaMillis))
}