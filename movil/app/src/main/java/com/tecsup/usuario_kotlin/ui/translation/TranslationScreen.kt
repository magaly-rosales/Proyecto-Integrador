package com.tecsup.usuario_kotlin.ui.translation

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.usuario_kotlin.data.repository.RecentPhraseRepository
import com.tecsup.usuario_kotlin.data.repository.TranslationPreferencesRepository
import com.tecsup.usuario_kotlin.ui.theme.ProfileColors
import com.tecsup.usuario_kotlin.ui.translation.components.CameraDetectionCard
import com.tecsup.usuario_kotlin.ui.translation.components.ClearRecentPhrasesDialog
import com.tecsup.usuario_kotlin.ui.translation.components.SenasTopBar
import com.tecsup.usuario_kotlin.ui.translation.components.SenasBottomBar as SharedSenasBottomBar
import com.tecsup.usuario_kotlin.ui.translation.components.TranslationPhraseCard
import com.tecsup.usuario_kotlin.ui.translation.components.recentPhrasesSection
import com.tecsup.usuario_kotlin.utils.RecognitionUtils
import com.tecsup.usuario_kotlin.utils.SequenceBuffer
import com.tecsup.usuario_kotlin.utils.TextToSpeechHelper
import com.tecsup.usuario_kotlin.viewmodel.TranslationViewModel

@Composable
fun TranslationScreen(
    viewModel: TranslationViewModel,
    userId: Int,
    preferencesRepository: TranslationPreferencesRepository,
    userInitial: String = "M",
    notificationCount: Int = 3,
    onNavigateToSenas: () -> Unit = {},
    onNavigateToPerfil: () -> Unit = {},
    onSpeakText: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Estados del reconocimiento
    val currentSign by viewModel.translatedSign.collectAsStateWithLifecycle()
    val confidence by viewModel.confidence.collectAsStateWithLifecycle()
    val handLandmarks by viewModel.handLandmarks.collectAsStateWithLifecycle()
    val frameCount by viewModel.sequenceFrameCount.collectAsStateWithLifecycle()
    val recognizingSequence by viewModel.isRecognizingSequence.collectAsStateWithLifecycle()
    val dynamicRecognition by viewModel.isDynamicRecognition.collectAsStateWithLifecycle()
    val recognitionError by viewModel.errorMessage.collectAsStateWithLifecycle()
    val phraseWords by viewModel.phraseWords.collectAsStateWithLifecycle()

    val registrarSena by rememberUpdatedState(onSpeakText)

    // Historial persistente por usuario
    val recentPhraseRepository = remember(
        context.applicationContext,
        userId
    ) {
        RecentPhraseRepository(
            context.applicationContext,
            userId
        )
    }

    val frasesRecientes by recentPhraseRepository.frases
        .collectAsStateWithLifecycle()

    // Preferencias compartidas desde MainActivity
    val outputMode by preferencesRepository.outputMode
        .collectAsStateWithLifecycle()

    var showClearDialog by remember(userId) {
        mutableStateOf(false)
    }

    // Estados de cámara
    var cameraError by remember {
        mutableStateOf<String?>(null)
    }

    var lensFacing by remember {
        mutableIntStateOf(CameraSelector.LENS_FACING_BACK)
    }

    var screenActive by remember(lifecycleOwner) {
        mutableStateOf(
            lifecycleOwner.lifecycle.currentState
                .isAtLeast(Lifecycle.State.STARTED)
        )
    }

    // Permisos de cámara
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted

        if (!granted) {
            viewModel.detenerReconocimiento()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(
                Manifest.permission.CAMERA
            )
        }
    }

    // Motor de voz
    var speechHelper by remember {
        mutableStateOf<TextToSpeechHelper?>(null)
    }

    DisposableEffect(context.applicationContext) {
        val helper = TextToSpeechHelper(
            context.applicationContext
        )

        speechHelper = helper

        onDispose {
            helper.close()
            speechHelper = null
        }
    }

    // Ciclo de vida de la pantalla
    DisposableEffect(lifecycleOwner, viewModel, context) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    screenActive = true
                }

                Lifecycle.Event.ON_RESUME -> {
                    hasCameraPermission =
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED

                    if (!hasCameraPermission) {
                        viewModel.detenerReconocimiento()
                    }
                }

                Lifecycle.Event.ON_STOP -> {
                    screenActive = false
                    viewModel.detenerReconocimiento()
                    speechHelper?.stop()
                }

                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.cerrarSesionTraductor()
        }
    }

    // Ejecutar la salida según la preferencia actual
    fun ejecutarSalida(
        palabras: List<String>,
        desdeHistorial: Boolean = false
    ) {
        val frase = palabras.joinToString(" ").trim()

        if (frase.isBlank()) return

        val modoActual = preferencesRepository.outputMode.value

        // Modo texto: guardar sin reproducir audio
        if (!modoActual.usesVoice) {
            speechHelper?.stop()
            recentPhraseRepository.guardar(palabras)

            if (!desdeHistorial) {
                Toast.makeText(
                    context,
                    "Frase guardada",
                    Toast.LENGTH_SHORT
                ).show()
            }

            return
        }

        // Modos voz y ambos
        val helper = speechHelper

        if (helper == null || !helper.isReady) {
            Toast.makeText(
                context,
                "El motor de voz no está disponible",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (helper.speak(frase)) {
            recentPhraseRepository.guardar(palabras)
        } else {
            Toast.makeText(
                context,
                "No se pudo reproducir la frase",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Confirmación para eliminar el historial
    if (showClearDialog) {
        ClearRecentPhrasesDialog(
            onConfirm = {
                recentPhraseRepository.limpiar()
                showClearDialog = false
            },
            onDismiss = {
                showClearDialog = false
            }
        )
    }

    // Etiqueta de reconocimiento
    val badgeText = when {
        handLandmarks.isEmpty() ->
            "BUSCANDO SEÑA..."

        dynamicRecognition &&
                RecognitionUtils.esNombreValido(currentSign) ->
            "MOV. · ${currentSign.uppercase()}"

        recognizingSequence ->
            "ANALIZANDO MOV."

        (
                currentSign == "Esperando seña..." ||
                        !RecognitionUtils.esNombreValido(currentSign)
                ) && frameCount > 0 ->
            "MOV. $frameCount/${SequenceBuffer.WINDOW_SIZE}"

        currentSign.isBlank() ||
                currentSign == "Esperando seña..." ->
            "BUSCANDO SEÑA..."

        confidence != null ->
            "${currentSign.uppercase()} · $confidence%"

        else -> currentSign.uppercase()
    }

    Scaffold(
        containerColor = ProfileColors.BgDark,

        topBar = {
            SenasTopBar(
                userInitial,
                notificationCount
            )
        },

        bottomBar = {
            SenasBottomBar(
                selectedTab = 0,
                onSelectTraducir = {},
                onSelectSenas = onNavigateToSenas,
                onSelectPerfil = onNavigateToPerfil
            )
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(
                top = 8.dp,
                bottom = 20.dp
            )
        ) {
            // Cámara y reconocimiento
            item(key = "camera") {
                CameraDetectionCard(
                    hasCameraPermission = hasCameraPermission,
                    screenActive = screenActive,
                    cameraError = cameraError ?: recognitionError,
                    landmarks = handLandmarks,
                    lensFacing = lensFacing,
                    detectedLabel = badgeText,

                    onRequestPermission = {
                        permissionLauncher.launch(
                            Manifest.permission.CAMERA
                        )
                    },

                    onSwitchCamera = {
                        viewModel.detenerReconocimiento()
                        cameraError = null

                        lensFacing = if (
                            lensFacing == CameraSelector.LENS_FACING_BACK
                        ) {
                            CameraSelector.LENS_FACING_FRONT
                        } else {
                            CameraSelector.LENS_FACING_BACK
                        }
                    },

                    onHandsDetected = { manos ->
                        if (
                            cameraError == null &&
                            lifecycleOwner.lifecycle.currentState
                                .isAtLeast(Lifecycle.State.STARTED)
                        ) {
                            viewModel.onHandDetected(
                                puntos = manos.flatten(),
                                onSignAccepted = { sena ->
                                    registrarSena(sena)
                                }
                            )
                        }
                    },

                    onCameraError = { error ->
                        cameraError = error

                        if (error != null) {
                            viewModel.detenerReconocimiento()
                        }
                    },

                    onCameraDisposed = {
                        viewModel.detenerReconocimiento()
                    }
                )
            }

            // Construcción de frases y modo de salida
            item(key = "translation_phrase") {
                TranslationPhraseCard(
                    words = phraseWords,
                    outputMode = outputMode,

                    onOutputModeChange = { mode ->
                        if (!mode.usesVoice) {
                            speechHelper?.stop()
                        }

                        preferencesRepository
                            .cambiarModoSalida(mode)
                    },

                    onSpeakClick = {
                        ejecutarSalida(phraseWords)
                    },

                    onDeleteLastClick = {
                        viewModel.borrarUltimaPalabra()
                    },

                    onClearClick = {
                        viewModel.limpiarFrase()
                    }
                )
            }

            // Historial persistente
            recentPhrasesSection(
                frases = frasesRecientes,
                maxPhrases = RecentPhraseRepository.MAX_PHRASES,

                onRepeat = { frase ->
                    viewModel.cargarFrase(frase.palabras)

                    ejecutarSalida(
                        palabras = frase.palabras,
                        desdeHistorial = true
                    )
                },

                onDelete = { id ->
                    recentPhraseRepository.eliminar(id)
                },

                onClearRequest = {
                    showClearDialog = true
                }
            )
        }
    }
}

// Compatibilidad con las demás pantallas
@Composable
fun SenasBottomBar(
    selectedTab: Int,
    onSelectTraducir: () -> Unit,
    onSelectSenas: () -> Unit,
    onSelectPerfil: () -> Unit
) {
    SharedSenasBottomBar(
        selectedTab = selectedTab,
        onSelectTraducir = onSelectTraducir,
        onSelectSenas = onSelectSenas,
        onSelectPerfil = onSelectPerfil
    )
}