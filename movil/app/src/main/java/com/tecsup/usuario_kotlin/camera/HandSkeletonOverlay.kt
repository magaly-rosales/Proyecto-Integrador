package com.tecsup.usuario_kotlin.camera

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.tecsup.usuario_kotlin.data.model.PuntoManoDto

private const val LANDMARKS_PER_HAND = 21

// Conexiones correspondientes a una mano
private val HAND_CONNECTIONS = listOf(

    // Pulgar
    0 to 1,
    1 to 2,
    2 to 3,
    3 to 4,

    // Índice
    0 to 5,
    5 to 6,
    6 to 7,
    7 to 8,

    // Medio
    5 to 9,
    9 to 10,
    10 to 11,
    11 to 12,

    // Anular
    9 to 13,
    13 to 14,
    14 to 15,
    15 to 16,

    // Meñique
    13 to 17,
    17 to 18,
    18 to 19,
    19 to 20,

    // Base de la palma
    0 to 17
)

@Composable
fun HandSkeletonOverlay(
    landmarks: List<PuntoManoDto>,
    isFrontCamera: Boolean,
    modifier: Modifier = Modifier
) {

    // Verificar que exista al menos una mano completa
    if (landmarks.size < LANDMARKS_PER_HAND) {
        return
    }

    // Separar los puntos de cada mano
    val hands = landmarks
        .chunked(LANDMARKS_PER_HAND)
        .filter { hand ->
            hand.size == LANDMARKS_PER_HAND
        }

    val lineColor = Color(0xFF22B8CF)
    val pointInnerColor = Color.White

    Canvas(modifier = modifier) {

        // Convertir coordenadas de MediaPipe
        // a coordenadas de la pantalla
        fun toOffset(punto: PuntoManoDto): Offset {

            val normX = if (isFrontCamera) {
                1f - punto.x
            } else {
                punto.x
            }

            return Offset(
                x = normX.coerceIn(0f, 1f) * size.width,
                y = punto.y.coerceIn(0f, 1f) * size.height
            )
        }

        // Dibujar las conexiones de todas las manos
        hands.forEach { hand ->

            HAND_CONNECTIONS.forEach {
                    (startIdx, endIdx) ->

                drawLine(
                    color = lineColor.copy(alpha = 0.85f),

                    start = toOffset(
                        hand[startIdx]
                    ),

                    end = toOffset(
                        hand[endIdx]
                    ),

                    strokeWidth = 2.5.dp.toPx(),

                    cap = StrokeCap.Round
                )
            }
        }

        // Dibujar los puntos de todas las manos
        hands.forEach { hand ->

            hand.forEach { punto ->

                val center = toOffset(punto)

                drawCircle(
                    color = lineColor,
                    radius = 5.dp.toPx(),
                    center = center
                )

                drawCircle(
                    color = pointInnerColor,
                    radius = 2.5.dp.toPx(),
                    center = center
                )
            }
        }
    }
}