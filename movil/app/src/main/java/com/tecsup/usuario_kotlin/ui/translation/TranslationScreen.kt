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

    // Resultado actual del traductor
    val currentSign by viewModel.translatedSign.collectAsState()
    val context = LocalContext.current

    // Mensaje de error de la cámara
    var cameraError by remember {
        mutableStateOf<String?>(null)
    }

    // Comprobar permiso de cámara
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Solicitar permiso al usuario
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(
                Manifest.permission.CAMERA
            )
        }
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

            // TÍTULO
            Text(
                text = "Traductor de Señas en Vivo",
                style = MaterialTheme.typography.headlineMedium
            )

            // ÁREA DE CÁMARA
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

                        // Cámara real mediante CameraX
                        CameraPreview(
                            modifier = Modifier.fillMaxSize(),
                            onError = { error ->
                                cameraError = error
                            }
                        )

                        // Mostrar errores si ocurren
                        cameraError?.let { error ->
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                } else {

                    // Mostrar botón si falta el permiso
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

            // RESULTADO DE RECONOCIMIENTO
            Text(
                text = "Seña detectada: $currentSign",
                style = MaterialTheme.typography.titleLarge
            )

            // BOTÓN TEMPORAL DE PRUEBA
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