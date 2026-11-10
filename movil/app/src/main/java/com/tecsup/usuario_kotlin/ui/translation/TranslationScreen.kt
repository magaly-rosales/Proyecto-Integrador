package com.tecsup.usuario_kotlin.ui.translation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.usuario_kotlin.ui.auth.LogoIcon
import com.tecsup.usuario_kotlin.ui.auth.SkeletonHandGraphic
import com.tecsup.usuario_kotlin.viewmodel.TranslationViewModel

// Color Palette
private val ScreenBg = Color(0xFF06141B)
private val CardBg = Color(0xFF091A23)
private val PrimaryCyan = Color(0xFF14A3A7)
private val MutedTextGray = Color(0xFF7E96A4)
private val BadgeBg = Color(0xFF0E232E)
private val NotificationBadgeYellow = Color(0xFFFBBF24)
private val BottomNavBg = Color(0xFF040D12)
private val NavUnselected = Color(0xFF586B76)

@Composable
fun TranslationScreen(
    viewModel: TranslationViewModel = remember { TranslationViewModel() },
    onProfileClick: () -> Unit = {}
) {
    val recentPhrases by viewModel.recentPhrases.collectAsState()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedIndex = viewModel.selectedNavTab,
                onTabSelected = { index ->
                    viewModel.selectTab(index)
                    if (index == 2) {
                        onProfileClick()
                    }
                }
            )
        },
        containerColor = ScreenBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // --- TOP BAR ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App Logo & Name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LogoIcon(modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)) {
                                append("Señas")
                            }
                            withStyle(style = SpanStyle(color = PrimaryCyan, fontWeight = FontWeight.Bold, fontSize = 20.sp)) {
                                append("App")
                            }
                        }
                    )
                }

                // Bell Badge & Profile Avatar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Notification Bell Icon with Badge 3
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { /* Notification click */ },
                        contentAlignment = Alignment.Center
                    ) {
                        BellIcon(
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        // Badge count "3"
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .align(Alignment.TopEnd)
                                .background(NotificationBadgeYellow, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "3",
                                style = TextStyle(
                                    color = ScreenBg,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // User Profile Avatar ("M")
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(PrimaryCyan, shape = CircleShape)
                            .clickable { onProfileClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "M",
                            style = TextStyle(
                                color = ScreenBg,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- CAMERA DETECTOR AREA CARD ---
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .border(width = 1.5.dp, color = PrimaryCyan, shape = RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                color = ScreenBg
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Top-Left Badge: "Mano detectada"
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .align(Alignment.TopStart)
                            .background(BadgeBg, shape = RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(PrimaryCyan, shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mano detectada",
                            style = TextStyle(
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    // Top-Right Frame Icon
                    Box(
                        modifier = Modifier
                            .padding(14.dp)
                            .align(Alignment.TopEnd)
                            .size(36.dp)
                            .background(BadgeBg, shape = RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        FrameExpandIcon(
                            tint = PrimaryCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Center Dashed Bounding Box & Hand Graphic
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(width = 160.dp, height = 180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Dashed rectangle border
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRoundRect(
                                color = PrimaryCyan,
                                cornerRadius = CornerRadius(14.dp.toPx()),
                                style = Stroke(
                                    width = 1.5.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                                )
                            )
                        }

                        // Badge at top center of dashed box: "AYUDA · 92%"
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = (-12).dp)
                                .background(PrimaryCyan, shape = RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "AYUDA · ${(viewModel.confidence * 100).toInt()}%",
                                style = TextStyle(
                                    color = ScreenBg,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        // Hand Landmark Graphic inside box
                        SkeletonHandGraphic(
                            modifier = Modifier
                                .size(width = 110.dp, height = 120.dp)
                                .offset(y = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- TRADUCCIÓN CARD SECTION ---
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = CardBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "TRADUCCIÓN",
                        style = TextStyle(
                            color = MutedTextGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SpeakerIcon(
                            tint = PrimaryCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = viewModel.currentPhrase.ifBlank { "Esperando seña..." },
                            style = TextStyle(
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // "Decir en voz alta" Primary Cyan Button
                        Button(
                            onClick = { viewModel.speakPhrase() },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryCyan,
                                contentColor = ScreenBg
                            )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                SpeakerIcon(
                                    tint = ScreenBg,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Decir en voz alta",
                                    style = TextStyle(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ScreenBg
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Clear Button [X]
                        IconButton(
                            onClick = { viewModel.clearPhrase() },
                            modifier = Modifier
                                .size(48.dp)
                                .background(ScreenBg, shape = RoundedCornerShape(14.dp))
                                .border(width = 1.dp, color = Color(0xFF16303C), shape = RoundedCornerShape(14.dp))
                        ) {
                            ClearIcon(
                                tint = PrimaryCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- FRASES RECIENTES SECTION ---
            Text(
                text = "FRASES RECIENTES",
                style = TextStyle(
                    color = MutedTextGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                recentPhrases.forEach { history ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.replayPhrase(history.phrase) },
                        shape = RoundedCornerShape(14.dp),
                        color = CardBg
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = history.phrase,
                                    style = TextStyle(
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = history.timestamp,
                                    style = TextStyle(
                                        color = MutedTextGray,
                                        fontSize = 12.sp
                                    )
                                )
                            }

                            IconButton(
                                onClick = { viewModel.replayPhrase(history.phrase) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                ReplayIcon(
                                    tint = PrimaryCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- HELP ACCORDION CARD ---
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* Expand help accordion */ },
                shape = RoundedCornerShape(14.dp),
                color = CardBg
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        QuestionMarkIcon(
                            tint = PrimaryCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)) {
                                    append("Cómo usar SeñasApp")
                                }
                                withStyle(style = SpanStyle(color = MutedTextGray, fontSize = 14.sp)) {
                                    append(" · 4 pasos")
                                }
                            }
                        )
                    }

                    ChevronDownIcon(
                        tint = MutedTextGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// --- BOTTOM NAVIGATION BAR ---
@Composable
private fun BottomNavigationBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = BottomNavBg,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 0: Traducir
            NavTabItem(
                title = "Traducir",
                isSelected = selectedIndex == 0,
                onClick = { onTabSelected(0) },
                icon = { tint -> CameraNavIcon(tint = tint, modifier = Modifier.size(22.dp)) }
            )

            // Tab 1: Señas
            NavTabItem(
                title = "Señas",
                isSelected = selectedIndex == 1,
                onClick = { onTabSelected(1) },
                icon = { tint -> BookNavIcon(tint = tint, modifier = Modifier.size(22.dp)) }
            )

            // Tab 2: Perfil
            NavTabItem(
                title = "Perfil",
                isSelected = selectedIndex == 2,
                onClick = { onTabSelected(2) },
                icon = { tint -> ProfileNavIcon(tint = tint, modifier = Modifier.size(22.dp)) }
            )
        }
    }
}

@Composable
private fun NavTabItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: @Composable (Color) -> Unit
) {
    val tint = if (isSelected) PrimaryCyan else NavUnselected
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        icon(tint)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = TextStyle(
                color = tint,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        )
    }
}

// --- CUSTOM CANVAS ICONS ---

@Composable
private fun BellIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.8f.dp.toPx()

        val bellPath = Path().apply {
            moveTo(w * 0.3f, h * 0.75f)
            lineTo(w * 0.7f, h * 0.75f)
            cubicTo(w * 0.8f, h * 0.75f, w * 0.85f, h * 0.65f, w * 0.8f, h * 0.5f)
            lineTo(w * 0.75f, h * 0.35f)
            cubicTo(w * 0.75f, h * 0.2f, w * 0.63f, h * 0.1f, w * 0.5f, h * 0.1f)
            cubicTo(w * 0.37f, h * 0.1f, w * 0.25f, h * 0.2f, w * 0.25f, h * 0.35f)
            lineTo(w * 0.2f, h * 0.5f)
            cubicTo(w * 0.15f, h * 0.65f, w * 0.2f, h * 0.75f, w * 0.3f, h * 0.75f)
            close()
        }
        drawPath(path = bellPath, color = tint, style = Stroke(width = stroke))
        drawArc(
            color = tint,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.42f, h * 0.78f),
            size = Size(w * 0.16f, h * 0.14f),
            style = Stroke(width = stroke)
        )
    }
}

@Composable
private fun FrameExpandIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 2.dp.toPx()
        val len = w * 0.3f

        // Top-Left
        drawLine(tint, Offset(0f, 0f), Offset(len, 0f), stroke, StrokeCap.Round)
        drawLine(tint, Offset(0f, 0f), Offset(0f, len), stroke, StrokeCap.Round)
        // Top-Right
        drawLine(tint, Offset(w, 0f), Offset(w - len, 0f), stroke, StrokeCap.Round)
        drawLine(tint, Offset(w, 0f), Offset(w, len), stroke, StrokeCap.Round)
        // Bottom-Left
        drawLine(tint, Offset(0f, h), Offset(len, h), stroke, StrokeCap.Round)
        drawLine(tint, Offset(0f, h), Offset(0f, h - len), stroke, StrokeCap.Round)
        // Bottom-Right
        drawLine(tint, Offset(w, h), Offset(w - len, h), stroke, StrokeCap.Round)
        drawLine(tint, Offset(w, h), Offset(w, h - len), stroke, StrokeCap.Round)
    }
}

@Composable
private fun SpeakerIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.8f.dp.toPx()

        val speakerPath = Path().apply {
            moveTo(w * 0.15f, h * 0.35f)
            lineTo(w * 0.35f, h * 0.35f)
            lineTo(w * 0.6f, h * 0.15f)
            lineTo(w * 0.6f, h * 0.85f)
            lineTo(w * 0.35f, h * 0.65f)
            lineTo(w * 0.15f, h * 0.65f)
            close()
        }
        drawPath(speakerPath, color = tint, style = Stroke(width = stroke))
        // Sound waves
        drawArc(
            color = tint,
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(w * 0.5f, h * 0.25f),
            size = Size(w * 0.35f, h * 0.5f),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun ClearIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.8f.dp.toPx()

        // Backspace tag shape with X inside
        val tagPath = Path().apply {
            moveTo(w * 0.35f, h * 0.2f)
            lineTo(w * 0.85f, h * 0.2f)
            cubicTo(w * 0.92f, h * 0.2f, w * 0.95f, h * 0.25f, w * 0.95f, h * 0.3f)
            lineTo(w * 0.95f, h * 0.7f)
            cubicTo(w * 0.95f, h * 0.75f, w * 0.92f, h * 0.8f, w * 0.85f, h * 0.8f)
            lineTo(w * 0.35f, h * 0.8f)
            lineTo(w * 0.08f, h * 0.5f)
            close()
        }
        drawPath(tagPath, color = tint, style = Stroke(width = stroke))

        // X lines inside
        drawLine(tint, Offset(w * 0.45f, h * 0.35f), Offset(w * 0.75f, h * 0.65f), stroke, StrokeCap.Round)
        drawLine(tint, Offset(w * 0.75f, h * 0.35f), Offset(w * 0.45f, h * 0.65f), stroke, StrokeCap.Round)
    }
}

@Composable
private fun ReplayIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.8f.dp.toPx()

        // Counter-clockwise arrow circle
        drawArc(
            color = tint,
            startAngle = 40f,
            sweepAngle = 290f,
            useCenter = false,
            topLeft = Offset(w * 0.15f, h * 0.15f),
            size = Size(w * 0.7f, h * 0.7f),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        // Arrow head
        val arrowPath = Path().apply {
            moveTo(w * 0.62f, h * 0.08f)
            lineTo(w * 0.75f, h * 0.25f)
            lineTo(w * 0.52f, h * 0.28f)
        }
        drawPath(arrowPath, color = tint, style = Stroke(width = stroke, cap = StrokeCap.Round))
    }
}

@Composable
private fun QuestionMarkIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.8f.dp.toPx()

        drawCircle(color = tint, radius = w / 2f - stroke, style = Stroke(width = stroke))

        // Question mark curve
        val qPath = Path().apply {
            moveTo(w * 0.38f, h * 0.35f)
            cubicTo(w * 0.38f, h * 0.24f, w * 0.62f, h * 0.24f, w * 0.62f, h * 0.38f)
            cubicTo(w * 0.62f, h * 0.5f, w * 0.5f, h * 0.52f, w * 0.5f, h * 0.62f)
        }
        drawPath(qPath, color = tint, style = Stroke(width = stroke, cap = StrokeCap.Round))
        drawCircle(color = tint, radius = stroke * 0.9f, center = Offset(w * 0.5f, h * 0.75f))
    }
}

@Composable
private fun ChevronDownIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 2.dp.toPx()

        drawLine(tint, Offset(w * 0.15f, h * 0.35f), Offset(w * 0.5f, h * 0.7f), stroke, StrokeCap.Round)
        drawLine(tint, Offset(w * 0.5f, h * 0.7f), Offset(w * 0.85f, h * 0.35f), stroke, StrokeCap.Round)
    }
}

@Composable
private fun CameraNavIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.8f.dp.toPx()

        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.1f, h * 0.25f),
            size = Size(w * 0.52f, h * 0.5f),
            cornerRadius = CornerRadius(4.dp.toPx()),
            style = Stroke(width = stroke)
        )
        val camTriangle = Path().apply {
            moveTo(w * 0.62f, h * 0.4f)
            lineTo(w * 0.9f, h * 0.25f)
            lineTo(w * 0.9f, h * 0.75f)
            lineTo(w * 0.62f, h * 0.6f)
            close()
        }
        drawPath(camTriangle, color = tint, style = Stroke(width = stroke))
    }
}

@Composable
private fun BookNavIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.8f.dp.toPx()

        // Book outline
        val bookLeft = Path().apply {
            moveTo(w * 0.5f, h * 0.25f)
            cubicTo(w * 0.35f, h * 0.15f, w * 0.15f, h * 0.2f, w * 0.1f, h * 0.25f)
            lineTo(w * 0.1f, h * 0.75f)
            cubicTo(w * 0.15f, h * 0.7f, w * 0.35f, h * 0.65f, w * 0.5f, h * 0.75f)
            close()
        }
        val bookRight = Path().apply {
            moveTo(w * 0.5f, h * 0.25f)
            cubicTo(w * 0.65f, h * 0.15f, w * 0.85f, h * 0.2f, w * 0.9f, h * 0.25f)
            lineTo(w * 0.9f, h * 0.75f)
            cubicTo(w * 0.85f, h * 0.7f, w * 0.65f, h * 0.65f, w * 0.5f, h * 0.75f)
            close()
        }
        drawPath(bookLeft, color = tint, style = Stroke(width = stroke))
        drawPath(bookRight, color = tint, style = Stroke(width = stroke))
    }
}

@Composable
private fun ProfileNavIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.8f.dp.toPx()

        // Head circle
        drawCircle(color = tint, radius = w * 0.22f, center = Offset(w * 0.5f, h * 0.3f), style = Stroke(width = stroke))
        // Shoulder arc
        drawArc(
            color = tint,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.18f, h * 0.52f),
            size = Size(w * 0.64f, h * 0.4f),
            style = Stroke(width = stroke)
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
fun TranslationScreenPreview() {
    TranslationScreen()
}
