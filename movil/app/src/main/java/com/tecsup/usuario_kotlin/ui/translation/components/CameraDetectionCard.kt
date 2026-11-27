package com.tecsup.usuario_kotlin.ui.translation.components

import androidx.camera.core.CameraSelector
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cameraswitch
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.usuario_kotlin.camera.CameraPreview
import com.tecsup.usuario_kotlin.camera.HandSkeletonOverlay
import com.tecsup.usuario_kotlin.data.model.PuntoManoDto
import com.tecsup.usuario_kotlin.ui.theme.ProfileColors

@Composable
fun CameraDetectionCard(
    hasCameraPermission: Boolean,
    screenActive: Boolean,
    cameraError: String?,
    landmarks: List<PuntoManoDto>,
    lensFacing: Int,
    detectedLabel: String,
    onRequestPermission: () -> Unit,
    onSwitchCamera: () -> Unit,
    onHandsDetected: (List<List<PuntoManoDto>>) -> Unit,
    onCameraError: (String?) -> Unit,
    onCameraDisposed: () -> Unit
) {
    val handDetected = landmarks.isNotEmpty()
    val disposeCallback by rememberUpdatedState(onCameraDisposed)

    // Color del marco según el tema seleccionado
    val frameColor = ProfileColors.AccentCyan.copy(alpha = 0.75f)

    val handStatus = when (landmarks.size) {
        0 -> "Buscando mano..."
        21 -> "1 mano detectada (21 puntos)"
        42 -> "2 manos detectadas (42 puntos)"
        else -> "Detectados ${landmarks.size} puntos"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(275.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(ProfileColors.CardDark)
            .border(
                1.5.dp,
                ProfileColors.AccentCyan.copy(alpha = 0.7f),
                RoundedCornerShape(20.dp)
            )
    ) {
        if (hasCameraPermission) {

            if (screenActive) {
                DisposableEffect(lensFacing) {
                    onDispose {
                        disposeCallback()
                    }
                }

                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    lensFacing = lensFacing,
                    onHandsDetected = onHandsDetected,
                    onError = onCameraError
                )

                HandSkeletonOverlay(
                    landmarks = landmarks,
                    isFrontCamera =
                        lensFacing == CameraSelector.LENS_FACING_FRONT,
                    modifier = Modifier.fillMaxSize()
                )
            }

            cameraError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(12.dp)
                )
            }

        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ProfileColors.AccentCyan,
                        contentColor = ProfileColors.BgDark
                    )
                ) {
                    Text(
                        text = "Permitir acceso a la cámara",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            // Estado de la mano
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .clip(RoundedCornerShape(50))
                    .background(
                        ProfileColors.CardElevated.copy(alpha = 0.85f)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (handDetected) ProfileColors.AccentCyan
                            else ProfileColors.TextSecondary
                        )
                )

                Spacer(Modifier.width(6.dp))

                Text(
                    text = handStatus,
                    color = ProfileColors.TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Cambiar cámara
            IconButton(
                onClick = onSwitchCamera,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        ProfileColors.CardElevated.copy(alpha = 0.85f)
                    )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Cameraswitch,
                    contentDescription = "Cambiar cámara frontal/trasera",
                    tint = ProfileColors.AccentCyan,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Marco y resultado del reconocimiento
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 10.dp)
                    .width(175.dp)
                    .height(185.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRoundRect(
                        color = frameColor,
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(
                                floatArrayOf(12f, 10f), 0f
                            )
                        ),
                        cornerRadius = CornerRadius(
                            14.dp.toPx(), 14.dp.toPx()
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-11).dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(ProfileColors.AccentCyan)
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = detectedLabel,
                        color = ProfileColors.BgDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}