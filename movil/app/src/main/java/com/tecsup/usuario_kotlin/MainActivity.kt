package com.tecsup.usuario_kotlin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.tecsup.usuario_kotlin.data.model.TextSize
import com.tecsup.usuario_kotlin.data.remote.RetrofitClient
import com.tecsup.usuario_kotlin.data.repository.NotificationRepository
import com.tecsup.usuario_kotlin.data.repository.TranslationPreferencesRepository
import com.tecsup.usuario_kotlin.ui.auth.ForgotPasswordScreen
import com.tecsup.usuario_kotlin.ui.auth.LoginScreen
import com.tecsup.usuario_kotlin.ui.auth.RegisterScreen
import com.tecsup.usuario_kotlin.ui.home.ChangePasswordScreen
import com.tecsup.usuario_kotlin.ui.home.EditProfileScreen
import com.tecsup.usuario_kotlin.ui.home.HomeScreen
import com.tecsup.usuario_kotlin.ui.home.NotificationsScreen
import com.tecsup.usuario_kotlin.ui.home.ProfileScreen
import com.tecsup.usuario_kotlin.ui.home.SettingsScreen
import com.tecsup.usuario_kotlin.ui.home.UserGuideScreen
import com.tecsup.usuario_kotlin.ui.theme.UsuariokotlinTheme
import com.tecsup.usuario_kotlin.ui.translation.TranslationScreen
import com.tecsup.usuario_kotlin.ui.viewmodel.AuthViewModel
import com.tecsup.usuario_kotlin.ui.viewmodel.ProfileViewModel
import com.tecsup.usuario_kotlin.viewmodel.TranslationViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    private val profileViewModel: ProfileViewModel by viewModels {
        ProfileViewModel.factory(authViewModel.sessionState)
    }

    private val translationViewModel: TranslationViewModel by viewModels()

    private val protectedScreens = setOf(
        "home",
        "translation",
        "perfil",
        "edit_profile",
        "change_password",
        "settings",
        "notifications_translation",
        "notifications_profile",
        "user_guide"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            val userId = authViewModel.loggedUserId

            // Repositorio compartido de preferencias
            val preferencesRepository = remember(userId) {
                userId?.takeIf { it > 0 }?.let {
                    TranslationPreferencesRepository(
                        applicationContext,
                        it
                    )
                }
            }

            // Repositorio compartido de notificaciones
            val notificationRepository = remember(userId) {
                userId?.takeIf { it > 0 }?.let {
                    NotificationRepository(
                        applicationContext,
                        it
                    )
                }
            }

            // Generar bienvenida una sola vez por cuenta
            LaunchedEffect(
                notificationRepository,
                authViewModel.loginSuccess
            ) {
                if (authViewModel.loginSuccess) {
                    notificationRepository
                        ?.generarBienvenida()
                }
            }

            // Observar notificaciones del usuario
            val notifications = notificationRepository
                ?.notifications
                ?.collectAsStateWithLifecycle()
                ?.value ?: emptyList()

            // Cantidad de notificaciones sin leer
            val notificationCount = notifications.count {
                !it.leida
            }

            // Preferencias visuales
            val darkModeEnabled = preferencesRepository
                ?.darkMode
                ?.collectAsStateWithLifecycle()
                ?.value ?: true

            val textSize = preferencesRepository
                ?.textSize
                ?.collectAsStateWithLifecycle()
                ?.value ?: TextSize.NORMAL

            UsuariokotlinTheme(
                darkTheme = darkModeEnabled,
                textSize = textSize
            ) {
                val screenSaver = Saver<String, String>(
                    save = { it },
                    restore = { savedScreen ->
                        if (
                            savedScreen in protectedScreens &&
                            !authViewModel.loginSuccess
                        ) {
                            "login"
                        } else {
                            savedScreen
                        }
                    }
                )

                var currentScreen by rememberSaveable(
                    stateSaver = screenSaver
                ) {
                    mutableStateOf("login")
                }

                // Proteger las pantallas privadas
                val activeScreen = if (
                    currentScreen in protectedScreens &&
                    (
                            !authViewModel.loginSuccess ||
                                    preferencesRepository == null ||
                                    notificationRepository == null
                            )
                ) {
                    "login"
                } else {
                    currentScreen
                }

                // Datos del usuario autenticado
                val userName = authViewModel.loggedUserName
                val userEmail = authViewModel.loggedUserEmail
                val userPhoto = authViewModel.loggedUserPhoto

                val userInitial = userName.trim()
                    .firstOrNull()?.uppercase() ?: "U"

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (activeScreen) {

                        // Inicio de sesión
                        "login" -> {
                            LoginScreen(
                                authViewModel = authViewModel,

                                onLoginSuccess = {
                                    currentScreen = "translation"
                                },

                                onRegisterClick = {
                                    authViewModel.resetRegisterSuccess()
                                    currentScreen = "register"
                                },

                                onForgotPasswordClick = {
                                    authViewModel.resetForgotPasswordSuccess()
                                    currentScreen = "forgot_password"
                                }
                            )
                        }

                        // Registro
                        "register" -> {
                            RegisterScreen(
                                authViewModel = authViewModel,

                                onRegisterSuccess = {
                                    authViewModel.resetRegisterSuccess()
                                    currentScreen = "login"
                                },

                                onLoginClick = {
                                    currentScreen = "login"
                                }
                            )
                        }

                        // Recuperación de contraseña
                        "forgot_password" -> {
                            ForgotPasswordScreen(
                                authViewModel = authViewModel,

                                onBackClick = {
                                    authViewModel.resetForgotPasswordSuccess()
                                    currentScreen = "login"
                                }
                            )
                        }

                        // Catálogo y progreso del usuario
                        "home" -> {
                            if (
                                userId != null &&
                                userId > 0 &&
                                notificationRepository != null
                            ) {
                                key(userId) {
                                    HomeScreen(
                                        username = userName,
                                        userId = userId,

                                        onNavigateToTranslator = {
                                            currentScreen = "translation"
                                        },

                                        onNavigateToPerfil = {
                                            currentScreen = "perfil"
                                        },

                                        viewModel = translationViewModel,

                                        // Notificar práctica exitosa
                                        onPracticeCompleted = { nombreSena ->
                                            notificationRepository
                                                .notificarPracticaExitosa(
                                                    nombreSena
                                                )
                                        }
                                    )
                                }
                            } else {
                                LaunchedEffect(Unit) {
                                    cerrarSesion()
                                    currentScreen = "login"
                                }
                            }
                        }

                        // Traductor
                        "translation" -> {
                            if (
                                userId != null &&
                                userId > 0 &&
                                preferencesRepository != null
                            ) {
                                key(userId) {
                                    TranslationScreen(
                                        viewModel = translationViewModel,
                                        userId = userId,

                                        preferencesRepository =
                                            preferencesRepository,

                                        userInitial = userInitial,

                                        notificationCount =
                                            notificationCount,

                                        onNavigateToSenas = {
                                            currentScreen = "home"
                                        },

                                        onNavigateToPerfil = {
                                            currentScreen = "perfil"
                                        },

                                        onNavigateToNotifications = {
                                            currentScreen =
                                                "notifications_translation"
                                        },

                                        onSpeakText = { nombreSena ->
                                            registrarTraduccion(nombreSena)
                                        }
                                    )
                                }
                            } else {
                                LaunchedEffect(Unit) {
                                    cerrarSesion()
                                    currentScreen = "login"
                                }
                            }
                        }

                        // Mi cuenta
                        "perfil" -> {
                            ProfileScreen(
                                userName = userName,
                                userEmail = userEmail,
                                userPhoto = userPhoto,

                                notificationCount =
                                    notificationCount,

                                darkModeEnabled = darkModeEnabled,

                                onDarkModeChange = { enabled ->
                                    preferencesRepository
                                        ?.cambiarModoOscuro(enabled)
                                },

                                textSize = textSize,

                                onTextSizeChange = { size ->
                                    preferencesRepository
                                        ?.cambiarTamanoTexto(size)
                                },

                                onNavigateToSettings = {
                                    currentScreen = "settings"
                                },

                                onNavigateToNotifications = {
                                    currentScreen =
                                        "notifications_profile"
                                },

                                // Navegar a la guía de uso
                                onNavigateToGuide = {
                                    currentScreen = "user_guide"
                                },

                                onNavigateToTraducir = {
                                    currentScreen = "translation"
                                },

                                onNavigateToSenas = {
                                    currentScreen = "home"
                                },

                                onLogout = {
                                    cerrarSesion()
                                    currentScreen = "login"
                                },

                                onNavigateToEditProfile = {
                                    profileViewModel.resetProfileUpdateState()
                                    profileViewModel.resetPhotoUpdateState()
                                    currentScreen = "edit_profile"
                                }
                            )
                        }

                        // Notificaciones desde traductor o perfil
                        "notifications_translation",
                        "notifications_profile" -> {
                            if (
                                userId != null &&
                                notificationRepository != null
                            ) {
                                val returnScreen = if (
                                    activeScreen ==
                                    "notifications_translation"
                                ) {
                                    "translation"
                                } else {
                                    "perfil"
                                }

                                key(userId, activeScreen) {
                                    NotificationsScreen(
                                        repository =
                                            notificationRepository,

                                        onBackClick = {
                                            currentScreen = returnScreen
                                        }
                                    )
                                }
                            } else {
                                LaunchedEffect(Unit) {
                                    cerrarSesion()
                                    currentScreen = "login"
                                }
                            }
                        }

                        // Configuración de preferencias
                        "settings" -> {
                            if (
                                userId != null &&
                                preferencesRepository != null
                            ) {
                                key(userId) {
                                    SettingsScreen(
                                        preferencesRepository =
                                            preferencesRepository,

                                        onBackClick = {
                                            currentScreen = "perfil"
                                        }
                                    )
                                }
                            } else {
                                LaunchedEffect(Unit) {
                                    cerrarSesion()
                                    currentScreen = "login"
                                }
                            }
                        }

                        // Guía de uso de SeñasApp
                        "user_guide" -> {
                            if (
                                userId != null &&
                                userId > 0
                            ) {
                                key(userId) {
                                    UserGuideScreen(
                                        onBackClick = {
                                            currentScreen = "perfil"
                                        }
                                    )
                                }
                            } else {
                                LaunchedEffect(Unit) {
                                    cerrarSesion()
                                    currentScreen = "login"
                                }
                            }
                        }

                        // Editar perfil
                        "edit_profile" -> {
                            EditProfileScreen(
                                profileViewModel = profileViewModel,

                                onBackClick = {
                                    currentScreen = "perfil"
                                },

                                onNavigateToChangePassword = {
                                    profileViewModel.resetPasswordChangeState()
                                    profileViewModel.resetPhotoUpdateState()
                                    currentScreen = "change_password"
                                }
                            )
                        }

                        // Cambiar contraseña
                        "change_password" -> {
                            ChangePasswordScreen(
                                profileViewModel = profileViewModel,

                                onBackClick = {
                                    currentScreen = "edit_profile"
                                }
                            )
                        }

                        // Pantalla de respaldo
                        else -> {
                            LoginScreen(
                                authViewModel = authViewModel,

                                onLoginSuccess = {
                                    currentScreen = "translation"
                                },

                                onRegisterClick = {
                                    authViewModel.resetRegisterSuccess()
                                    currentScreen = "register"
                                },

                                onForgotPasswordClick = {
                                    authViewModel.resetForgotPasswordSuccess()
                                    currentScreen = "forgot_password"
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Registrar las señas reconocidas
    private fun registrarTraduccion(nombreSena: String) {
        lifecycleScope.launch {
            try {
                val respuesta = RetrofitClient.apiService
                    .registrarTraduccion(
                        mapOf("sena" to nombreSena)
                    )

                if (!respuesta.isSuccessful) {
                    Toast.makeText(
                        this@MainActivity,
                        "No se pudo guardar la traducción",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Error al guardar la traducción",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Cerrar sesión conservando los datos locales
    private fun cerrarSesion() {
        translationViewModel.cerrarSesionTraductor()
        profileViewModel.clearProfileOperations()
        authViewModel.logout()
    }
}