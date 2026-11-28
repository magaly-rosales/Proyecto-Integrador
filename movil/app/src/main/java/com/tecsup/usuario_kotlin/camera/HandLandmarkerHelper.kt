package com.tecsup.usuario_kotlin.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.tecsup.usuario_kotlin.data.model.PuntoManoDto

class HandLandmarkerHelper(context: Context) {

    private val detector: HandLandmarker

    private var ultimoTimestamp = 0L

    // Información de cada mano detectada
    private data class ManoDetectada(
        val etiqueta: String,
        val puntos: List<PuntoManoDto>
    )

    init {

        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("hand_landmarker.task")
            .build()

        val options =
            HandLandmarker.HandLandmarkerOptions
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

    // Detectar una o dos manos mediante MediaPipe
    fun detectar(
        imageProxy: ImageProxy
    ): List<List<PuntoManoDto>> {

        val original = imageProxy.toBitmap()

        val rotation =
            imageProxy.imageInfo.rotationDegrees

        // Corregir la orientación del fotograma
        val bitmap = if (rotation == 0) {

            original

        } else {

            Bitmap.createBitmap(
                original,
                0,
                0,
                original.width,
                original.height,
                Matrix().apply {
                    postRotate(rotation.toFloat())
                },
                true
            )
        }

        val imagen =
            BitmapImageBuilder(bitmap).build()

        // Garantizar timestamps consecutivos
        val timestamp = maxOf(
            SystemClock.uptimeMillis(),
            ultimoTimestamp + 1
        )

        ultimoTimestamp = timestamp

        // Ejecutar reconocimiento de manos
        val resultado = try {

            detector.detectForVideo(
                imagen,
                timestamp
            )

        } finally {

            imagen.close()

            if (bitmap !== original) {
                original.recycle()
            }
        }

        val etiquetas = resultado.handedness()

        // Obtener los puntos y la etiqueta de cada mano
        val manosDetectadas =
            resultado.landmarks()
                .mapIndexed { index, mano ->

                    val etiqueta =
                        etiquetas.getOrNull(index)
                            ?.maxByOrNull { it.score() }
                            ?.categoryName()
                            .orEmpty()

                    val puntos = mano.map { punto ->

                        PuntoManoDto(
                            x = punto.x(),
                            y = punto.y(),
                            z = punto.z()
                        )
                    }

                    ManoDetectada(
                        etiqueta = etiqueta,
                        puntos = puntos
                    )
                }
                .filter { mano ->

                    mano.puntos.size == 21 &&
                            mano.puntos.all { punto ->

                                punto.x.isFinite() &&
                                        punto.y.isFinite() &&
                                        punto.z.isFinite()
                            }
                }

        // Mantener el mismo orden utilizado en la web:
        // primero Left y después Right
        val manosOrdenadas =
            manosDetectadas.sortedWith(
                compareBy<ManoDetectada> { mano ->

                    when (mano.etiqueta.lowercase()) {

                        "left" -> 0

                        "right" -> 1

                        else -> 2
                    }

                }.thenBy { mano ->

                    // Criterio alternativo cuando las
                    // etiquetas son iguales o desconocidas
                    mano.puntos.first().x
                }
            )

        // Conservar el formato esperado por CameraPreview
        return manosOrdenadas.map { mano ->

            mano.puntos
        }
    }

    // Liberar los recursos de MediaPipe
    fun cerrar() {

        detector.close()
    }
}