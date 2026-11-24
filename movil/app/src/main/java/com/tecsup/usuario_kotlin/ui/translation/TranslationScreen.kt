
package com.tecsup.usuario_kotlin.ui.translation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.tecsup.usuario_kotlin.camera.CameraPreview
import com.tecsup.usuario_kotlin.viewmodel.TranslationViewModel

@Composable
fun TranslationScreen(
    viewModel: TranslationViewModel
) {
    val currentSign by viewModel.translatedSign.collectAsState()
    val context = LocalContext.current

    var cameraError by remember {
        mutableStateOf<String?>(null)
    }

    var handCount by remember {
        mutableIntStateOf(0)
    }

    var pointCount by remember {
        mutableIntStateOf(0)
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val handStatus = when (handCount) {
        0 -> "No se detectan manos"
        1 -> "1 mano detectada ($pointCount puntos)"
        else -> "2 manos detectadas ($pointCount puntos)"
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Traductor de Señas en Vivo",
                style = MaterialTheme.typography.headlineMedium
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                if (hasCameraPermission) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CameraPreview(
                            modifier = Modifier.fillMaxSize(),
                            onHandsDetected = { manos ->
                                handCount = manos.size
                                pointCount = manos.sumOf {
                                    it.size
                                }
                            },
                            onError = { error ->
                                cameraError = error
                            }
                        )

                        cameraError?.let { error ->
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = {
                                permissionLauncher.launch(
                                    Manifest.permission.CAMERA
                                )
                            }
                        ) {
                            Text("Permitir acceso a la cámara")
                        }
                    }
                }
            }

            Text(
                text = handStatus,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Seña detectada: $currentSign",
                style = MaterialTheme.typography.bodyLarge
            )

            Button(
                onClick = {
                    viewModel.updateTranslation("¡Hola!")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Probar seña")
            }
        }
    }
}