package com.tecsup.usuario_kotlin.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.usuario_kotlin.ui.theme.ProfileColors
import com.tecsup.usuario_kotlin.utils.decodificarFotoPerfil

@Composable
fun ProfileAvatar(
    userName: String,
    foto: String?,
    modifier: Modifier = Modifier,
    avatarSize: Dp = 84.dp,
    fontSize: TextUnit = 34.sp
) {
    val initial = userName
        .trim()
        .firstOrNull()
        ?.uppercase() ?: "U"

    // Decodificar nuevamente cuando cambie la fotografía
    val bitmap by key(foto) {
        produceState<Bitmap?>(
            initialValue = null,
            key1 = foto
        ) {
            value = decodificarFotoPerfil(foto)
        }
    }

    Box(
        modifier = modifier
            .size(avatarSize)
            .clip(CircleShape)
            .background(ProfileColors.AccentCyan),
        contentAlignment = Alignment.Center
    ) {
        val imagen = bitmap

        if (imagen != null) {

            Image(
                bitmap = imagen.asImageBitmap(),
                contentDescription = "Foto de perfil",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

        } else {

            Text(
                text = initial,
                color = ProfileColors.BgDark,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold
            )
        }
    }
}