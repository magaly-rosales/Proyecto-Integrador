package com.tecsup.usuario_kotlin.ui.home

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.usuario_kotlin.ui.components.*
import com.tecsup.usuario_kotlin.ui.theme.ProfileColors
import com.tecsup.usuario_kotlin.ui.viewmodel.ProfileViewModel

@Composable
fun ChangePasswordScreen(
    profileViewModel: ProfileViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // Las contraseñas permanecen temporalmente en memoria
    var actual by remember { mutableStateOf("") }
    var nueva by remember { mutableStateOf("") }
    var confirmacion by remember { mutableStateOf("") }

    val isChanging = profileViewModel.isChangingPassword
    val isBusy = profileViewModel.isBusy

    val passwordsNotMatch =
        confirmacion.isNotEmpty() && nueva != confirmacion

    val limpiarCampos: () -> Unit = {
        actual = ""
        nueva = ""
        confirmacion = ""
        focusManager.clearFocus()
    }

    val regresar: () -> Unit = {
        if (!isBusy) {
            limpiarCampos()
            profileViewModel.resetPasswordChangeState()
            onBackClick()
        }
    }

    BackHandler {
        regresar()
    }

    LaunchedEffect(profileViewModel.passwordChangeSuccess) {
        if (profileViewModel.passwordChangeSuccess) {

            limpiarCampos()

            Toast.makeText(
                context,
                "Contraseña actualizada correctamente",
                Toast.LENGTH_SHORT
            ).show()

            profileViewModel.resetPasswordChangeState()
            onBackClick()
        }
    }

    Scaffold(
        containerColor = ProfileColors.BgDark
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
            ScreenHeader(
                title = "Contraseña",
                onBackClick = regresar,
                enabled = !isBusy
            )

            PasswordSecurityHeader()

            // Formulario de cambio de contraseña
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = ProfileColors.CardDark,
                border = BorderStroke(
                    1.dp,
                    ProfileColors.CardElevated
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    Text(
                        text = "Datos de seguridad",
                        color = ProfileColors.TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Ingresa tu contraseña actual y la nueva contraseña.",
                        color = ProfileColors.TextSecondary,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    PasswordTextField(
                        label = "Contraseña actual",
                        placeholder = "Ingresa tu contraseña actual",
                        value = actual,
                        onValueChange = {
                            actual = it
                            profileViewModel.resetPasswordChangeState()
                        },
                        enabled = !isBusy
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    PasswordTextField(
                        label = "Nueva contraseña",
                        placeholder = "Ingresa tu nueva contraseña",
                        value = nueva,
                        onValueChange = {
                            nueva = it
                            profileViewModel.resetPasswordChangeState()
                        },
                        enabled = !isBusy
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Debe contener entre 6 y 30 caracteres.",
                        color = ProfileColors.TextSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    PasswordTextField(
                        label = "Confirmar contraseña",
                        placeholder = "Repite tu nueva contraseña",
                        value = confirmacion,
                        onValueChange = {
                            confirmacion = it
                            profileViewModel.resetPasswordChangeState()
                        },
                        enabled = !isBusy,
                        isError = passwordsNotMatch
                    )

                    if (passwordsNotMatch) {
                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Las contraseñas no coinciden",
                            color = ProfileColors.ErrorColor,
                            fontSize = 11.sp
                        )
                    }

                    ErrorMessage(
                        message = profileViewModel.passwordErrorMessage
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    PrimaryButton(
                        text = "Actualizar contraseña",
                        onClick = {
                            focusManager.clearFocus()

                            profileViewModel.cambiarPassword(
                                actual = actual,
                                nueva = nueva,
                                confirmacion = confirmacion
                            )
                        },
                        enabled = !isBusy,
                        isLoading = isChanging,
                        loadingText = "Actualizando..."
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SecondaryButton(
                        text = "Cancelar",
                        onClick = regresar,
                        enabled = !isBusy
                    )
                }
            }

            PasswordSecurityTip()
        }
    }
}

// Encabezado visual de seguridad
@Composable
private fun PasswordSecurityHeader() {

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(ProfileColors.CardElevated),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Security,
                contentDescription = null,
                tint = ProfileColors.AccentCyan,
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Cambiar contraseña",
            color = ProfileColors.TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Protege la seguridad de tu cuenta.",
            color = ProfileColors.TextSecondary,
            fontSize = 13.sp
        )
    }
}

// Recomendación de seguridad
@Composable
private fun PasswordSecurityTip() {

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ProfileColors.CardDark,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, ProfileColors.CardElevated)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Security,
                contentDescription = null,
                tint = ProfileColors.AccentCyan,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Utiliza una contraseña única, larga y difícil de adivinar.",
                color = ProfileColors.TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}