package com.tecsup.usuario_kotlin.ui.translation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.usuario_kotlin.ui.theme.ProfileColors

@Composable
fun TranslationPhraseCard(
    words: List<String>,
    onSpeakClick: () -> Unit,
    onDeleteLastClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasWords = words.isNotEmpty()
    val phrase = words.joinToString(" ")

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = ProfileColors.CardDark,
        border = BorderStroke(
            1.dp,
            ProfileColors.CardElevated
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cabecera y cantidad de señas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TRADUCCIÓN",
                    color = ProfileColors.TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )

                Text(
                    text = if (words.size == 1) {
                        "1 seña"
                    } else {
                        "${words.size} señas"
                    },
                    color = ProfileColors.TextSecondary,
                    fontSize = 11.sp
                )
            }

            // Mostrar la frase completa
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                    contentDescription = null,
                    tint = ProfileColors.AccentCyan,
                    modifier = Modifier.size(22.dp)
                )

                Spacer(Modifier.width(10.dp))

                Text(
                    text = phrase.ifBlank { "Esperando seña..." },
                    color = ProfileColors.TextPrimary,
                    fontSize = 21.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            // Reproducción de voz y borrar última seña
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onSpeakClick,
                    enabled = hasWords,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ProfileColors.AccentCyan,
                        contentColor = ProfileColors.BgDark,
                        disabledContainerColor = ProfileColors.DisabledAccent
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = "Decir en voz alta",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                OutlinedButton(
                    onClick = onDeleteLastClick,
                    enabled = hasWords,
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp),
                    border = BorderStroke(
                        1.dp,
                        ProfileColors.CardElevated
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = ProfileColors.CardElevated
                            .copy(alpha = 0.5f),
                        contentColor = ProfileColors.AccentCyan
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Backspace,
                        contentDescription = "Borrar última seña",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Limpiar toda la frase
            TextButton(
                onClick = onClearClick,
                enabled = hasWords,
                modifier = Modifier.align(Alignment.End),
                contentPadding = PaddingValues(
                    horizontal = 8.dp,
                    vertical = 0.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(Modifier.width(6.dp))

                Text(
                    text = "Limpiar frase",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}