package com.tecsup.usuario_kotlin.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class FraseReciente(
    val id: String = UUID.randomUUID().toString(),
    val palabras: List<String>,
    val fechaMillis: Long = System.currentTimeMillis()
) {
    // Construir el texto completo
    val texto: String
        get() = palabras.joinToString(" ")

    // Formatear la fecha de almacenamiento
    val tiempo: String
        get() = SimpleDateFormat(
            "dd/MM/yyyy · HH:mm",
            Locale("es", "PE")
        ).format(Date(fechaMillis))
}