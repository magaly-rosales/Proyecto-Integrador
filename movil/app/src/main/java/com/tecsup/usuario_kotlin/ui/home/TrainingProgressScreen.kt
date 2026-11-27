package com.tecsup.usuario_kotlin.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PanTool
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.usuario_kotlin.data.model.PracticeProgress
import com.tecsup.usuario_kotlin.ui.components.ScreenHeader
import com.tecsup.usuario_kotlin.ui.components.PrimaryButton
import com.tecsup.usuario_kotlin.ui.theme.ProfileColors
import kotlin.math.roundToInt

@Composable
fun TrainingProgressScreen(
    progress: List<PracticeProgress>,
    catalogIds: Set<Int>?,
    onBackClick: () -> Unit
) {
    val totalSenas = progress.size

    val totalPracticas = progress.sumOf {
        it.practicasExitosas
    }

    val totalCatalogo = catalogIds?.size ?: 0

    val completadasCatalogo = progress.count {
        catalogIds?.contains(it.senaId) == true
    }

    val porcentaje = if (totalCatalogo > 0) {
        (
                completadasCatalogo.toFloat() /
                        totalCatalogo
                ).coerceIn(0f, 1f)
    } else {
        0f
    }

    BackHandler(onBack = onBackClick)

    Scaffold(
        containerColor = ProfileColors.BgDark
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(
                top = 12.dp,
                bottom = 20.dp
            )
        ) {
            // Cabecera
            item(key = "header") {
                ScreenHeader(
                    title = "Mi progreso",
                    onBackClick = onBackClick
                )
            }

            item(key = "description") {
                Text(
                    text = "Consulta tus avances en el " +
                            "aprendizaje de lengua de señas.",
                    color = ProfileColors.TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }

            // Estadísticas generales
            item(key = "statistics") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {
                    ProgressStatisticCard(
                        title = "Señas practicadas con éxito",
                        value = totalSenas.toString(),
                        icon = Icons.Rounded.PanTool,
                        iconColor = ProfileColors.AccentCyan,
                        modifier = Modifier.weight(1f)
                    )

                    ProgressStatisticCard(
                        title = "Prácticas exitosas",
                        value = totalPracticas.toString(),
                        icon = Icons.Outlined.EmojiEvents,
                        iconColor = ProfileColors.SuccessColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Progreso respecto al catálogo completo
            item(key = "catalog_progress") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = ProfileColors.CardDark,
                    border = BorderStroke(
                        1.dp,
                        ProfileColors.CardElevated
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Progreso del catálogo",
                            color = ProfileColors.TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        if (totalCatalogo > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$completadasCatalogo de " +
                                            "$totalCatalogo señas",
                                    color = ProfileColors.TextSecondary,
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1f)
                                )

                                Text(
                                    text = "${
                                        (porcentaje * 100).roundToInt()
                                    }%",
                                    color = ProfileColors.AccentCyan,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            LinearProgressIndicator(
                                progress = { porcentaje },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(50)),
                                color = ProfileColors.AccentCyan,
                                trackColor = ProfileColors.CardElevated
                            )

                            Text(
                                text = "Señas del catálogo actual " +
                                        "practicadas correctamente.",
                                color = ProfileColors.TextSecondary,
                                fontSize = 12.sp
                            )
                        } else {
                            Text(
                                text = "El catálogo no está disponible " +
                                        "para calcular el porcentaje.",
                                color = ProfileColors.TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Encabezado del historial
            item(key = "history_header") {
                Text(
                    text = "HISTORIAL DE ENTRENAMIENTO",
                    color = ProfileColors.TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            }

            if (progress.isEmpty()) {
                item(key = "empty_progress") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = ProfileColors.CardDark
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment =
                                Alignment.CenterHorizontally,
                            verticalArrangement =
                                Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PanTool,
                                contentDescription = null,
                                tint = ProfileColors.AccentCyan,
                                modifier = Modifier.size(42.dp)
                            )

                            Text(
                                text = "Aún no tienes prácticas exitosas",
                                color = ProfileColors.TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Practica una seña del catálogo " +
                                        "para comenzar a registrar tus avances.",
                                color = ProfileColors.TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            } else {
                items(
                    items = progress,
                    key = { it.senaId }
                ) { registro ->
                    PracticeHistoryCard(registro)
                }
            }

            // Volver al catálogo
            item(key = "back_button") {
                PrimaryButton(
                    text = "Volver al catálogo",
                    onClick = onBackClick
                )
            }
        }
    }
}

// Tarjeta de estadísticas
@Composable
private fun ProgressStatisticCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = ProfileColors.CardDark,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            ProfileColors.CardElevated
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(28.dp)
            )

            Text(
                text = value,
                color = ProfileColors.TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = title,
                color = ProfileColors.TextSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

// Registro individual del entrenamiento
@Composable
private fun PracticeHistoryCard(
    registro: PracticeProgress
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ProfileColors.CardDark,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            1.dp,
            ProfileColors.CardElevated
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = "Práctica completada",
                tint = ProfileColors.SuccessColor,
                modifier = Modifier.size(30.dp)
            )

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = registro.nombreSena,
                    color = ProfileColors.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (registro.practicasExitosas == 1L) {
                        "1 práctica exitosa"
                    } else {
                        "${registro.practicasExitosas} prácticas exitosas"
                    },
                    color = ProfileColors.SuccessColor,
                    fontSize = 13.sp
                )

                Text(
                    text = "Última práctica: " +
                            registro.fechaFormateada,
                    color = ProfileColors.TextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}