package com.tecsup.usuario_kotlin.ui.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PanTool
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tecsup.usuario_kotlin.data.model.SenaCatalogoDto
import com.tecsup.usuario_kotlin.ui.theme.ProfileColors
import com.tecsup.usuario_kotlin.utils.CatalogMediaUtils

@Composable
fun SenaCatalogItemCard(
    sena: SenaCatalogoDto,
    apiUrl: String?,
    onPracticeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onPracticeClick),
        shape = RoundedCornerShape(16.dp),
        color = ProfileColors.CardDark,
        border = BorderStroke(
            1.dp,
            ProfileColors.CardElevated
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Miniatura de la seña
            SenaCatalogImage(
                imagenSena = sena.imagen,
                imagenCategoria = sena.imagenCategoria,
                apiUrl = apiUrl
            )

            // Información de la seña
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = sena.nombre,
                        color = ProfileColors.TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(
                            weight = 1f,
                            fill = false
                        )
                    )

                    if (sena.nueva) {
                        Spacer(modifier = Modifier.width(8.dp))
                        NuevaSenaBadge()
                    }
                }

                if (!sena.descripcion.isNullOrBlank()) {

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = sena.descripcion,
                        color = ProfileColors.TextSecondary,
                        fontSize = 13.sp,
                        maxLines = 2
                    )
                }

                if (!sena.categoria.isNullOrBlank()) {

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = sena.categoria,
                        color = ProfileColors.AccentCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// Imagen reutilizable para catálogo y detalles
@Composable
fun SenaCatalogImage(
    imagenSena: String?,
    imagenCategoria: String?,
    apiUrl: String?,
    modifier: Modifier = Modifier.size(46.dp),
    iconSize: Dp = 22.dp,
    cornerRadius: Dp = 12.dp,
    contentDescription: String? = null
) {
    // Obtener imágenes disponibles por prioridad
    val imagenes = remember(
        imagenSena,
        imagenCategoria,
        apiUrl
    ) {
        if (apiUrl.isNullOrBlank()) {

            emptyList()

        } else {

            CatalogMediaUtils.obtenerImagenes(
                imagenSena = imagenSena,
                imagenCategoria = imagenCategoria,
                apiUrl = apiUrl
            )
        }
    }

    var imageIndex by remember(imagenes) {
        mutableIntStateOf(0)
    }

    val currentImage = imagenes.getOrNull(imageIndex)

    var imageLoaded by remember(currentImage) {
        mutableStateOf(false)
    }

    val imageShape =
        RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .clip(imageShape)
            .background(ProfileColors.CardElevated)
            .border(
                width = 1.dp,
                color = ProfileColors.AccentCyan.copy(
                    alpha = 0.5f
                ),
                shape = imageShape
            ),
        contentAlignment = Alignment.Center
    ) {
        // Icono alternativo mientras carga o si falla
        if (!imageLoaded || currentImage == null) {

            Icon(
                imageVector = Icons.Rounded.PanTool,
                contentDescription = null,
                tint = ProfileColors.AccentCyan,
                modifier = Modifier.size(iconSize)
            )
        }

        // Descargar y mostrar imagen
        if (currentImage != null) {

            key(currentImage) {

                AsyncImage(
                    model = currentImage,
                    contentDescription = contentDescription,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(1.dp),
                    contentScale = ContentScale.Fit,
                    onLoading = {
                        imageLoaded = false
                    },
                    onSuccess = {
                        imageLoaded = true
                    },
                    onError = {

                        imageLoaded = false

                        // Intentar cargar la imagen alternativa
                        if (
                            imagenes.getOrNull(imageIndex) ==
                            currentImage
                        ) {
                            imageIndex++
                        }
                    }
                )
            }
        }
    }
}

// Etiqueta reutilizable de seña nueva
@Composable
fun NuevaSenaBadge(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(ProfileColors.BadgeYellow)
            .padding(
                horizontal = 6.dp,
                vertical = 2.dp
            )
    ) {
        Text(
            text = "NUEVA",
            color = ProfileColors.BgDark,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}