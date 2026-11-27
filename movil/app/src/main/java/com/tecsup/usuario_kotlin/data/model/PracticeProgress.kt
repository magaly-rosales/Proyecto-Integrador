package com.tecsup.usuario_kotlin.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PracticeProgress(
    val senaId: Int,
    val nombreSena: String,
    val practicasExitosas: Long = 1L,
    val ultimaPracticaMillis: Long = System.currentTimeMillis()
) {
    val fechaFormateada: String
        get() = SimpleDateFormat(
            "dd/MM/yyyy · HH:mm",
            Locale("es", "PE")
        ).format(Date(ultimaPracticaMillis))
}