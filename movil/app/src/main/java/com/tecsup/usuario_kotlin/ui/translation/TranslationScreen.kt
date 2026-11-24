package com.tecsup.usuario_kotlin.ui.translation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.tecsup.usuario_kotlin.viewmodel.TranslationViewModel

@Composable
fun TranslationScreen(viewModel: TranslationViewModel) {
    val currentSign by viewModel.translatedSign.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
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
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                if (hasCameraPermission) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.surfaceProvider = previewView.surfaceProvider
                                }
                                // Usa DEFAULT_FRONT_CAMERA para cámara frontal o DEFAULT_BACK_CAMERA para trasera
                                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview
                                )
                            }, ContextCompat.getMainExecutor(ctx))

                            previewView
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                            Text("Permitir acceso a la cámara")
                        }
                    }
                }
            }

            Text(
                text = "Seña detectada: $currentSign",
                style = MaterialTheme.typography.titleLarge
            )

            Button(
                onClick = { viewModel.updateTranslation("¡Hola!") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Probar seña")
            }
        }
    }
}