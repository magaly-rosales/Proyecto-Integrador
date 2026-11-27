package com.tecsup.usuario_kotlin.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.usuario_kotlin.data.model.TextSize
import com.tecsup.usuario_kotlin.ui.theme.ProfileColors

@Composable
fun TextSizeDialog(
    selectedSize: TextSize,
    onConfirm: (TextSize) -> Unit,
    onDismiss: () -> Unit
) {
    // Selección temporal hasta confirmar
    var temporarySize by remember(selectedSize) {
        mutableStateOf(selectedSize)
    }

    // Evitar aplicar dos veces la escala en la vista previa
    val previewScale = temporarySize.scale / selectedSize.scale

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ProfileColors.CardDark,

        icon = {
            Icon(
                imageVector = Icons.Outlined.TextFields,
                contentDescription = null,
                tint = ProfileColors.AccentCyan,
                modifier = Modifier.size(30.dp)
            )
        },

        title = {
            Text(
                text = "Tamaño del texto",
                color = ProfileColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },

        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Selecciona el tamaño de texto que " +
                            "prefieras para la aplicación.",
                    color = ProfileColors.TextSecondary,
                    fontSize = 13.sp
                )

                // Opciones disponibles
                Column(
                    modifier = Modifier.selectableGroup()
                ) {
                    TextSize.values().forEach { size ->

                        val selected = temporarySize == size

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .selectable(
                                    selected = selected,
                                    onClick = {
                                        temporarySize = size
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selected,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = ProfileColors.AccentCyan,
                                    unselectedColor = ProfileColors.TextSecondary
                                )
                            )

                            Spacer(Modifier.width(10.dp))

                            Text(
                                text = size.label,
                                color = ProfileColors.TextPrimary,
                                fontSize = 15.sp,
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = "${(size.scale * 100).toInt()}%",
                                color = ProfileColors.TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                HorizontalDivider(
                    color = ProfileColors.CardElevated
                )

                // Vista previa del tamaño seleccionado
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ProfileColors.CardElevated)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "VISTA PREVIA",
                        color = ProfileColors.TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "SeñasApp",
                        color = ProfileColors.AccentCyan,
                        fontSize = (18f * previewScale).sp,
                        lineHeight = (24f * previewScale).sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Hola, ¿cómo estás?",
                        color = ProfileColors.TextPrimary,
                        fontSize = (20f * previewScale).sp,
                        lineHeight = (28f * previewScale).sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },

        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(temporarySize)
                }
            ) {
                Text(
                    text = "Aceptar",
                    color = ProfileColors.AccentCyan,
                    fontWeight = FontWeight.Bold
                )
            }
        },

        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancelar",
                    color = ProfileColors.TextSecondary
                )
            }
        }
    )
}