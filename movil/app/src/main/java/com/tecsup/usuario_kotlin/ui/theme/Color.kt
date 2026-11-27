package com.tecsup.usuario_kotlin.ui.theme

import androidx.compose.ui.graphics.Color

// Colores originales del tema
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Paleta compartida de SeñasApp
object ProfileColors {

    val BgDark = Color(0xFF08141A)
    val CardDark = Color(0xFF0F2129)
    val CardElevated = Color(0xFF132A34)

    val AccentCyan = Color(0xFF22B8CF)

    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFF8A9BA8)

    val BadgeYellow = Color(0xFFF5B82E)
    val ErrorColor = Color(0xFFFF8585)

    val BorderColor = TextSecondary.copy(alpha = 0.35f)
    val DisabledBorderColor = TextSecondary.copy(alpha = 0.20f)
    val SecondaryButtonBorder = TextSecondary.copy(alpha = 0.5f)

    val ErrorBackground = ErrorColor.copy(alpha = 0.10f)
    val ErrorBorder = ErrorColor.copy(alpha = 0.35f)

    val DisabledAccent = AccentCyan.copy(alpha = 0.5f)
}