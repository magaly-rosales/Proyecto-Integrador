package com.tecsup.usuario_kotlin.ui.home

import android.content.Context
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
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
fun EditProfileScreen(
    authViewModel: AuthViewModel,
    onBackClick: () -> Unit
) {

    val context = LocalContext.current

    val userId = authViewModel.loggedUserId

    // Conservar los datos originales del usuario
    val originalEmail = rememberSaveable(userId) {
        authViewModel.loggedUserEmail
    }

    // Campos editables
    var nombre by rememberSaveable(userId) {
        mutableStateOf(authViewModel.loggedUserName)
    }

    var email by rememberSaveable(userId) {
        mutableStateOf(authViewModel.loggedUserEmail)
    }

    val isUpdating = authViewModel.isUpdatingProfile

    val initial = authViewModel.loggedUserName
        .trim()
        .firstOrNull()
        ?.uppercase() ?: "U"

    // Regresar al perfil sin guardar los cambios
    val regresar: () -> Unit = {
        if (!isUpdating) {
            authViewModel.resetProfileUpdateState()
            onBackClick()
        }
    }

    // Controlar el botón de retroceso de Android
    BackHandler {
        regresar()
    }

    // Procesar una actualización exitosa
    LaunchedEffect(authViewModel.profileUpdateSuccess) {

        if (authViewModel.profileUpdateSuccess) {

            // Sincronizar el correo de la opción Recordarme
            val preferences = context.getSharedPreferences(
                "senasapp_preferences",
                Context.MODE_PRIVATE
            )

            val rememberedEmail = preferences.getString(
                "remembered_email",
                null
            )

            // Actualizar solamente si el correo anterior
            // estaba guardado mediante Recordarme
            if (
                rememberedEmail != null &&
                rememberedEmail.equals(
                    originalEmail,
                    ignoreCase = true
                )
            ) {
                preferences.edit()
                    .putString(
                        "remembered_email",
                        authViewModel.loggedUserEmail
                    )
                    .apply()
            }

            Toast.makeText(
                context,
                "Perfil actualizado correctamente",
                Toast.LENGTH_SHORT
            ).show()

            authViewModel.resetProfileUpdateState()

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
                    enabled = !isUpdating
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
                    text = "Mi perfil",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Información del usuario
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(AccentCyan),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = initial,
                        color = BgDark,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = authViewModel.loggedUserName,
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = authViewModel.loggedUserEmail,
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }

            // Tarjeta de edición de datos personales
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
                        text = "Datos personales",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Actualiza la información de tu cuenta.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Campo nombre
                    Text(
                        text = "Nombre completo",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = {
                            nombre = it
                            authViewModel.resetProfileUpdateState()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isUpdating,
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        placeholder = {
                            Text(
                                text = "Ingresa tu nombre",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = AccentCyan
                            )
                        },
                        colors = profileTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "${nombre.length}/50 caracteres",
                        modifier = Modifier.align(Alignment.End),
                        color = if (nombre.length > 50) {
                            ErrorColor
                        } else {
                            TextSecondary
                        },
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Campo correo electrónico
                    Text(
                        text = "Correo electrónico",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            authViewModel.resetProfileUpdateState()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isUpdating,
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email
                        ),
                        placeholder = {
                            Text(
                                text = "nombre@correo.com",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Email,
                                contentDescription = null,
                                tint = AccentCyan
                            )
                        },
                        colors = profileTextFieldColors()
                    )

                    // Mostrar errores de validación o del servidor
                    authViewModel.profileErrorMessage?.let { error ->

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

                    // Botón guardar cambios
                    Button(
                        onClick = {
                            authViewModel.actualizarPerfil(
                                nombre = nombre,
                                email = email
                            )
                        },
                        enabled = !isUpdating,
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

                        if (isUpdating) {

                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = BgDark,
                                strokeWidth = 2.dp
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = "Guardando...",
                                fontWeight = FontWeight.Bold
                            )

                        } else {

                            Text(
                                text = "Guardar cambios",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Botón cancelar
                    OutlinedButton(
                        onClick = regresar,
                        enabled = !isUpdating,
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
        }
    }
}

// Estilos reutilizables para los campos de texto
@Composable
private fun profileTextFieldColors(): TextFieldColors {

    return OutlinedTextFieldDefaults.colors(
        focusedContainerColor = CardElevated,
        unfocusedContainerColor = CardElevated,
        disabledContainerColor = CardElevated,
        focusedBorderColor = AccentCyan,
        unfocusedBorderColor = TextSecondary.copy(alpha = 0.35f),
        disabledBorderColor = TextSecondary.copy(alpha = 0.20f),
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        disabledTextColor = TextSecondary,
        cursorColor = AccentCyan
    )
}