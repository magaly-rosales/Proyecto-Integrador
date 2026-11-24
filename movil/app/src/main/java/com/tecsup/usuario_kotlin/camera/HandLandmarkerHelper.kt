package com.tecsup.usuario_kotlin.camera

import android.content.Context
import android.os.SystemClock
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.ImageProcessingOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.tecsup.usuario_kotlin.data.model.PuntoManoDto

class HandLandmarkerHelper(context: Context) {

    private val detector: HandLandmarker

    private var ultimoTimestamp = 0L

    init {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("hand_landmarker.task")
            .build()

        val options = HandLandmarker.HandLandmarkerOptions
            .builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.VIDEO)
            .setNumHands(2)
            .build()

        detector = HandLandmarker.createFromOptions(
            context.applicationContext,
            options
        )
    }

    // Devuelve 21 puntos por cada mano encontrada.
    fun detectar(
        imageProxy: ImageProxy
    ): List<List<PuntoManoDto>> {

        val bitmap = imageProxy.toBitmap()
        val imagen = BitmapImageBuilder(bitmap).build()

        val processingOptions = ImageProcessingOptions
            .builder()
            .setRotationDegrees(
                imageProxy.imageInfo.rotationDegrees
            )
            .build()

        val timestamp = maxOf(
            SystemClock.uptimeMillis(),
            ultimoTimestamp + 1
        )

        ultimoTimestamp = timestamp

        val resultado = detector.detectForVideo(
            imagen,
            processingOptions,
            timestamp
        )

        return resultado.landmarks().map { mano ->
            mano.map { punto ->
                PuntoManoDto(
                    x = punto.x(),
                    y = punto.y(),
                    z = punto.z()
                )
            }
        }
    }

    fun cerrar() {
        detector.close()
    }
}
