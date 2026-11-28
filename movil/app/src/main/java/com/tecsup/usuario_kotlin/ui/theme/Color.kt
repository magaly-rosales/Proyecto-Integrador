package com.tecsup.usuario_kotlin.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Colores originales de Material
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Estructura de la paleta de SeñasApp
internal data class SenasPalette(
    val background: Color,
    val card: Color,
    val elevated: Color,
    val accent: Color,
    val text: Color,
    val secondaryText: Color,
    val badge: Color,
    val error: Color,
    val success: Color
)

// Tema oscuro original
internal val DarkSenasPalette = SenasPalette(
    background = Color(0xFF08141A),
    card = Color(0xFF0F2129),
    elevated = Color(0xFF132A34),
    accent = Color(0xFF22B8CF),
    text = Color(0xFFFFFFFF),
    secondaryText = Color(0xFF8A9BA8),
    badge = Color(0xFFF5B82E),
    error = Color(0xFFFF8585),
    success = Color(0xFF4ADE80)
)

// Tema claro
internal val LightSenasPalette = SenasPalette(
    background = Color(0xFFF3F7F9),
    card = Color(0xFFFFFFFF),
    elevated = Color(0xFFDDE7EC),
    accent = Color(0xFF087D91),
    text = Color(0xFF14232D),
    secondaryText = Color(0xFF536875),
    badge = Color(0xFF8A5300),
    error = Color(0xFFB3261E),
    success = Color(0xFF17753D)
)

// Paleta disponible para los componentes Compose
internal val LocalSenasPalette = staticCompositionLocalOf {
    DarkSenasPalette
}

// Compatibilidad con los colores existentes
object ProfileColors {

    val BgDark: Color
        @Composable @ReadOnlyComposable
        get() = LocalSenasPalette.current.background

    val CardDark: Color
        @Composable @ReadOnlyComposable
        get() = LocalSenasPalette.current.card

    val CardElevated: Color
        @Composable @ReadOnlyComposable
        get() = LocalSenasPalette.current.elevated

    val AccentCyan: Color
        @Composable @ReadOnlyComposable
        get() = LocalSenasPalette.current.accent

    val TextPrimary: Color
        @Composable @ReadOnlyComposable
        get() = LocalSenasPalette.current.text

    val TextSecondary: Color
        @Composable @ReadOnlyComposable
        get() = LocalSenasPalette.current.secondaryText

    val BadgeYellow: Color
        @Composable @ReadOnlyComposable
        get() = LocalSenasPalette.current.badge

    val ErrorColor: Color
        @Composable @ReadOnlyComposable
        get() = LocalSenasPalette.current.error

    val SuccessColor: Color
        @Composable @ReadOnlyComposable
        get() = LocalSenasPalette.current.success

    val BorderColor: Color
        @Composable @ReadOnlyComposable
        get() = TextSecondary.copy(alpha = 0.35f)

    val DisabledBorderColor: Color
        @Composable @ReadOnlyComposable
        get() = TextSecondary.copy(alpha = 0.20f)

    val SecondaryButtonBorder: Color
        @Composable @ReadOnlyComposable
        get() = TextSecondary.copy(alpha = 0.5f)

    val ErrorBackground: Color
        @Composable @ReadOnlyComposable
        get() = ErrorColor.copy(alpha = 0.10f)

    val ErrorBorder: Color
        @Composable @ReadOnlyComposable
        get() = ErrorColor.copy(alpha = 0.35f)

    val DisabledAccent: Color
        @Composable @ReadOnlyComposable
        get() = AccentCyan.copy(alpha = 0.5f)
}