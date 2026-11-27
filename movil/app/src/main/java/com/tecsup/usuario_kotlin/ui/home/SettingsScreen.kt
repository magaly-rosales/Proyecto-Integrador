package com.tecsup.usuario_kotlin.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.usuario_kotlin.data.model.OutputMode
import com.tecsup.usuario_kotlin.data.repository.TranslationPreferencesRepository
import com.tecsup.usuario_kotlin.ui.components.ScreenHeader
import com.tecsup.usuario_kotlin.ui.components.SecondaryButton
import com.tecsup.usuario_kotlin.ui.home.components.TextSizeDialog
import com.tecsup.usuario_kotlin.ui.theme.ProfileColors

@Composable
fun SettingsScreen(
    preferencesRepository: TranslationPreferencesRepository,
    onBackClick: () -> Unit
) {
    val outputMode by preferencesRepository.outputMode
        .collectAsStateWithLifecycle()

    val darkMode by preferencesRepository.darkMode
        .collectAsStateWithLifecycle()

    val textSize by preferencesRepository.textSize
        .collectAsStateWithLifecycle()

    var showTextSizeDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showResetDialog by rememberSaveable {
        mutableStateOf(false)
    }

    BackHandler(onBack = onBackClick)

    // Diálogo de tamaño de texto
    if (showTextSizeDialog) {
        TextSizeDialog(
            selectedSize = textSize,
            onConfirm = { size ->
                preferencesRepository.cambiarTamanoTexto(size)
                showTextSizeDialog = false
            },
            onDismiss = {
                showTextSizeDialog = false
            }
        )
    }

    // Confirmación para restablecer preferencias
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = {
                showResetDialog = false
            },
            containerColor = ProfileColors.CardDark,
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                    tint = ProfileColors.AccentCyan
                )
            },
            title = {
                Text(
                    text = "¿Restablecer preferencias?",
                    color = ProfileColors.TextPrimary
                )
            },
            text = {
                Text(
                    text = "Se recuperarán el modo Ambos, " +
                            "el tema oscuro y el tamaño de texto normal. " +
                            "Tu historial y tus datos no se eliminarán.",
                    color = ProfileColors.TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        preferencesRepository.restablecerPreferencias()
                        showResetDialog = false
                    }
                ) {
                    Text(
                        text = "Restablecer",
                        color = ProfileColors.AccentCyan
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                    }
                ) {
                    Text(
                        text = "Cancelar",
                        color = ProfileColors.TextSecondary
                    )
                }
            }
        )
    }

    Scaffold(
        containerColor = ProfileColors.BgDark
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ScreenHeader(
                title = "Configuración",
                onBackClick = onBackClick
            )

            Text(
                text = "Personaliza SeñasApp según tus preferencias.",
                color = ProfileColors.TextSecondary,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )

            // Preferencias del traductor
            SettingsSection(title = "TRADUCCIÓN") {
                SettingsTitle(
                    icon = Icons.AutoMirrored.Rounded.VolumeUp,
                    title = "Modo de salida"
                )

                Column(
                    modifier = Modifier.selectableGroup()
                ) {
                    OutputMode.values().forEach { mode ->

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = outputMode == mode,
                                    onClick = {
                                        preferencesRepository
                                            .cambiarModoSalida(mode)
                                    },
                                    role = Role.RadioButton
                                )
                                .heightIn(min = 48.dp)
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = outputMode == mode,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor =
                                        ProfileColors.AccentCyan,
                                    unselectedColor =
                                        ProfileColors.TextSecondary
                                )
                            )

                            Spacer(Modifier.width(10.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = mode.label,
                                    color = ProfileColors.TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Text(
                                    text = when (mode) {
                                        OutputMode.TEXT ->
                                            "Mostrar y guardar sin audio."

                                        OutputMode.VOICE ->
                                            "Reproducir mediante voz."

                                        OutputMode.BOTH ->
                                            "Mostrar texto y reproducir voz."
                                    },
                                    color = ProfileColors.TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Preferencias visuales y accesibilidad
            SettingsSection(
                title = "APARIENCIA Y ACCESIBILIDAD"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DarkMode,
                        contentDescription = null,
                        tint = ProfileColors.AccentCyan
                    )

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Modo oscuro",
                            color = ProfileColors.TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = if (darkMode) {
                                "Tema oscuro activado"
                            } else {
                                "Tema claro activado"
                            },
                            color = ProfileColors.TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Switch(
                        checked = darkMode,
                        onCheckedChange = {
                            preferencesRepository.cambiarModoOscuro(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ProfileColors.BgDark,
                            checkedTrackColor = ProfileColors.AccentCyan,
                            uncheckedThumbColor =
                                ProfileColors.TextSecondary,
                            uncheckedTrackColor =
                                ProfileColors.CardElevated
                        )
                    )
                }

                HorizontalDivider(
                    color = ProfileColors.CardElevated
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showTextSizeDialog = true
                        }
                        .heightIn(min = 48.dp)
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.TextFields,
                        contentDescription = null,
                        tint = ProfileColors.AccentCyan
                    )

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tamaño del texto",
                            color = ProfileColors.TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = "Ajusta la lectura de la aplicación.",
                            color = ProfileColors.TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = textSize.label,
                        color = ProfileColors.AccentCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Vista previa del tamaño actual
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = ProfileColors.CardElevated,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "VISTA PREVIA",
                            color = ProfileColors.TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = "Hola, ¿cómo estás?",
                            color = ProfileColors.TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Restablecer valores originales
            SecondaryButton(
                text = "Restablecer preferencias",
                onClick = {
                    showResetDialog = true
                }
            )

            Text(
                text = "Tus preferencias se guardan automáticamente " +
                        "en este dispositivo para tu cuenta.",
                color = ProfileColors.TextSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

// Contenedor reutilizable de configuración
@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            color = ProfileColors.TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = ProfileColors.CardDark,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(
                1.dp,
                ProfileColors.CardElevated
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content
            )
        }
    }
}

// Encabezado reutilizable
@Composable
private fun SettingsTitle(
    icon: ImageVector,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ProfileColors.AccentCyan,
            modifier = Modifier.size(22.dp)
        )

        Spacer(Modifier.width(12.dp))

        Text(
            text = title,
            color = ProfileColors.TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}