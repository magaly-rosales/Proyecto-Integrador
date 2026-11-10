package com.tecsup.usuario_kotlin.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val SenasCyan = Color(0xFF14A3A7)

private val DarkColorScheme = darkColorScheme(
    primary = SenasCyan,
    background = Color(0xFF06141B),
    surface = Color(0xFF091A23),
    onPrimary = Color(0xFF06141B),
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = SenasCyan,
    background = Color(0xFFF4F6F8),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onBackground = Color(0xFF0C181E),
    onSurface = Color(0xFF0C181E)
)

@Composable
fun UsuariokotlinTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
