
package com.tecsup.usuario_kotlin.camera

import android.os.Handler
import android.os.Looper

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
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

    // Cámara seleccionada por el usuario
    lensFacing: Int = CameraSelector.LENS_FACING_BACK,

    // Resultados de MediaPipe
    onHandsDetected: (List<List<PuntoManoDto>>) -> Unit = {},

    // Análisis adicional de fotogramas (opcional)
    onFrameAnalyzed: ((ImageProxy) -> Unit)? = null,

    // Mensajes de error
    onError: (String?) -> Unit = {}
) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val handsCallback by rememberUpdatedState(onHandsDetected)
    val frameCallback by rememberUpdatedState(onFrameAnalyzed)
    val errorCallback by rememberUpdatedState(onError)

    // Crear la vista de la cámara una sola vez
    val previewView = remember(context) {
        PreviewView(context).apply {
            implementationMode =
                PreviewView.ImplementationMode.COMPATIBLE

            scaleType =
                PreviewView.ScaleType.FILL_CENTER
        }
    }

    // Reiniciar CameraX cuando cambia la cámara frontal/trasera
    DisposableEffect(
        lifecycleOwner,
        previewView,
        lensFacing,
        onFrameAnalyzed != null
    ) {

        val active = AtomicBoolean(true)
        val executor = Executors.newSingleThreadExecutor()
        val mainHandler = Handler(Looper.getMainLooper())

        var provider: ProcessCameraProvider? = null

        // 1. Crear la vista previa de CameraX
        val preview = Preview.Builder()
            .build()
            .apply {
                setSurfaceProvider(
                    previewView.surfaceProvider
                )
            }

        // 2. Crear analizador de imágenes
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(
                ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
            )
            .build()

        // 3. Inicializar detector de manos de MediaPipe
        val helper: HandLandmarkerHelper? = try {

            HandLandmarkerHelper(context)

        } catch (e: LinkageError) {

            errorCallback(
                "Biblioteca de MediaPipe incompatible: " +
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

        // Evitar mostrar el mismo error muchas veces
        val errorDetectado = AtomicBoolean(false)

        // 4. Analizar fotogramas si MediaPipe está disponible
        // o si existe un analizador adicional
        val usarAnalisis =
            helper != null || onFrameAnalyzed != null

        if (usarAnalisis) {

            imageAnalysis.setAnalyzer(executor) { imageProxy ->

                try {

                    if (active.get()) {

                        // Callback adicional del equipo.
                        // No debe cerrar imageProxy ni usarlo
                        // después de retornar.
                        frameCallback?.invoke(imageProxy)

                        if (helper != null) {

                            // MediaPipe detecta las manos
                            val manos = helper.detectar(imageProxy)

                            // Actualizar Compose en el hilo principal
                            mainHandler.post {

                                if (active.get()) {
                                    handsCallback(manos)
                                }
                            }
                        }
                    }

                } catch (e: LinkageError) {

                    if (
                        errorDetectado.compareAndSet(
                            false,
                            true
                        )
                    ) {

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

                    if (
                        errorDetectado.compareAndSet(
                            false,
                            true
                        )
                    ) {

                        mainHandler.post {

                            if (active.get()) {
                                errorCallback(
                                    "Error analizando cámara: " +
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

        // 5. Obtener proveedor de CameraX
        val cameraFuture =
            ProcessCameraProvider.getInstance(context)

        cameraFuture.addListener({

            if (active.get()) {

                try {

                    val cameraProvider = cameraFuture.get()

                    // Cámara solicitada desde TranslationScreen
                    val preferred = if (
                        lensFacing ==
                        CameraSelector.LENS_FACING_FRONT
                    ) {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    } else {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    }

                    // Cámara alternativa
                    val alternative = if (
                        lensFacing ==
                        CameraSelector.LENS_FACING_FRONT
                    ) {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    } else {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    }

                    // 6. Elegir cámara disponible
                    val selector = when {

                        cameraProvider.hasCamera(preferred) -> {
                            preferred
                        }

                        cameraProvider.hasCamera(alternative) -> {
                            alternative
                        }

                        else -> null
                    }

                    if (selector == null) {

                        errorCallback(
                            "No hay cámaras disponibles"
                        )

                    } else {

                        // 7. Asociar los casos de uso al ciclo de vida
                        if (usarAnalisis) {

                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                selector,
                                preview,
                                imageAnalysis
                            )

                        } else {

                            // Mostrar webcam aunque MediaPipe falle
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                selector,
                                preview
                            )
                        }

                        provider = cameraProvider

                        // Limpiar el error solamente si
                        // MediaPipe inició correctamente
                        if (helper != null) {
                            errorCallback(null)
                        }

                        // Reiniciar el contador de manos
                        mainHandler.post {
                            if (active.get()) {
                                handsCallback(emptyList())
                            }
                        }
                    }

                } catch (e: Exception) {

                    errorCallback(
                        "Error al abrir la cámara: " +
                                (e.localizedMessage ?: "error")
                    )
                }
            }

        }, ContextCompat.getMainExecutor(context))

        // 8. Liberar recursos al salir o cambiar de cámara
        onDispose {

            active.set(false)

            // Detener análisis
            imageAnalysis.clearAnalyzer()

            // Liberar únicamente los usos de esta pantalla
            provider?.let { cameraProvider ->

                runCatching {
                    cameraProvider.unbind(
                        preview,
                        imageAnalysis
                    )
                }
            }

            // Cerrar MediaPipe después de los análisis pendientes
            executor.execute {

                try {
                    helper?.cerrar()
                } catch (_: Exception) {
                    // Evitar errores al liberar recursos
                } catch (_: LinkageError) {
                    // Evitar errores nativos durante el cierre
                }
            }

            executor.shutdown()
        }
    }

    // 9. Mostrar cámara en Jetpack Compose
    AndroidView(
        factory = { previewView },
        modifier = modifier.fillMaxSize()
    )
}
