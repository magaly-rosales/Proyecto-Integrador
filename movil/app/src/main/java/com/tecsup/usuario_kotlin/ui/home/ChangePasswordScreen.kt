package com.tecsup.usuario_kotlin.ui.home

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.usuario_kotlin.ui.viewmodel.AuthViewModel

// Colores correspondientes al diseño original
private val BgDark = Color(0xFF08141A)
private val CardDark = Color(0xFF0F2129)
private val CardElevated = Color(0xFF132A34)
private val AccentCyan = Color(0xFF22B8CF)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF8A9BA8)
private val ErrorColor = Color(0xFFFF8585)

@Composable
fun ChangePasswordScreen(
    authViewModel: AuthViewModel,
    onBackClick: () -> Unit
) {

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // Las contraseñas se mantienen temporalmente en memoria
    var actual by remember { mutableStateOf("") }
    var nueva by remember { mutableStateOf("") }
    var confirmacion by remember { mutableStateOf("") }

    val isChanging = authViewModel.isChangingPassword

    // Limpiar los campos al abandonar la pantalla
    val limpiarCampos: () -> Unit = {
        actual = ""
        nueva = ""
        confirmacion = ""
        focusManager.clearFocus()
    }

    // Regresar a la pantalla de edición del perfil
    val regresar: () -> Unit = {
        if (!isChanging) {

            limpiarCampos()

            authViewModel.resetPasswordChangeState()

            onBackClick()
        }
    }

    // Controlar el botón de retroceso de Android
    BackHandler {
        regresar()
    }

    // Procesar un cambio de contraseña exitoso
    LaunchedEffect(authViewModel.passwordChangeSuccess) {

        if (authViewModel.passwordChangeSuccess) {

            limpiarCampos()

            Toast.makeText(
                context,
                "Contraseña actualizada correctamente",
                Toast.LENGTH_SHORT
            ).show()

            authViewModel.resetPasswordChangeState()

            onBackClick()
        }
    }

    Scaffold(
        containerColor = BgDark
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // Cabecera de la pantalla
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = regresar,
                    enabled = !isChanging
                ) {

                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Regresar",
                        tint = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "Contraseña",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Encabezado de seguridad
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(CardElevated),
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector = Icons.Outlined.Security,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Cambiar contraseña",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Protege la seguridad de tu cuenta.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }

            // Tarjeta del formulario
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = CardDark,
                border = BorderStroke(1.dp, CardElevated)
            ) {

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Text(
                        text = "Datos de seguridad",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Ingresa tu contraseña actual y la nueva contraseña.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Contraseña actual
                    PasswordInputField(
                        label = "Contraseña actual",
                        placeholder = "Ingresa tu contraseña actual",
                        value = actual,
                        onValueChange = {
                            actual = it
                            authViewModel.resetPasswordChangeState()
                        },
                        enabled = !isChanging
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Nueva contraseña
                    PasswordInputField(
                        label = "Nueva contraseña",
                        placeholder = "Ingresa tu nueva contraseña",
                        value = nueva,
                        onValueChange = {
                            nueva = it
                            authViewModel.resetPasswordChangeState()
                        },
                        enabled = !isChanging
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Debe contener entre 6 y 30 caracteres.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Confirmación de la contraseña
                    PasswordInputField(
                        label = "Confirmar contraseña",
                        placeholder = "Repite tu nueva contraseña",
                        value = confirmacion,
                        onValueChange = {
                            confirmacion = it
                            authViewModel.resetPasswordChangeState()
                        },
                        enabled = !isChanging,
                        isError = confirmacion.isNotEmpty() &&
                                nueva != confirmacion
                    )

                    // Validación visual de coincidencia
                    if (
                        confirmacion.isNotEmpty() &&
                        nueva != confirmacion
                    ) {

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Las contraseñas no coinciden",
                            color = ErrorColor,
                            fontSize = 11.sp
                        )
                    }

                    // Mostrar errores de validación o del servidor
                    authViewModel.passwordErrorMessage?.let { error ->

                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = ErrorColor.copy(alpha = 0.10f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                1.dp,
                                ErrorColor.copy(alpha = 0.35f)
                            )
                        ) {

                            Text(
                                text = error,
                                color = ErrorColor,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón actualizar contraseña
                    Button(
                        onClick = {

                            focusManager.clearFocus()

                            authViewModel.cambiarPassword(
                                actual = actual,
                                nueva = nueva,
                                confirmacion = confirmacion
                            )
                        },
                        enabled = !isChanging,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentCyan,
                            contentColor = BgDark,
                            disabledContainerColor =
                                AccentCyan.copy(alpha = 0.5f),
                            disabledContentColor = BgDark
                        )
                    ) {

                        if (isChanging) {

                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = BgDark,
                                strokeWidth = 2.dp
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = "Actualizando...",
                                fontWeight = FontWeight.Bold
                            )

                        } else {

                            Text(
                                text = "Actualizar contraseña",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Botón cancelar
                    OutlinedButton(
                        onClick = regresar,
                        enabled = !isChanging,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            TextSecondary.copy(alpha = 0.5f)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextSecondary
                        )
                    ) {

                        Text(
                            text = "Cancelar",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Recomendación de seguridad
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CardDark,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CardElevated)
            ) {

                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = Icons.Outlined.Security,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Utiliza una contraseña única, larga y difícil de adivinar.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// Componente reutilizable para los campos de contraseña
@Composable
private fun PasswordInputField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true,
    isError: Boolean = false
) {

    var passwordVisible by remember {
        mutableStateOf(false)
    }

    Column {

        Text(
            text = label,
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true,
            isError = isError,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                autoCorrectEnabled = false
            ),
            visualTransformation = if (
                passwordVisible && enabled
            ) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            placeholder = {
                Text(
                    text = placeholder,
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = AccentCyan
                )
            },
            trailingIcon = {

                IconButton(
                    onClick = {
                        passwordVisible = !passwordVisible
                    },
                    enabled = enabled
                ) {

                    Icon(
                        imageVector = if (passwordVisible) {
                            Icons.Outlined.VisibilityOff
                        } else {
                            Icons.Outlined.Visibility
                        },
                        contentDescription = if (passwordVisible) {
                            "Ocultar contraseña"
                        } else {
                            "Mostrar contraseña"
                        },
                        tint = TextSecondary
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CardElevated,
                unfocusedContainerColor = CardElevated,
                disabledContainerColor = CardElevated,
                errorContainerColor = CardElevated,
                focusedBorderColor = AccentCyan,
                unfocusedBorderColor =
                    TextSecondary.copy(alpha = 0.35f),
                disabledBorderColor =
                    TextSecondary.copy(alpha = 0.20f),
                errorBorderColor = ErrorColor,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                disabledTextColor = TextSecondary,
                errorTextColor = TextPrimary,
                cursorColor = AccentCyan,
                errorCursorColor = ErrorColor
            )
        )
    }
}