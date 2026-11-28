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
fun DeletePhotoDialog(
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
                text = "Eliminar foto de perfil",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "¿Estás seguro de que deseas eliminar " +
                        "tu foto de perfil? Se volverá a mostrar " +
                        "la inicial de tu nombre."
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = enabled
            ) {
                Text(
                    text = "Eliminar",
                    color = ProfileColors.ErrorColor,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Cancelar",
                    color = ProfileColors.TextSecondary
                )
            }
        }
    )
}