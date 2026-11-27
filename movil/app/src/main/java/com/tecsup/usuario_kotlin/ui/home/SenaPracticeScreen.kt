package com.tecsup.usuario_kotlin.ui.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cameraswitch
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PanTool
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tecsup.usuario_kotlin.camera.CameraPreview
import com.tecsup.usuario_kotlin.camera.HandSkeletonOverlay
import com.tecsup.usuario_kotlin.data.model.SenaCatalogoDto
import com.tecsup.usuario_kotlin.ui.components.PrimaryButton
import com.tecsup.usuario_kotlin.ui.components.ScreenHeader
import com.tecsup.usuario_kotlin.ui.components.SecondaryButton
import com.tecsup.usuario_kotlin.ui.home.components.SenaCatalogImage
import com.tecsup.usuario_kotlin.ui.theme.ProfileColors
import com.tecsup.usuario_kotlin.viewmodel.PracticeStatus
import com.tecsup.usuario_kotlin.viewmodel.PracticeUiState
import com.tecsup.usuario_kotlin.viewmodel.PracticeViewModel

private val PracticeSuccess = Color(0xFF4ADE80)

@Composable
fun SenaPracticeScreen(
    sena: SenaCatalogoDto,
    apiUrl: String?,
    onBackClick: () -> Unit,
    practiceViewModel: PracticeViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Estado observable de la práctica
    val state by practiceViewModel.uiState
        .collectAsStateWithLifecycle()

    // Cámara seleccionada
    var lensFacing by rememberSaveable {
        mutableIntStateOf(
            CameraSelector.LENS_FACING_FRONT
        )
    }

    // Permiso de cámara
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.RequestPermission()
        ) { granted ->

            hasCameraPermission = granted
        }

    // Iniciar y limpiar la sesión de práctica
    DisposableEffect(
        sena.id,
        sena.nombre,
        lifecycleOwner,
        practiceViewModel,
        context
    ) {
        practiceViewModel.iniciarPractica(
            sena.nombre
        )

        val observer =
            LifecycleEventObserver { _, event ->

                when (event) {

                    Lifecycle.Event.ON_RESUME -> {

                        hasCameraPermission =
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED

                        if (!hasCameraPermission) {
                            practiceViewModel.limpiarManos()
                        }
                    }

                    Lifecycle.Event.ON_STOP -> {

                        // Detener una práctica activa
                        // al salir de la aplicación
                        if (
                            practiceViewModel.uiState
                                .value.isActive
                        ) {
                            practiceViewModel.detenerPractica()
                        }
                    }

                    else -> Unit
                }
            }

        lifecycleOwner.lifecycle.addObserver(
            observer
        )

        onDispose {

            lifecycleOwner.lifecycle.removeObserver(
                observer
            )

            practiceViewModel.limpiarPractica()
        }
    }

    val regresar: () -> Unit = {

        practiceViewModel.limpiarPractica()

        onBackClick()
    }

    // Retroceso mediante el botón de Android
    BackHandler(
        onBack = regresar
    )

    Scaffold(
        containerColor = ProfileColors.BgDark
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(16.dp)
        ) {
            // Cabecera
            ScreenHeader(
                title = "Practicar seña",
                onBackClick = regresar
            )

            // Seña objetivo
            PracticeTargetCard(
                sena = sena,
                apiUrl = apiUrl
            )

            Text(
                text = "Coloca tus manos frente a la cámara " +
                        "e intenta realizar la seña indicada.",
                color = ProfileColors.TextSecondary,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )

            // Cámara y detección de manos
            PracticeCameraCard(
                state = state,
                hasCameraPermission = hasCameraPermission,
                lensFacing = lensFacing,

                onRequestPermission = {

                    permissionLauncher.launch(
                        Manifest.permission.CAMERA
                    )
                },

                onSwitchCamera = {

                    practiceViewModel.limpiarManos()

                    lensFacing = if (
                        lensFacing ==
                        CameraSelector.LENS_FACING_FRONT
                    ) {
                        CameraSelector.LENS_FACING_BACK
                    } else {
                        CameraSelector.LENS_FACING_FRONT
                    }
                },

                onHandsDetected = { puntos ->

                    practiceViewModel.onHandsDetected(
                        puntos
                    )
                },

                onCameraError = { mensaje ->

                    if (!mensaje.isNullOrBlank()) {

                        practiceViewModel.mostrarErrorCamara(
                            mensaje
                        )
                    }
                }
            )

            // Resultado de la evaluación
            PracticeResultCard(
                state = state,
                hasCameraPermission = hasCameraPermission
            )

            // Acciones de práctica
            if (state.isActive) {

                SecondaryButton(
                    text = "Detener práctica",
                    onClick = {

                        practiceViewModel.detenerPractica()
                    }
                )

            } else {

                val buttonText = when (state.status) {

                    PracticeStatus.SUCCESS ->
                        "Practicar nuevamente"

                    PracticeStatus.ERROR ->
                        "Reintentar práctica"

                    else ->
                        "Iniciar práctica"
                }

                PrimaryButton(
                    text = buttonText,
                    onClick = {

                        practiceViewModel.iniciarPractica(
                            sena.nombre
                        )
                    }
                )
            }

            SecondaryButton(
                text = "Volver a los detalles",
                onClick = regresar
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )
        }
    }
}

// Tarjeta de la seña objetivo
@Composable
private fun PracticeTargetCard(
    sena: SenaCatalogoDto,
    apiUrl: String?
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ProfileColors.CardDark,
        border = BorderStroke(
            1.dp,
            ProfileColors.CardElevated
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(14.dp)
        ) {

            SenaCatalogImage(
                imagenSena = sena.imagen,
                imagenCategoria = sena.imagenCategoria,
                apiUrl = apiUrl,
                modifier = Modifier.size(72.dp),
                iconSize = 34.dp,
                cornerRadius = 14.dp,
                contentDescription =
                    "Referencia visual del catálogo"
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = "SEÑA OBJETIVO",
                    color = ProfileColors.TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = sena.nombre,
                    color = ProfileColors.TextPrimary,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold
                )

                if (!sena.categoria.isNullOrBlank()) {

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text = sena.categoria,
                        color = ProfileColors.AccentCyan,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// Tarjeta de cámara y MediaPipe
@Composable
private fun PracticeCameraCard(
    state: PracticeUiState,
    hasCameraPermission: Boolean,
    lensFacing: Int,
    onRequestPermission: () -> Unit,
    onSwitchCamera: () -> Unit,
    onHandsDetected: (
        List<com.tecsup.usuario_kotlin.data.model.PuntoManoDto>
    ) -> Unit,
    onCameraError: (String?) -> Unit
) {
    val cameraShape = RoundedCornerShape(20.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(275.dp)
            .clip(cameraShape)
            .background(ProfileColors.CardDark)
            .border(
                1.5.dp,
                ProfileColors.AccentCyan.copy(
                    alpha = 0.7f
                ),
                cameraShape
            ),
        contentAlignment = Alignment.Center
    ) {

        when {

            // Cámara desactivada
            !state.isActive -> {

                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment =
                        Alignment.CenterHorizontally,
                    verticalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {

                    Icon(
                        imageVector = if (
                            state.practiceCompleted
                        ) {
                            Icons.Rounded.CheckCircle
                        } else {
                            Icons.Rounded.PanTool
                        },
                        contentDescription = null,
                        tint = if (
                            state.practiceCompleted
                        ) {
                            PracticeSuccess
                        } else {
                            ProfileColors.AccentCyan
                        },
                        modifier = Modifier.size(52.dp)
                    )

                    Text(
                        text = if (
                            state.practiceCompleted
                        ) {
                            "Práctica completada"
                        } else {
                            "Cámara desactivada"
                        },
                        color = ProfileColors.TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Solicitar permiso de cámara
            !hasCameraPermission -> {

                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment =
                        Alignment.CenterHorizontally,
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {

                    Icon(
                        imageVector = Icons.Rounded.PanTool,
                        contentDescription = null,
                        tint = ProfileColors.AccentCyan,
                        modifier = Modifier.size(42.dp)
                    )

                    Text(
                        text = "Necesitamos acceso a la cámara",
                        color = ProfileColors.TextPrimary,
                        fontSize = 14.sp
                    )

                    PrimaryButton(
                        text = "Permitir acceso",
                        onClick = onRequestPermission
                    )
                }
            }

            // Cámara activa
            else -> {

                key(lensFacing) {

                    CameraPreview(
                        modifier = Modifier.fillMaxSize(),
                        lensFacing = lensFacing,

                        onHandsDetected = { manos ->

                            onHandsDetected(
                                manos.flatten()
                            )
                        },

                        onError = onCameraError
                    )
                }

                // Dibujar cada mano por separado
                state.handLandmarks
                    .chunked(21)
                    .filter { it.size == 21 }
                    .forEach { mano ->

                        HandSkeletonOverlay(
                            landmarks = mano,
                            isFrontCamera =
                                lensFacing ==
                                        CameraSelector.LENS_FACING_FRONT,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                // Estado de detección
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            ProfileColors.BgDark.copy(
                                alpha = 0.85f
                            )
                        )
                        .padding(
                            horizontal = 10.dp,
                            vertical = 8.dp
                        ),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (state.handDetected) {
                                    PracticeSuccess
                                } else {
                                    ProfileColors.BadgeYellow
                                }
                            )
                    )

                    Spacer(
                        modifier = Modifier.width(6.dp)
                    )

                    Text(
                        text = when (state.handCount) {

                            1 -> "1 mano detectada"

                            2 -> "2 manos detectadas"

                            else -> "Buscando manos..."
                        },
                        color = ProfileColors.TextPrimary,
                        fontSize = 11.sp
                    )
                }

                // Cambiar cámara frontal o trasera
                IconButton(
                    onClick = onSwitchCamera,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            ProfileColors.BgDark.copy(
                                alpha = 0.8f
                            )
                        )
                ) {

                    Icon(
                        imageVector =
                            Icons.Rounded.Cameraswitch,
                        contentDescription =
                            "Cambiar cámara",
                        tint = ProfileColors.TextPrimary
                    )
                }
            }
        }
    }
}

// Resultado de la práctica
@Composable
private fun PracticeResultCard(
    state: PracticeUiState,
    hasCameraPermission: Boolean
) {
    val resultColor = when (state.status) {

        PracticeStatus.SUCCESS ->
            PracticeSuccess

        PracticeStatus.ERROR ->
            ProfileColors.ErrorColor

        PracticeStatus.NOT_RECOGNIZED,
        PracticeStatus.DIFFERENT_SIGN ->
            ProfileColors.BadgeYellow

        else ->
            ProfileColors.AccentCyan
    }

    val title = when (state.status) {

        PracticeStatus.IDLE ->
            "Práctica detenida"

        PracticeStatus.WAITING_HAND ->

            if (hasCameraPermission) {
                "Esperando tus manos..."
            } else {
                "Permiso de cámara necesario"
            }

        PracticeStatus.ANALYZING ->
            "Analizando seña..."

        PracticeStatus.NOT_RECOGNIZED ->
            "Seña no reconocida"

        PracticeStatus.DIFFERENT_SIGN ->
            "Inténtalo nuevamente"

        PracticeStatus.CONFIRMING ->
            "¡Vamos bien!"

        PracticeStatus.SUCCESS ->
            "¡Seña reconocida!"

        PracticeStatus.ERROR ->
            "No se pudo completar la práctica"
    }

    val message = when (state.status) {

        PracticeStatus.IDLE ->
            "Puedes iniciar otro intento cuando desees."

        PracticeStatus.WAITING_HAND ->

            if (hasCameraPermission) {
                "Coloca tus manos dentro de la cámara."
            } else {
                "Autoriza el acceso para iniciar la detección."
            }

        PracticeStatus.ANALYZING ->
            "Estamos consultando el reconocimiento."

        PracticeStatus.NOT_RECOGNIZED ->
            "Observa la referencia e intenta realizar la seña otra vez."

        PracticeStatus.DIFFERENT_SIGN ->
            "La seña detectada no coincide con el objetivo."

        PracticeStatus.CONFIRMING ->
            "Mantén la seña para confirmar el reconocimiento."

        PracticeStatus.SUCCESS ->
            "El reconocimiento coincide con la seña objetivo."

        PracticeStatus.ERROR ->
            state.errorMessage ?: "Inténtalo nuevamente."
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ProfileColors.CardDark,
        border = BorderStroke(
            1.dp,
            resultColor.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            Text(
                text = "RESULTADO DE LA PRÁCTICA",
                color = ProfileColors.TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                if (state.isAnalyzing) {

                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = resultColor,
                        strokeWidth = 2.dp
                    )

                    Spacer(
                        modifier = Modifier.width(10.dp)
                    )

                } else if (state.practiceCompleted) {

                    Icon(
                        imageVector =
                            Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = resultColor,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(
                        modifier = Modifier.width(10.dp)
                    )
                }

                Text(
                    text = title,
                    color = resultColor,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = message,
                color = ProfileColors.TextSecondary,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )

            state.detectedSign?.let { detectada ->

                HorizontalDivider(
                    color = ProfileColors.CardElevated
                )

                Text(
                    text = "Seña detectada: $detectada",
                    color = ProfileColors.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (
                state.status == PracticeStatus.CONFIRMING ||
                state.status == PracticeStatus.SUCCESS
            ) {

                val required =
                    PracticeViewModel.REQUIRED_CONFIRMATIONS

                LinearProgressIndicator(
                    progress = {
                        (
                                state.confirmations.toFloat() /
                                        required
                                ).coerceIn(0f, 1f)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50)),
                    color = resultColor,
                    trackColor = ProfileColors.CardElevated
                )

                Text(
                    text = "${state.confirmations} de " +
                            "$required confirmaciones",
                    color = ProfileColors.TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}