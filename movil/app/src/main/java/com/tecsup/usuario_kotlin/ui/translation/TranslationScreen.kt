package com.tecsup.usuario_kotlin.ui.translation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Videocam
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
import com.tecsup.usuario_kotlin.camera.CameraPreview
import com.tecsup.usuario_kotlin.viewmodel.TranslationViewModel

private val BgDark = Color(0xFF08141A)
private val CardDark = Color(0xFF0F2129)
private val CardElevated = Color(0xFF132A34)
private val AccentCyan = Color(0xFF22B8CF)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF8A9BA8)
private val BadgeYellow = Color(0xFFF5B82E)

data class FraseReciente(
    val texto: String,
    val tiempo: String
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
    val currentSign by viewModel.translatedSign.collectAsState()
    val context = LocalContext.current

    var cameraError by remember {
        mutableStateOf<String?>(null)
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val frasesRecientes = remember {
        mutableStateListOf(
            FraseReciente("Buenos días, ¿cómo estás?", "Hace 5 min"),
            FraseReciente("Necesito ir al baño", "Hace 20 min"),
            FraseReciente("Gracias por tu ayuda", "Ayer")
        )
    }

    Scaffold(
        containerColor = BgDark,
        topBar = {
            SenasTopBar(
                userInitial = userInitial,
                notificationCount = notificationCount
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
            contentPadding = PaddingValues(top = 8.dp, bottom = 20.dp)
        ) {
            item {
                CameraDetectionCard(
                    hasCameraPermission = hasCameraPermission,
                    cameraError = cameraError,
                    onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    detectedLabel = if (currentSign.isBlank() || currentSign == "Esperando seña...") {
                        "AYUDA · 92%"
                    } else {
                        currentSign.uppercase()
                    },
                    cameraPreview = {
                        CameraPreview(
                            modifier = Modifier.fillMaxSize(),
                            onError = { error ->
                                cameraError = error
                            }
                        )
                    }
                )
            }

            item {
                val displaySign = if (currentSign == "Esperando seña...") "Necesito ayuda" else currentSign
                TranslationResultCard(
                    translatedText = displaySign,
                    onSpeakClick = {
                        onSpeakText(displaySign)
                    },
                    onClearClick = {
                        viewModel.updateTranslation("")
                    }
                )
            }

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

            items(frasesRecientes) { item ->
                RecentPhraseItem(
                    frase = item,
                    onRepeatClick = {
                        viewModel.updateTranslation(item.texto)
                        onSpeakText(item.texto)
                    }
                )
            }
        }
    }
}

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
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = TextPrimary, fontWeight = FontWeight.Bold)) {
                        append("Señas")
                    }
                    withStyle(SpanStyle(color = AccentCyan, fontWeight = FontWeight.Bold)) {
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
                    contentDescription = "Notificaciones",
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

@Composable
private fun CameraDetectionCard(
    hasCameraPermission: Boolean,
    cameraError: String?,
    onRequestPermission: () -> Unit,
    detectedLabel: String,
    cameraPreview: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(275.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(CardDark)
            .border(1.5.dp, AccentCyan.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
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
                    Text("Permitir acceso a la cámara", fontWeight = FontWeight.Bold)
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
                        .background(AccentCyan)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Mano detectada",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CardElevated.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Fullscreen,
                    contentDescription = "Expandir cámara",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

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
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                        ),
                        cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
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

@Composable
private fun TranslationResultCard(
    translatedText: String,
    onSpeakClick: () -> Unit,
    onClearClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = CardDark,
        border = BorderStroke(1.dp, CardElevated)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "TRADUCCIÓN",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = translatedText.ifBlank { "Esperando seña..." },
                    color = TextPrimary,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onSpeakClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentCyan,
                        contentColor = BgDark
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Decir en voz alta",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                OutlinedButton(
                    onClick = onClearClick,
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp),
                    border = BorderStroke(1.dp, CardElevated),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = CardElevated.copy(alpha = 0.5f),
                        contentColor = AccentCyan
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Backspace,
                        contentDescription = "Borrar texto",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

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
                Spacer(modifier = Modifier.height(2.dp))
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

@Composable
fun SenasBottomBar(
    selectedTab: Int,
    onSelectTraducir: () -> Unit,
    onSelectSenas: () -> Unit,
    onSelectPerfil: () -> Unit
) {
    NavigationBar(
        containerColor = BgDark,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = onSelectTraducir,
            icon = { Icon(Icons.Outlined.Videocam, contentDescription = "Traducir") },
            label = { Text("Traducir", fontSize = 12.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentCyan,
                selectedTextColor = AccentCyan,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = onSelectSenas,
            icon = { Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = "Señas") },
            label = { Text("Señas", fontSize = 12.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentCyan,
                selectedTextColor = AccentCyan,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = onSelectPerfil,
            icon = { Icon(Icons.Outlined.Person, contentDescription = "Perfil") },
            label = { Text("Perfil", fontSize = 12.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentCyan,
                selectedTextColor = AccentCyan,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary,
                indicatorColor = Color.Transparent
            )
        )
    }
}