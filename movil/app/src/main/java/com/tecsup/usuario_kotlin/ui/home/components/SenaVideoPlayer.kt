package com.tecsup.usuario_kotlin.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.tecsup.usuario_kotlin.ui.theme.ProfileColors

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun SenaVideoPlayer(
    videoUrl: String,
    onError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Mantener actualizado el evento de error
    val currentOnError by rememberUpdatedState(onError)

    // Estado del reproductor
    var player by remember(videoUrl, lifecycleOwner) {
        mutableStateOf<ExoPlayer?>(null)
    }

    // Controlar el ciclo de vida del video
    DisposableEffect(
        videoUrl,
        lifecycleOwner,
        context
    ) {
        var activePlayer: ExoPlayer? = null
        var playbackPosition = 0L

        val listener = object : Player.Listener {

            override fun onPlayerError(
                error: PlaybackException
            ) {
                currentOnError()
            }
        }

        // Crear y preparar el reproductor
        fun iniciarReproductor() {

            if (activePlayer != null) return

            val nuevoPlayer = ExoPlayer.Builder(
                context.applicationContext
            ).build()

            activePlayer = nuevoPlayer

            nuevoPlayer.apply {

                addListener(listener)

                setMediaItem(
                    MediaItem.fromUri(videoUrl)
                )

                // El usuario inicia la reproducción
                playWhenReady = false

                // Permitir repetir la seña manualmente
                repeatMode = Player.REPEAT_MODE_OFF

                seekTo(playbackPosition)

                prepare()
            }

            player = nuevoPlayer
        }

        // Pausar el video
        fun pausarReproductor() {
            activePlayer?.pause()
        }

        // Liberar los recursos multimedia
        fun liberarReproductor() {

            activePlayer?.let { reproductor ->

                playbackPosition =
                    reproductor.currentPosition

                reproductor.removeListener(listener)

                reproductor.release()
            }

            activePlayer = null
            player = null
        }

        // Observar el ciclo de vida de la aplicación
        val observer = LifecycleEventObserver { _, event ->

            when (event) {

                Lifecycle.Event.ON_START -> {
                    iniciarReproductor()
                }

                Lifecycle.Event.ON_PAUSE -> {
                    pausarReproductor()
                }

                Lifecycle.Event.ON_STOP -> {
                    liberarReproductor()
                }

                Lifecycle.Event.ON_DESTROY -> {
                    liberarReproductor()
                }

                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        // Inicializar si la pantalla ya está visible
        if (
            lifecycleOwner.lifecycle.currentState
                .isAtLeast(Lifecycle.State.STARTED)
        ) {
            iniciarReproductor()
        }

        // Limpiar recursos al salir de la pantalla
        onDispose {

            lifecycleOwner.lifecycle
                .removeObserver(observer)

            liberarReproductor()
        }
    }

    // Contenedor visual del reproductor
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {

        if (player == null) {

            CircularProgressIndicator(
                color = ProfileColors.AccentCyan,
                modifier = Modifier.size(30.dp),
                strokeWidth = 3.dp
            )

        } else {

            AndroidView(
                factory = { viewContext ->

                    PlayerView(viewContext).apply {

                        useController = true

                        // Evitar recortar partes del video
                        resizeMode =
                            AspectRatioFrameLayout.RESIZE_MODE_FIT

                        // Mostrar controles inicialmente
                        controllerShowTimeoutMs = 3000

                        setShowBuffering(
                            PlayerView.SHOW_BUFFERING_ALWAYS
                        )

                        showController()
                    }
                },
                update = { playerView ->

                    if (playerView.player !== player) {

                        playerView.player = player

                        playerView.showController()
                    }
                },
                onRelease = { playerView ->

                    playerView.player = null
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}