package com.tecsup.usuario_kotlin.camera

import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onError: (String?) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val errorCallback by rememberUpdatedState(onError)

    val previewView = remember(context) {
        PreviewView(context).apply {
            implementationMode =
                PreviewView.ImplementationMode.COMPATIBLE

            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    DisposableEffect(lifecycleOwner, previewView) {

        var active = true
        var provider: ProcessCameraProvider? = null
        var previewUseCase: Preview? = null

        val future =
            ProcessCameraProvider.getInstance(context)

        future.addListener({

            if (active) {
                try {
                    val cameraProvider = future.get()

                    val selector = when {
                        cameraProvider.hasCamera(
                            CameraSelector.DEFAULT_BACK_CAMERA
                        ) -> CameraSelector.DEFAULT_BACK_CAMERA

                        cameraProvider.hasCamera(
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        ) -> CameraSelector.DEFAULT_FRONT_CAMERA

                        else -> null
                    }

                    if (selector == null) {
                        errorCallback("No se encontró una cámara")
                    } else {
                        val preview = Preview.Builder()
                            .build()
                            .apply {
                                setSurfaceProvider(
                                    previewView.surfaceProvider
                                )
                            }

                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            selector,
                            preview
                        )

                        provider = cameraProvider
                        previewUseCase = preview

                        errorCallback(null)
                    }

                } catch (e: Exception) {
                    errorCallback(
                        "Error de cámara: " +
                                (e.localizedMessage ?: "desconocido")
                    )
                }
            }

        }, ContextCompat.getMainExecutor(context))

        onDispose {
            active = false

            previewUseCase?.let { preview ->
                provider?.unbind(preview)
            }
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier.fillMaxSize()
    )
}
