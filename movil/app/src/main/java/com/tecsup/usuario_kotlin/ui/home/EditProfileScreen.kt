package com.tecsup.usuario_kotlin.ui.home

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.usuario_kotlin.ui.components.ErrorMessage
import com.tecsup.usuario_kotlin.ui.components.ProfileAvatar as SharedProfileAvatar
import com.tecsup.usuario_kotlin.ui.components.ScreenHeader
import com.tecsup.usuario_kotlin.ui.home.components.*
import com.tecsup.usuario_kotlin.ui.theme.ProfileColors
import com.tecsup.usuario_kotlin.ui.viewmodel.ProfileViewModel
import com.tecsup.usuario_kotlin.utils.prepararFotoPerfil as prepararImagen
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun EditProfileScreen(
    profileViewModel: ProfileViewModel,
    onBackClick: () -> Unit,
    onNavigateToChangePassword: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userId = profileViewModel.loggedUserId

    val originalEmail = rememberSaveable(userId) {
        profileViewModel.loggedUserEmail
    }

    var nombre by rememberSaveable(userId) {
        mutableStateOf(profileViewModel.loggedUserName)
    }

    var email by rememberSaveable(userId) {
        mutableStateOf(profileViewModel.loggedUserEmail)
    }

    var showPhotoOptions by rememberSaveable(userId) {
        mutableStateOf(false)
    }

    var showDeletePhotoDialog by rememberSaveable(userId) {
        mutableStateOf(false)
    }

    var showUnsavedChangesDialog by rememberSaveable(userId) {
        mutableStateOf(false)
    }

    var isPreparingPhoto by remember(userId) {
        mutableStateOf(false)
    }

    val isUpdating = profileViewModel.isUpdatingProfile

    val isPhotoBusy =
        isPreparingPhoto || profileViewModel.isUpdatingPhoto

    val isBusy =
        isPreparingPhoto || profileViewModel.isBusy

    val hasPhoto =
        !profileViewModel.loggedUserPhoto.isNullOrBlank()

    val hasUnsavedChanges =
        nombre != profileViewModel.loggedUserName ||
                email != profileViewModel.loggedUserEmail

    // Selector de fotografías
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->

        if (
            uri != null && userId != null &&
            !isBusy && profileViewModel.loginSuccess
        ) {
            profileViewModel.resetPhotoUpdateState()
            isPreparingPhoto = true

            scope.launch {
                try {
                    val foto = prepararImagen(
                        context.applicationContext,
                        uri
                    )

                    if (
                        profileViewModel.loginSuccess &&
                        profileViewModel.loggedUserId == userId
                    ) {
                        profileViewModel.actualizarFotoPerfil(foto)
                    }

                } catch (e: CancellationException) {
                    throw e

                } catch (e: Exception) {
                    if (profileViewModel.loggedUserId == userId) {
                        profileViewModel.mostrarErrorFoto(
                            if (e is IllegalArgumentException) {
                                e.message ?: "Imagen no válida"
                            } else {
                                "No se pudo procesar la imagen seleccionada"
                            }
                        )
                    }

                } finally {
                    isPreparingPhoto = false
                }
            }
        }
    }

    val abrirOpcionesFoto: () -> Unit = {
        if (!isBusy) {
            profileViewModel.resetPhotoUpdateState()
            showPhotoOptions = true
        }
    }

    val regresar: () -> Unit = {
        if (!isBusy) {
            profileViewModel.resetProfileUpdateState()
            profileViewModel.resetPhotoUpdateState()
            onBackClick()
        }
    }

    val navegarCambioPassword: () -> Unit = {
        profileViewModel.resetProfileUpdateState()
        profileViewModel.resetPasswordChangeState()
        profileViewModel.resetPhotoUpdateState()
        onNavigateToChangePassword()
    }

    val abrirCambioPassword: () -> Unit = {
        if (!isBusy) {
            if (hasUnsavedChanges) {
                showUnsavedChangesDialog = true
            } else {
                navegarCambioPassword()
            }
        }
    }

    BackHandler {
        when {
            showDeletePhotoDialog ->
                showDeletePhotoDialog = false

            showPhotoOptions ->
                showPhotoOptions = false

            showUnsavedChangesDialog ->
                showUnsavedChangesDialog = false

            else -> regresar()
        }
    }

    // Actualización exitosa de datos personales
    LaunchedEffect(profileViewModel.profileUpdateSuccess) {
        if (profileViewModel.profileUpdateSuccess) {

            val preferences = context.getSharedPreferences(
                "senasapp_preferences",
                Context.MODE_PRIVATE
            )

            val rememberedEmail = preferences.getString(
                "remembered_email", null
            )

            if (
                rememberedEmail != null &&
                rememberedEmail.equals(originalEmail, ignoreCase = true)
            ) {
                preferences.edit()
                    .putString(
                        "remembered_email",
                        profileViewModel.loggedUserEmail
                    )
                    .apply()
            }

            Toast.makeText(
                context,
                "Perfil actualizado correctamente",
                Toast.LENGTH_SHORT
            ).show()

            profileViewModel.resetProfileUpdateState()
            onBackClick()
        }
    }

    // Actualización exitosa de fotografía
    LaunchedEffect(profileViewModel.photoUpdateSuccess) {
        if (profileViewModel.photoUpdateSuccess) {
            Toast.makeText(
                context,
                profileViewModel.photoSuccessMessage
                    ?: "Foto de perfil actualizada correctamente",
                Toast.LENGTH_SHORT
            ).show()

            profileViewModel.resetPhotoUpdateState()
        }
    }

    // Diálogos reutilizables
    if (showPhotoOptions) {
        PhotoOptionsDialog(
            hasPhoto = hasPhoto,
            enabled = !isBusy,
            onDismiss = { showPhotoOptions = false },
            onSelectPhoto = {
                showPhotoOptions = false

                try {
                    photoLauncher.launch("image/*")
                } catch (e: Exception) {
                    profileViewModel.mostrarErrorFoto(
                        "No se pudo abrir el selector de imágenes"
                    )
                }
            },
            onDeletePhoto = {
                showPhotoOptions = false
                showDeletePhotoDialog = true
            }
        )
    }

    if (showDeletePhotoDialog) {
        DeletePhotoDialog(
            enabled = !isBusy,
            onDismiss = { showDeletePhotoDialog = false },
            onConfirm = {
                showDeletePhotoDialog = false
                profileViewModel.eliminarFotoPerfil()
            }
        )
    }

    if (showUnsavedChangesDialog) {
        UnsavedChangesDialog(
            enabled = !isBusy,
            onDismiss = { showUnsavedChangesDialog = false },
            onConfirm = {
                showUnsavedChangesDialog = false
                nombre = profileViewModel.loggedUserName
                email = profileViewModel.loggedUserEmail
                navegarCambioPassword()
            }
        )
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
                title = "Mi perfil",
                onBackClick = regresar,
                enabled = !isBusy
            )

            ProfilePhotoSection(
                userName = profileViewModel.loggedUserName,
                userEmail = profileViewModel.loggedUserEmail,
                userPhoto = profileViewModel.loggedUserPhoto,
                hasPhoto = hasPhoto,
                isBusy = isBusy,
                isPhotoBusy = isPhotoBusy,
                isPreparingPhoto = isPreparingPhoto,
                errorMessage = profileViewModel.photoErrorMessage,
                onPhotoClick = abrirOpcionesFoto
            )

            PersonalDataCard(
                nombre = nombre,
                email = email,
                onNombreChange = {
                    nombre = it
                    profileViewModel.resetProfileUpdateState()
                },
                onEmailChange = {
                    email = it
                    profileViewModel.resetProfileUpdateState()
                },
                onSave = {
                    profileViewModel.actualizarPerfil(nombre, email)
                },
                onCancel = regresar,
                isBusy = isBusy,
                isUpdating = isUpdating,
                errorMessage = profileViewModel.profileErrorMessage
            )

            SecurityCard(
                onChangePassword = abrirCambioPassword,
                enabled = !isBusy
            )
        }
    }
}

// Sección visual de fotografía y datos del usuario
@Composable
private fun ProfilePhotoSection(
    userName: String,
    userEmail: String,
    userPhoto: String?,
    hasPhoto: Boolean,
    isBusy: Boolean,
    isPhotoBusy: Boolean,
    isPreparingPhoto: Boolean,
    errorMessage: String?,
    onPhotoClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.size(84.dp)) {

            SharedProfileAvatar(
                userName = userName,
                foto = userPhoto
            )

            IconButton(
                onClick = onPhotoClick,
                enabled = !isBusy,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(32.dp)
                    .background(ProfileColors.BgDark, CircleShape)
                    .padding(2.dp)
                    .background(ProfileColors.AccentCyan, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Outlined.CameraAlt,
                    contentDescription = "Cambiar foto de perfil",
                    tint = ProfileColors.BgDark,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = userName,
            color = ProfileColors.TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = userEmail,
            color = ProfileColors.TextSecondary,
            fontSize = 13.sp
        )

        if (isPhotoBusy) {
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = ProfileColors.AccentCyan,
                    strokeWidth = 2.dp
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = if (isPreparingPhoto) {
                        "Procesando imagen..."
                    } else {
                        "Actualizando foto..."
                    },
                    color = ProfileColors.AccentCyan,
                    fontSize = 12.sp
                )
            }
        } else {
            TextButton(
                onClick = onPhotoClick,
                enabled = !isBusy
            ) {
                Text(
                    text = if (hasPhoto) "Cambiar foto" else "Agregar foto",
                    color = ProfileColors.AccentCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        ErrorMessage(
            message = errorMessage,
            topSpacing = 8.dp
        )
    }
}