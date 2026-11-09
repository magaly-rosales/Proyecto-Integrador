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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
private val CardSubtitleGray = Color(0xFF6A7B84)

@Composable
fun ForgotPasswordScreen(
    authViewModel: AuthViewModel,
    onBackClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }

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
                text = "¿Olvidaste tu clave?",
                style = TextStyle(
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 34.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Tranquilo, te ayudamos a recuperarla en un\npar de pasos.",
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
                // Back button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onBackClick() }
                ) {
                    BackChevronIcon(
                        tint = FieldLabelDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Volver",
                        style = TextStyle(
                            color = FieldLabelDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Recuperar contraseña",
                    style = TextStyle(
                        color = CardTitleDark,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Escribe el correo de tu cuenta y te enviaremos un\nenlace para crear una nueva contraseña.",
                    style = TextStyle(
                        color = CardSubtitleGray,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Field: Correo
                Text(
                    text = "Correo",
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

                Spacer(modifier = Modifier.height(24.dp))

                // Submit Button
                Button(
                    onClick = { authViewModel.forgotPassword(email) },
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
                            text = "Enviar enlace",
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

                authViewModel.forgotPasswordMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = msg,
                        color = PrimaryCyan,
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }
}

// Back Chevron Icon (<)
@Composable
private fun BackChevronIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidthPx = 2.5f.dp.toPx()

        drawLine(
            color = tint,
            start = androidx.compose.ui.geometry.Offset(w * 0.75f, h * 0.15f),
            end = androidx.compose.ui.geometry.Offset(w * 0.25f, h * 0.5f),
            strokeWidth = strokeWidthPx,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = androidx.compose.ui.geometry.Offset(w * 0.25f, h * 0.5f),
            end = androidx.compose.ui.geometry.Offset(w * 0.75f, h * 0.85f),
            strokeWidth = strokeWidthPx,
            cap = StrokeCap.Round
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
fun ForgotPasswordScreenPreview() {
    ForgotPasswordScreen(
        authViewModel = AuthViewModel(),
        onBackClick = {}
    )
}
