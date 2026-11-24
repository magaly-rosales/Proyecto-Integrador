
package com.tecsup.usuario_kotlin.camera

import android.os.Handler
import android.os.Looper

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
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

import com.tecsup.usuario_kotlin.data.model.PuntoManoDto

import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onHandsDetected: (List<List<PuntoManoDto>>) -> Unit = {},
    onError: (String?) -> Unit = {}
) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val handsCallback by rememberUpdatedState(onHandsDetected)
    val errorCallback by rememberUpdatedState(onError)

    // Vista previa de la webcam
    val previewView = remember(context) {
        PreviewView(context).apply {
            implementationMode =
                PreviewView.ImplementationMode.COMPATIBLE

            scaleType =
                PreviewView.ScaleType.FILL_CENTER
        }
    }

    DisposableEffect(lifecycleOwner, previewView) {

        val active = AtomicBoolean(true)
        val executor = Executors.newSingleThreadExecutor()
        val mainHandler = Handler(Looper.getMainLooper())

        var provider: ProcessCameraProvider? = null

        // Vista previa de CameraX
        val preview = Preview.Builder()
            .build()
            .apply {
                setSurfaceProvider(
                    previewView.surfaceProvider
                )
            }

        // Analizador de imágenes
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(
                ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
            )
            .build()

        // Inicializar MediaPipe sin cerrar la aplicación
        // si una biblioteca nativa no es compatible.
        val helper: HandLandmarkerHelper? = try {

            HandLandmarkerHelper(context)

        } catch (e: LinkageError) {

            errorCallback(
                "No se pudo cargar la biblioteca de MediaPipe: " +
                        (e.localizedMessage ?: "error nativo")
            )

            null

        } catch (e: Exception) {

            errorCallback(
                "No se pudo iniciar MediaPipe: " +
                        (e.localizedMessage ?: "error desconocido")
            )

            null
        }

        if (helper != null) {

            val errorDetectado = AtomicBoolean(false)

            imageAnalysis.setAnalyzer(executor) { imageProxy ->

                try {

                    if (active.get()) {

                        // Detectar manos y extraer sus 21 puntos
                        val manos = helper.detectar(imageProxy)

                        // Enviar resultados a Compose
                        mainHandler.post {

                            if (active.get()) {
                                handsCallback(manos)
                            }
                        }
                    }

                } catch (e: LinkageError) {

                    if (errorDetectado.compareAndSet(false, true)) {

                        mainHandler.post {

                            if (active.get()) {
                                errorCallback(
                                    "Error nativo de MediaPipe: " +
                                            (e.localizedMessage ?: "error")
                                )
                            }
                        }
                    }

                } catch (e: Exception) {

                    if (errorDetectado.compareAndSet(false, true)) {

                        mainHandler.post {

                            if (active.get()) {
                                errorCallback(
                                    "Error al detectar manos: " +
                                            (e.localizedMessage ?: "error")
                                )
                            }
                        }
                    }

                } finally {

                    // Siempre liberar el fotograma
                    imageProxy.close()
                }
            }
        }

        // Obtener proveedor de CameraX
        val cameraFuture =
            ProcessCameraProvider.getInstance(context)

        cameraFuture.addListener({

            if (active.get()) {

                try {

                    val cameraProvider = cameraFuture.get()

                    // Priorizar cámara trasera del emulador
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

                        errorCallback(
                            "No hay ninguna cámara disponible"
                        )

                    } else {

                        if (helper != null) {

                            // Cámara + MediaPipe
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                selector,
                                preview,
                                imageAnalysis
                            )

                            errorCallback(null)

                        } else {

                            // Mantener webcam aunque MediaPipe falle
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                selector,
                                preview
                            )
                        }

                        provider = cameraProvider
                    }

                } catch (e: Exception) {

                    errorCallback(
                        "Error de cámara: " +
                                (e.localizedMessage ?: "error")
                    )
                }
            }

        }, ContextCompat.getMainExecutor(context))

        onDispose {

            // Detener actualizaciones
            active.set(false)

            // Desactivar análisis
            imageAnalysis.clearAnalyzer()

            // Liberar usos de CameraX de esta pantalla
            provider?.let { cameraProvider ->

                runCatching {
                    cameraProvider.unbind(
                        preview,
                        imageAnalysis
                    )
                }
            }

            // Cerrar detector en el hilo de análisis
            executor.execute {
                try {
                    helper?.cerrar()
                } catch (_: Exception) {
                    // Evitar fallos al liberar el detector
                }
            }

            executor.shutdown()
        }
    }

    // Mostrar cámara en Compose
    AndroidView(
        factory = { previewView },
        modifier = modifier.fillMaxSize()
    )
}
