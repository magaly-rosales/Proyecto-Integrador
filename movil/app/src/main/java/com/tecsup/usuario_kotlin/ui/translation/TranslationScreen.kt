package com.tecsup.usuario_kotlin.ui.translation

import android.Manifest
import android.content.pm.PackageManager
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.rounded.Cameraswitch
import androidx.compose.material.icons.rounded.PanTool
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tecsup.usuario_kotlin.camera.CameraPreview
import com.tecsup.usuario_kotlin.camera.HandSkeletonOverlay
import com.tecsup.usuario_kotlin.ui.translation.components.TranslationPhraseCard
import com.tecsup.usuario_kotlin.utils.RecognitionUtils
import com.tecsup.usuario_kotlin.utils.SequenceBuffer
import com.tecsup.usuario_kotlin.viewmodel.TranslationViewModel
import java.util.Locale

// Colores originales del traductor
private val BgDark = Color(0xFF08141A)
private val CardDark = Color(0xFF0F2129)
private val CardElevated = Color(0xFF132A34)
private val AccentCyan = Color(0xFF22B8CF)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF8A9BA8)
private val BadgeYellow = Color(0xFFF5B82E)

data class FraseReciente(
    val texto: String,
    val tiempo: String,
    val palabras: List<String> = listOf(texto)
)

@Composable
fun TranslationScreen(
    viewModel: TranslationViewModel,
    userInitial: String = "M",
    notificationCount: Int = 3,
    onNavigateToSenas: () -> Unit = {},
    onNavigateToPerfil: () -> Unit = {},
    onSpeakText: (String) -> Unit = {}
) {
    // Estados del reconocimiento
    val currentSign by viewModel.translatedSign.collectAsStateWithLifecycle()
    val confidence by viewModel.confidence.collectAsStateWithLifecycle()
    val handLandmarks by viewModel.handLandmarks.collectAsStateWithLifecycle()
    val frameCount by viewModel.sequenceFrameCount.collectAsStateWithLifecycle()
    val recognizingSequence by viewModel.isRecognizingSequence.collectAsStateWithLifecycle()
    val dynamicRecognition by viewModel.isDynamicRecognition.collectAsStateWithLifecycle()
    val recognitionError by viewModel.errorMessage.collectAsStateWithLifecycle()

    // Palabras acumuladas de la frase
    val phraseWords by viewModel.phraseWords.collectAsStateWithLifecycle()

    // Reutilizar el callback de registro de MainActivity
    val registrarSena by rememberUpdatedState(onSpeakText)

    val handDetected = handLandmarks.isNotEmpty()

    val handStatus = when (handLandmarks.size) {
        0 -> "Buscando mano..."
        21 -> "1 mano detectada (21 puntos)"
        42 -> "2 manos detectadas (42 puntos)"
        else -> "Detectados ${handLandmarks.size} puntos"
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Estados de cámara
    var cameraError by remember { mutableStateOf<String?>(null) }

    var lensFacing by remember {
        mutableIntStateOf(CameraSelector.LENS_FACING_BACK)
    }

    var screenActive by remember(lifecycleOwner) {
        mutableStateOf(
            lifecycleOwner.lifecycle.currentState
                .isAtLeast(Lifecycle.State.STARTED)
        )
    }

    // Permiso de cámara
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted

        if (!granted) {
            viewModel.detenerReconocimiento()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Motor de voz
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var ttsReady by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val languageResult = tts?.setLanguage(
                    Locale("es", "PE")
                )

                ttsReady = languageResult != null &&
                        languageResult >= TextToSpeech.LANG_AVAILABLE
            }
        }

        tts = textToSpeech

        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
            ttsReady = false
            tts = null
        }
    }

    // Control del ciclo de vida
    DisposableEffect(lifecycleOwner, viewModel, context) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    screenActive = true
                }

                Lifecycle.Event.ON_RESUME -> {
                    hasCameraPermission =
                        ContextCompat.checkSelfPermission(
                            context, Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED

                    if (!hasCameraPermission) {
                        viewModel.detenerReconocimiento()
                    }
                }

                Lifecycle.Event.ON_STOP -> {
                    screenActive = false
                    viewModel.detenerReconocimiento()
                    tts?.stop()
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

    // Frases recientes temporales
    val frasesRecientes = remember {
        mutableStateListOf<FraseReciente>()
    }

    // Reproducir una frase completa
    fun hablarFrase(palabras: List<String>) {
        val frase = palabras.joinToString(" ").trim()

        if (frase.isBlank()) return

        if (!ttsReady) {
            Toast.makeText(
                context,
                "El motor de voz no está disponible",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val resultado = tts?.speak(
            frase,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "frase_${System.currentTimeMillis()}"
        )

        if (resultado != TextToSpeech.SUCCESS) {
            Toast.makeText(
                context,
                "No se pudo reproducir la frase",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        // Guardar la frase sin duplicados
        frasesRecientes.removeAll {
            it.texto.equals(frase, ignoreCase = true)
        }

        frasesRecientes.add(
            0,
            FraseReciente(
                texto = frase,
                tiempo = "Ahora",
                palabras = palabras.toList()
            )
        )

        // Mantener el límite anterior de frases recientes
        if (frasesRecientes.size > 10) {
            frasesRecientes.removeAt(frasesRecientes.lastIndex)
        }
    }

    Scaffold(
        containerColor = BgDark,

        topBar = {
            SenasTopBar(userInitial, notificationCount)
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
            item {
                val badgeText = when {
                    !handDetected ->
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

                CameraDetectionCard(
                    hasCameraPermission = hasCameraPermission,
                    cameraError = cameraError ?: recognitionError,
                    handDetected = handDetected,
                    handStatus = handStatus,
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

                    cameraPreview = {
                        if (screenActive) {

                            DisposableEffect(viewModel, lensFacing) {
                                onDispose {
                                    viewModel.detenerReconocimiento()
                                }
                            }

                            Box(modifier = Modifier.fillMaxSize()) {
                                CameraPreview(
                                    modifier = Modifier.fillMaxSize(),
                                    lensFacing = lensFacing,

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

                                    onError = { error ->
                                        cameraError = error

                                        if (error != null) {
                                            viewModel.detenerReconocimiento()
                                        }
                                    }
                                )

                                HandSkeletonOverlay(
                                    landmarks = handLandmarks,
                                    isFrontCamera =
                                        lensFacing ==
                                                CameraSelector.LENS_FACING_FRONT,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                )
            }

            // Frase completa y acciones
            item {
                TranslationPhraseCard(
                    words = phraseWords,

                    onSpeakClick = {
                        hablarFrase(phraseWords)
                    },

                    onDeleteLastClick = {
                        viewModel.borrarUltimaPalabra()
                    },

                    onClearClick = {
                        viewModel.limpiarFrase()
                    }
                )
            }

            // Frases recientes
            item {
                Text(
                    text = "FRASES RECIENTES",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (frasesRecientes.isEmpty()) {
                item {
                    Text(
                        text = "Aún no hay traducciones recientes",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }

            items(
                items = frasesRecientes,
                key = { it.texto }
            ) { frase ->

                RecentPhraseItem(
                    frase = frase,

                    onRepeatClick = {
                        viewModel.cargarFrase(frase.palabras)
                        hablarFrase(frase.palabras)
                    }
                )
            }
        }
    }
}

// BARRA SUPERIOR

@Composable
private fun SenasTopBar(
    userInitial: String,
    notificationCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(CardElevated)
                    .border(1.5.dp, AccentCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PanTool,
                    contentDescription = "Logo SeñasApp",
                    tint = AccentCyan,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(10.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("Señas")
                    }

                    withStyle(
                        SpanStyle(
                            color = AccentCyan,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("App")
                    }
                },
                fontSize = 21.sp
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.padding(end = 12.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(26.dp)
                )

                if (notificationCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(BadgeYellow),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = notificationCount.toString(),
                            color = BgDark,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AccentCyan),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userInitial,
                    color = BgDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

// TARJETA DE CÁMARA

@Composable
private fun CameraDetectionCard(
    hasCameraPermission: Boolean,
    cameraError: String?,
    handDetected: Boolean,
    handStatus: String,
    onRequestPermission: () -> Unit,
    onSwitchCamera: () -> Unit,
    detectedLabel: String,
    cameraPreview: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(275.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(CardDark)
            .border(
                1.5.dp,
                AccentCyan.copy(alpha = 0.7f),
                RoundedCornerShape(20.dp)
            )
    ) {
        if (hasCameraPermission) {
            cameraPreview()

            cameraError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(12.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentCyan,
                        contentColor = BgDark
                    )
                ) {
                    Text(
                        text = "Permitir acceso a la cámara",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .clip(RoundedCornerShape(50))
                    .background(CardElevated.copy(alpha = 0.85f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (handDetected) AccentCyan else TextSecondary
                        )
                )

                Spacer(Modifier.width(6.dp))

                Text(
                    text = handStatus,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            IconButton(
                onClick = onSwitchCamera,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CardElevated.copy(alpha = 0.85f))
            ) {
                Icon(
                    imageVector = Icons.Rounded.Cameraswitch,
                    contentDescription = "Cambiar cámara frontal/trasera",
                    tint = AccentCyan,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Marco original de detección
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 10.dp)
                    .width(175.dp)
                    .height(185.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRoundRect(
                        color = AccentCyan.copy(alpha = 0.75f),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(
                                floatArrayOf(12f, 10f), 0f
                            )
                        ),
                        cornerRadius = CornerRadius(
                            14.dp.toPx(), 14.dp.toPx()
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-11).dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(AccentCyan)
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = detectedLabel,
                        color = BgDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// FRASES RECIENTES

@Composable
private fun RecentPhraseItem(
    frase: FraseReciente,
    onRepeatClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = CardDark,
        border = BorderStroke(1.dp, CardElevated)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = frase.texto,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = frase.tiempo,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            IconButton(onClick = onRepeatClick) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = "Repetir frase",
                    tint = AccentCyan
                )
            }
        }
    }
}

// BARRA INFERIOR COMPARTIDA

@Composable
fun SenasBottomBar(
    selectedTab: Int,
    onSelectTraducir: () -> Unit,
    onSelectSenas: () -> Unit,
    onSelectPerfil: () -> Unit
) {
    val navColors = NavigationBarItemDefaults.colors(
        selectedIconColor = AccentCyan,
        selectedTextColor = AccentCyan,
        unselectedIconColor = TextSecondary,
        unselectedTextColor = TextSecondary,
        indicatorColor = Color.Transparent
    )

    NavigationBar(
        containerColor = BgDark,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = onSelectTraducir,
            icon = {
                Icon(
                    Icons.Outlined.Videocam,
                    contentDescription = "Traducir"
                )
            },
            label = { Text("Traducir", fontSize = 12.sp) },
            colors = navColors
        )

        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = onSelectSenas,
            icon = {
                Icon(
                    Icons.AutoMirrored.Outlined.MenuBook,
                    contentDescription = "Señas"
                )
            },
            label = { Text("Señas", fontSize = 12.sp) },
            colors = navColors
        )

        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = onSelectPerfil,
            icon = {
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = "Perfil"
                )
            },
            label = { Text("Perfil", fontSize = 12.sp) },
            colors = navColors
        )
    }
}