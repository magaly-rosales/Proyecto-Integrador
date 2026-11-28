package com.tecsup.usuario_kotlin.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.usuario_kotlin.data.model.SenaCatalogoDto
import com.tecsup.usuario_kotlin.ui.components.PrimaryButton
import com.tecsup.usuario_kotlin.ui.components.SecondaryButton
import com.tecsup.usuario_kotlin.ui.components.ScreenHeader
import com.tecsup.usuario_kotlin.ui.home.components.NuevaSenaBadge
import com.tecsup.usuario_kotlin.ui.home.components.SenaCatalogImage
import com.tecsup.usuario_kotlin.ui.home.components.SenaVideoPlayer
import com.tecsup.usuario_kotlin.ui.theme.ProfileColors
import com.tecsup.usuario_kotlin.utils.CatalogMediaUtils

@Composable
fun SenaDetailScreen(
    sena: SenaCatalogoDto,
    apiUrl: String?,
    onBackClick: () -> Unit,
    onPracticeClick: () -> Unit
) {
    // Controlar el retroceso de Android
    BackHandler {
        onBackClick()
    }

    Scaffold(
        containerColor = ProfileColors.BgDark
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = 20.dp,
                    vertical = 16.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(20.dp)
        ) {
            // Cabecera
            ScreenHeader(
                title = "Detalle de seña",
                onBackClick = onBackClick
            )

            // Video o imagen de referencia
            SenaReferenceMedia(
                sena = sena,
                apiUrl = apiUrl
            )

            // Nombre y categoría
            SenaDetailInformation(
                sena = sena
            )

            // Descripción completa
            SenaDescriptionCard(
                descripcion = sena.descripcion
            )

            // Acciones disponibles
            PrimaryButton(
                text = "Practicar seña",
                onClick = onPracticeClick
            )

            SecondaryButton(
                text = "Volver al catálogo",
                onClick = onBackClick
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// Contenido multimedia de referencia
@Composable
private fun SenaReferenceMedia(
    sena: SenaCatalogoDto,
    apiUrl: String?
) {
    // Obtener la dirección completa del video
    val videoUrl = remember(
        sena.video,
        apiUrl
    ) {
        if (apiUrl.isNullOrBlank()) {

            null

        } else {

            CatalogMediaUtils.resolverUrlMedia(
                ruta = sena.video,
                apiUrl = apiUrl
            )
        }
    }

    // Controlar errores de reproducción
    var videoError by remember(sena.id, videoUrl) {
        mutableStateOf(false)
    }

    val showVideo =
        videoUrl != null && !videoError

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement =
            Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = if (showVideo) {
                "Video de referencia"
            } else {
                "Referencia visual"
            },
            color = ProfileColors.TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )

        if (videoUrl != null && !videoError) {

            // Reproducir el video registrado
            SenaVideoPlayer(
                videoUrl = videoUrl,
                onError = {
                    videoError = true
                }
            )

            Text(
                text = "Reproduce el video para observar el movimiento de la seña.",
                color = ProfileColors.TextSecondary,
                fontSize = 12.sp
            )

        } else {

            // Mostrar imagen cuando no exista video
            // o cuando ocurra un error de reproducción
            SenaCatalogImage(
                imagenSena = sena.imagen,
                imagenCategoria = sena.imagenCategoria,
                apiUrl = apiUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                iconSize = 72.dp,
                cornerRadius = 16.dp,
                contentDescription =
                    "Imagen de referencia del catálogo"
            )

            if (videoError) {

                Text(
                    text = "No se pudo reproducir el video de referencia.",
                    color = ProfileColors.ErrorColor,
                    fontSize = 12.sp
                )

                TextButton(
                    onClick = {
                        videoError = false
                    }
                ) {
                    Text(
                        text = "Reintentar video",
                        color = ProfileColors.AccentCyan,
                        fontWeight = FontWeight.Medium
                    )
                }

            } else {

                Text(
                    text = "Video de referencia no disponible.",
                    color = ProfileColors.TextSecondary,
                    fontSize = 12.sp
                )
            }

            Text(
                text = "Si no hay una imagen propia de la seña, se muestra la de su categoría o un icono.",
                color = ProfileColors.TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

// Información principal de la seña
@Composable
private fun SenaDetailInformation(
    sena: SenaCatalogoDto
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = sena.nombre,
                color = ProfileColors.TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(
                    weight = 1f,
                    fill = false
                )
            )

            if (sena.nueva) {

                Spacer(modifier = Modifier.width(10.dp))

                NuevaSenaBadge()
            }
        }

        if (!sena.categoria.isNullOrBlank()) {

            Spacer(modifier = Modifier.height(12.dp))

            SenaCategoryBadge(
                categoria = sena.categoria
            )
        }
    }
}

// Etiqueta de categoría
@Composable
private fun SenaCategoryBadge(
    categoria: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(ProfileColors.CardElevated)
            .padding(
                horizontal = 14.dp,
                vertical = 8.dp
            )
    ) {
        Text(
            text = categoria,
            color = ProfileColors.AccentCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// Tarjeta de descripción completa
@Composable
private fun SenaDescriptionCard(
    descripcion: String?
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = ProfileColors.CardDark,
        border = BorderStroke(
            1.dp,
            ProfileColors.CardElevated
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Descripción",
                color = ProfileColors.TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = descripcion
                    ?.takeIf { it.isNotBlank() }
                    ?: "Esta seña todavía no cuenta con una descripción.",
                color = ProfileColors.TextSecondary,
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
        }
    }
}