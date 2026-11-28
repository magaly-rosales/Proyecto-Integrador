package com.tecsup.usuario_kotlin.ui.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.usuario_kotlin.ui.theme.ProfileColors

@Composable
fun PhotoOptionsDialog(
    hasPhoto: Boolean,
    onSelectPhoto: () -> Unit,
    onDeletePhoto: () -> Unit,
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
                text = "Foto de perfil",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {
                PhotoOptionItem(
                    title = "Elegir de galería",
                    icon = Icons.Outlined.Image,
                    iconColor = ProfileColors.AccentCyan,
                    textColor = ProfileColors.TextPrimary,
                    enabled = enabled,
                    onClick = onSelectPhoto
                )

                if (hasPhoto) {
                    PhotoOptionItem(
                        title = "Eliminar foto",
                        icon = Icons.Outlined.Delete,
                        iconColor = ProfileColors.ErrorColor,
                        textColor = ProfileColors.ErrorColor,
                        enabled = enabled,
                        onClick = onDeletePhoto
                    )
                }
            }
        },
        confirmButton = {
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

// Componente interno para las opciones de fotografía
@Composable
private fun PhotoOptionItem(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    textColor: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = ProfileColors.CardElevated
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = enabled,
                    onClick = onClick
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = title,
                color = textColor,
                fontSize = 15.sp
            )
        }
    }
}