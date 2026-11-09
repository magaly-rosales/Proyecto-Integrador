package com.tecsup.usuario_kotlin.ui.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.usuario_kotlin.ui.viewmodel.AuthViewModel

// Color Constants matching design
private val DarkHeaderBg = Color(0xFF06141B)
private val PrimaryCyan = Color(0xFF14A3A7)
private val SubtitleGray = Color(0xFF9BB0BC)
private val CardTitleDark = Color(0xFF0C181E)
private val FieldLabelDark = Color(0xFF1C2A33)
private val FieldBg = Color(0xFFF2F4F6)
private val FieldBorder = Color(0xFFE5E9EC)
private val PlaceholderGray = Color(0xFF8E9AA0)
private val CheckboxTextDark = Color(0xFF3E4D56)
private val CardSubtitleGray = Color(0xFF6A7B84)

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    if (authViewModel.loginSuccess) {
        LaunchedEffect(Unit) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkHeaderBg)
    ) {
        // --- TOP SECTION (HEADER) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 20.dp, top = 40.dp, bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // App Logo
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LogoIcon(modifier = Modifier.size(44.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)) {
                                append("Señas")
                            }
                            withStyle(style = SpanStyle(color = PrimaryCyan, fontWeight = FontWeight.Bold, fontSize = 22.sp)) {
                                append("App")
                            }
                        }
                    )
                }

                // Skeleton Hand Tracking Graphic
                SkeletonHandGraphic(
                    modifier = Modifier
                        .size(width = 130.dp, height = 140.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cyan horizontal line accent
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .background(PrimaryCyan, shape = CircleShape)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Bienvenido a\nSeñasApp.",
                style = TextStyle(
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 34.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Ingresa a tu cuenta y sigue conversando sin\nbarreras.",
                style = TextStyle(
                    color = SubtitleGray,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            )
        }

        // --- BOTTOM CARD SECTION ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 28.dp)
            ) {
                Text(
                    text = "Iniciar sesión",
                    style = TextStyle(
                        color = CardTitleDark,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Ingresa con tu cuenta.",
                    style = TextStyle(
                        color = CardSubtitleGray,
                        fontSize = 14.sp
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Field 1: Usuario o correo
                Text(
                    text = "Usuario o correo",
                    style = TextStyle(
                        color = FieldLabelDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = {
                        Text(
                            "nombre@correo.com",
                            color = PlaceholderGray,
                            fontSize = 14.sp
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = FieldBg,
                        unfocusedContainerColor = FieldBg,
                        focusedBorderColor = PrimaryCyan,
                        unfocusedBorderColor = FieldBorder,
                        focusedTextColor = FieldLabelDark,
                        unfocusedTextColor = FieldLabelDark,
                        cursorColor = PrimaryCyan
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Field 2: Contraseña
                Text(
                    text = "Contraseña",
                    style = TextStyle(
                        color = FieldLabelDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = {
                        Text(
                            "........",
                            color = PlaceholderGray,
                            fontSize = 14.sp
                        )
                    },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            EyeToggleIcon(
                                isVisible = passwordVisible,
                                tint = CardSubtitleGray,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = FieldBg,
                        unfocusedContainerColor = FieldBg,
                        focusedBorderColor = PrimaryCyan,
                        unfocusedBorderColor = FieldBorder,
                        focusedTextColor = FieldLabelDark,
                        unfocusedTextColor = FieldLabelDark,
                        cursorColor = PrimaryCyan
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Checkbox & Forgot Password Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = PrimaryCyan,
                                uncheckedColor = PlaceholderGray
                            ),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Recordarme",
                            style = TextStyle(
                                color = CheckboxTextDark,
                                fontSize = 13.sp
                            )
                        )
                    }

                    Text(
                        text = "¿Olvidaste tu contraseña?",
                        style = TextStyle(
                            color = PrimaryCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline
                        ),
                        modifier = Modifier.clickable { /* Forgotten password logic if needed */ }
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Submit Button
                Button(
                    onClick = { authViewModel.login(email, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkHeaderBg,
                        contentColor = PrimaryCyan,
                        disabledContainerColor = DarkHeaderBg.copy(alpha = 0.7f),
                        disabledContentColor = PrimaryCyan.copy(alpha = 0.5f)
                    ),
                    enabled = !authViewModel.isLoading
                ) {
                    if (authViewModel.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = PrimaryCyan,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Iniciar sesión",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryCyan
                            )
                        )
                    }
                }

                authViewModel.errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Footer Text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "¿No tienes cuenta? ",
                        style = TextStyle(
                            color = CardSubtitleGray,
                            fontSize = 13.sp
                        )
                    )
                    Text(
                        text = "Regístrate",
                        style = TextStyle(
                            color = PrimaryCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline
                        ),
                        modifier = Modifier.clickable { onRegisterClick() }
                    )
                }
            }
        }
    }
}

// Custom App Logo Icon (Circular border + Hand gesture inside)
@Composable
internal fun LogoIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val diameter = size.minDimension
        val strokeWidthPx = 2.dp.toPx()
        val radius = (diameter - strokeWidthPx) / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // Outer cyan circle
        drawCircle(
            color = PrimaryCyan,
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidthPx)
        )

        // Hand shape inside
        val handColor = PrimaryCyan
        val scale = diameter / 44f

        // Draw 5 finger caps and palm connecting lines
        val fingerStroke = 2.dp.toPx()
        val cX = center.x
        val cY = center.y

        // Thumb
        drawLine(
            color = handColor,
            start = Offset(cX - 8f * scale, cY + 4f * scale),
            end = Offset(cX - 12f * scale, cY - 2f * scale),
            strokeWidth = fingerStroke,
            cap = StrokeCap.Round
        )
        // Index
        drawLine(
            color = handColor,
            start = Offset(cX - 5f * scale, cY + 6f * scale),
            end = Offset(cX - 6f * scale, cY - 9f * scale),
            strokeWidth = fingerStroke,
            cap = StrokeCap.Round
        )
        // Middle
        drawLine(
            color = handColor,
            start = Offset(cX, cY + 7f * scale),
            end = Offset(cX, cY - 11f * scale),
            strokeWidth = fingerStroke,
            cap = StrokeCap.Round
        )
        // Ring
        drawLine(
            color = handColor,
            start = Offset(cX + 5f * scale, cY + 6f * scale),
            end = Offset(cX + 5f * scale, cY - 9f * scale),
            strokeWidth = fingerStroke,
            cap = StrokeCap.Round
        )
        // Pinky
        drawLine(
            color = handColor,
            start = Offset(cX + 9f * scale, cY + 5f * scale),
            end = Offset(cX + 10f * scale, cY - 4f * scale),
            strokeWidth = fingerStroke,
            cap = StrokeCap.Round
        )
        // Palm base arc line
        drawArc(
            color = handColor,
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(cX - 9f * scale, cY + 2f * scale),
            size = Size(18f * scale, 10f * scale),
            style = Stroke(width = fingerStroke, cap = StrokeCap.Round)
        )
    }
}

// Custom Hand Tracking Skeleton Vector Graphic
@Composable
internal fun SkeletonHandGraphic(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Define joint points matching hand landmark pattern
        val wrist = Offset(w * 0.65f, h * 0.88f)

        // Thumb
        val t1 = Offset(w * 0.50f, h * 0.74f)
        val t2 = Offset(w * 0.35f, h * 0.65f)
        val t3 = Offset(w * 0.22f, h * 0.52f)
        val t4 = Offset(w * 0.12f, h * 0.42f)

        // Index
        val i1 = Offset(w * 0.50f, h * 0.48f)
        val i2 = Offset(w * 0.42f, h * 0.33f)
        val i3 = Offset(w * 0.37f, h * 0.20f)
        val i4 = Offset(w * 0.34f, h * 0.08f)

        // Middle
        val m1 = Offset(w * 0.62f, h * 0.44f)
        val m2 = Offset(w * 0.60f, h * 0.28f)
        val m3 = Offset(w * 0.58f, h * 0.16f)
        val m4 = Offset(w * 0.57f, h * 0.05f)

        // Ring
        val r1 = Offset(w * 0.74f, h * 0.48f)
        val r2 = Offset(w * 0.76f, h * 0.34f)
        val r3 = Offset(w * 0.78f, h * 0.22f)
        val r4 = Offset(w * 0.80f, h * 0.12f)

        // Pinky
        val p1 = Offset(w * 0.84f, h * 0.55f)
        val p2 = Offset(w * 0.88f, h * 0.45f)
        val p3 = Offset(w * 0.92f, h * 0.37f)
        val p4 = Offset(w * 0.95f, h * 0.29f)

        val strokePx = 2.dp.toPx()
        val nodeRadiusPx = 3.8f.dp.toPx()
        val innerDotPx = 1.8f.dp.toPx()
        val cyanColor = PrimaryCyan

        fun drawBone(pA: Offset, pB: Offset) {
            drawLine(color = cyanColor, start = pA, end = pB, strokeWidth = strokePx, cap = StrokeCap.Round)
        }

        // Draw connections (bones)
        // Thumb branch
        drawBone(wrist, t1)
        drawBone(t1, t2)
        drawBone(t2, t3)
        drawBone(t3, t4)

        // Index branch
        drawBone(wrist, i1)
        drawBone(i1, i2)
        drawBone(i2, i3)
        drawBone(i3, i4)

        // Middle branch
        drawBone(wrist, m1)
        drawBone(m1, m2)
        drawBone(m2, m3)
        drawBone(m3, m4)

        // Ring branch
        drawBone(wrist, r1)
        drawBone(r1, r2)
        drawBone(r2, r3)
        drawBone(r3, r4)

        // Pinky branch
        drawBone(wrist, p1)
        drawBone(p1, p2)
        drawBone(p2, p3)
        drawBone(p3, p4)

        // Palm connections
        drawBone(t1, i1)
        drawBone(i1, m1)
        drawBone(m1, r1)
        drawBone(r1, p1)

        // Draw joint nodes
        val allPoints = listOf(
            wrist,
            t1, t2, t3, t4,
            i1, i2, i3, i4,
            m1, m2, m3, m4,
            r1, r2, r3, r4,
            p1, p2, p3, p4
        )

        for (pt in allPoints) {
            drawCircle(color = cyanColor, radius = nodeRadiusPx, center = pt)
            drawCircle(color = Color.White, radius = innerDotPx, center = pt)
        }
    }
}

// Eye Icon Composable for password visibility toggle
@Composable
internal fun EyeToggleIcon(
    isVisible: Boolean,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cX = w / 2f
        val cY = h / 2f
        val strokeWidthPx = 1.8f.dp.toPx()

        // Upper curve of eye
        val eyePath = Path().apply {
            moveTo(w * 0.15f, cY)
            cubicTo(
                w * 0.35f, cY - h * 0.35f,
                w * 0.65f, cY - h * 0.35f,
                w * 0.85f, cY
            )
            cubicTo(
                w * 0.65f, cY + h * 0.35f,
                w * 0.35f, cY + h * 0.35f,
                w * 0.15f, cY
            )
            close()
        }

        drawPath(
            path = eyePath,
            color = tint,
            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
        )

        // Center pupil circle
        drawCircle(
            color = tint,
            radius = w * 0.14f,
            center = Offset(cX, cY)
        )

        // Slash line if hidden
        if (!isVisible) {
            drawLine(
                color = tint,
                start = Offset(w * 0.2f, h * 0.8f),
                end = Offset(w * 0.8f, h * 0.2f),
                strokeWidth = strokeWidthPx,
                cap = StrokeCap.Round
            )
        }
    }
}

// Preview
@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
fun LoginScreenPreview() {
    LoginScreen(
        authViewModel = AuthViewModel(),
        onLoginSuccess = {}
    )
}
