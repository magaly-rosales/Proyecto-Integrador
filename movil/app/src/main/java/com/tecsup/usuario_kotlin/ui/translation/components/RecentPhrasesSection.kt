package com.tecsup.usuario_kotlin.ui.translation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.usuario_kotlin.data.model.FraseReciente
import com.tecsup.usuario_kotlin.ui.theme.ProfileColors

// Sección completa del historial
fun LazyListScope.recentPhrasesSection(
    frases: List<FraseReciente>,
    maxPhrases: Int,
    onRepeat: (FraseReciente) -> Unit,
    onDelete: (String) -> Unit,
    onClearRequest: () -> Unit
) {
    item(key = "recent_header") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "FRASES RECIENTES",
                color = ProfileColors.TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                modifier = Modifier.weight(1f)
            )

            if (frases.isNotEmpty()) {
                TextButton(
                    onClick = onClearRequest,
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = null,
                        tint = ProfileColors.AccentCyan,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(Modifier.width(4.dp))

                    Text(
                        text = "Limpiar historial",
                        color = ProfileColors.AccentCyan,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }

    if (frases.isEmpty()) {
        item(key = "recent_empty") {
            Text(
                text = "Aún no hay traducciones recientes",
                color = ProfileColors.TextSecondary,
                fontSize = 13.sp
            )
        }
    }

    items(
        items = frases,
        key = { "recent_${it.id}" }
    ) { frase ->
        RecentPhraseItem(
            frase = frase,
            onRepeatClick = { onRepeat(frase) },
            onDeleteClick = { onDelete(frase.id) }
        )
    }

    if (frases.isNotEmpty()) {
        item(key = "recent_count") {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${frases.size} de $maxPhrases frases guardadas",
                    color = ProfileColors.TextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// Tarjeta individual
@Composable
private fun RecentPhraseItem(
    frase: FraseReciente,
    onRepeatClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = ProfileColors.CardDark,
        border = BorderStroke(1.dp, ProfileColors.CardElevated)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = frase.texto,
                    color = ProfileColors.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = frase.tiempo,
                    color = ProfileColors.TextSecondary,
                    fontSize = 11.sp
                )
            }

            IconButton(onClick = onRepeatClick) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = "Repetir frase",
                    tint = ProfileColors.AccentCyan
                )
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = "Eliminar frase",
                    tint = ProfileColors.TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Confirmación para eliminar todo el historial
@Composable
fun ClearRecentPhrasesDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ProfileColors.CardDark,
        icon = {
            Icon(
                imageVector = Icons.Outlined.DeleteOutline,
                contentDescription = null,
                tint = ProfileColors.AccentCyan
            )
        },
        title = {
            Text(
                text = "¿Limpiar historial?",
                color = ProfileColors.TextPrimary
            )
        },
        text = {
            Text(
                text = "Se eliminarán todas tus frases recientes. " +
                        "Esta acción no se puede deshacer.",
                color = ProfileColors.TextSecondary
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Eliminar",
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancelar",
                    color = ProfileColors.AccentCyan
                )
            }
        }
    )
}