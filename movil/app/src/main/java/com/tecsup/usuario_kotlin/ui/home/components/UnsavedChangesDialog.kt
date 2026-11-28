package com.tecsup.usuario_kotlin.ui.home.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tecsup.usuario_kotlin.ui.theme.ProfileColors

@Composable
fun UnsavedChangesDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    enabled: Boolean = true
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ProfileColors.CardDark,
        titleContentColor = ProfileColors.TextPrimary,
        textContentColor = ProfileColors.TextSecondary,
        shape = RoundedCornerShape(18.dp),
        title = {
            Text(
                text = "Cambios sin guardar",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Tienes cambios pendientes en tus datos personales. " +
                        "Si continúas, estos cambios no se guardarán."
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = enabled
            ) {
                Text(
                    text = "Continuar",
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