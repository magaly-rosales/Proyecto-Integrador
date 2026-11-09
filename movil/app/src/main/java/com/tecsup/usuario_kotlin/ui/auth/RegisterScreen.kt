package com.tecsup.usuario_kotlin.ui.auth

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
private val CardTitleDark = Color(0xFF0C181E)
private val FieldLabelDark = Color(0xFF1C2A33)
private val FieldBg = Color(0xFFF2F4F6)
private val FieldBorder = Color(0xFFE5E9EC)
private val PlaceholderGray = Color(0xFF8E9AA0)
private val CheckboxTextDark = Color(0xFF3E4D56)
private val CardSubtitleGray = Color(0xFF6A7B84)

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var acceptedTerms by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    if (authViewModel.registerSuccess) {
        LaunchedEffect(Unit) {
            onRegisterSuccess()
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
                text = "Regístrate y empieza a\ntraducir hoy.",
                style = TextStyle(
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 34.sp
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
                    text = "Crea tu cuenta",
                    style = TextStyle(
                        color = CardTitleDark,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Completa tus datos para empezar.",
                    style = TextStyle(
                        color = CardSubtitleGray,
                        fontSize = 14.sp
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Field 1: Usuario
                Text(
                    text = "Usuario",
                    style = TextStyle(
                        color = FieldLabelDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    placeholder = {
                        Text(
                            "Tu usuario",
                            color = PlaceholderGray,
                            fontSize = 14.sp
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
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

                Spacer(modifier = Modifier.height(16.dp))

                // Field 2: Correo
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

                Spacer(modifier = Modifier.height(16.dp))

                // Field 3: Contraseña
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
                            "Mínimo 8 caracteres",
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

                Spacer(modifier = Modifier.height(16.dp))

                // Checkbox & Terms Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = acceptedTerms,
                        onCheckedChange = { acceptedTerms = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = PrimaryCyan,
                            uncheckedColor = PlaceholderGray
                        ),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(color = CheckboxTextDark, fontSize = 13.sp)) {
                                append("Acepto los ")
                            }
                            withStyle(style = SpanStyle(color = PrimaryCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)) {
                                append("términos y condiciones")
                            }
                            withStyle(style = SpanStyle(color = CheckboxTextDark, fontSize = 13.sp)) {
                                append(" y la ")
                            }
                            withStyle(style = SpanStyle(color = PrimaryCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)) {
                                append("política de privacidad")
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Submit Button
                Button(
                    onClick = { authViewModel.register(username, email, password) },
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
                            text = "Registrarse",
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

                Spacer(modifier = Modifier.height(20.dp))

                // Footer Text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "¿Ya tienes cuenta? ",
                        style = TextStyle(
                            color = CardSubtitleGray,
                            fontSize = 13.sp
                        )
                    )
                    Text(
                        text = "Iniciar sesión",
                        style = TextStyle(
                            color = PrimaryCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline
                        ),
                        modifier = Modifier.clickable { onLoginClick() }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen(
        authViewModel = AuthViewModel(),
        onRegisterSuccess = {},
        onLoginClick = {}
    )
}
