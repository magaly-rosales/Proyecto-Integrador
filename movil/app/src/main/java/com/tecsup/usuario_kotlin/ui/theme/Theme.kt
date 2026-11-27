package com.tecsup.usuario_kotlin.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.tecsup.usuario_kotlin.data.model.TextSize

private val DarkColorScheme = darkColorScheme(
    primary = DarkSenasPalette.accent,
    onPrimary = DarkSenasPalette.background,
    secondary = DarkSenasPalette.secondaryText,
    background = DarkSenasPalette.background,
    onBackground = DarkSenasPalette.text,
    surface = DarkSenasPalette.card,
    onSurface = DarkSenasPalette.text,
    surfaceVariant = DarkSenasPalette.elevated,
    onSurfaceVariant = DarkSenasPalette.secondaryText,
    error = DarkSenasPalette.error
)

private val LightColorScheme = lightColorScheme(
    primary = LightSenasPalette.accent,
    onPrimary = LightSenasPalette.background,
    secondary = LightSenasPalette.secondaryText,
    background = LightSenasPalette.background,
    onBackground = LightSenasPalette.text,
    surface = LightSenasPalette.card,
    onSurface = LightSenasPalette.text,
    surfaceVariant = LightSenasPalette.elevated,
    onSurfaceVariant = LightSenasPalette.secondaryText,
    error = LightSenasPalette.error
)

@Composable
fun UsuariokotlinTheme(
    darkTheme: Boolean = true,
    textSize: TextSize = TextSize.NORMAL,
    content: @Composable () -> Unit
) {
    val palette = if (darkTheme) {
        DarkSenasPalette
    } else {
        LightSenasPalette
    }

    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    // Conservar la densidad y accesibilidad del dispositivo
    val systemDensity = LocalDensity.current

    val adjustedDensity = if (textSize == TextSize.NORMAL) {
        systemDensity
    } else {
        Density(
            density = systemDensity.density,
            fontScale = systemDensity.fontScale * textSize.scale
        )
    }

    // Aplicar el tema y la escala de texto
    CompositionLocalProvider(
        LocalSenasPalette provides palette,
        LocalDensity provides adjustedDensity
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}