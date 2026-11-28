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
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    private val profileViewModel: ProfileViewModel by viewModels {
        ProfileViewModel.factory(authViewModel.sessionState)
    }

    private val translationViewModel: TranslationViewModel by viewModels()

    // Peticiones de registro asociadas a la sesión
    private var translationRegistrationJob: Job? = null

    // Pantallas que requieren autenticación
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

            val sessionVersion =
                authViewModel.sessionState.generation

            // Comprobar que exista una sesión válida
            val hasValidSession =
                authViewModel.loginSuccess &&
                        userId != null &&
                        userId > 0

            // Preferencias independientes por sesión
            val preferencesRepository = remember(
                userId,
                sessionVersion,
                hasValidSession
            ) {
                if (hasValidSession && userId != null) {
                    TranslationPreferencesRepository(
                        applicationContext,
                        userId
                    )
                } else {
                    null
                }
            }

            // Notificaciones independientes por sesión
            val notificationRepository = remember(
                userId,
                sessionVersion,
                hasValidSession
            ) {
                if (hasValidSession && userId != null) {
                    NotificationRepository(
                        applicationContext,
                        userId
                    )
                } else {
                    null
                }
            }

            // Generar bienvenida únicamente con sesión válida
            LaunchedEffect(
                notificationRepository,
                hasValidSession
            ) {
                if (hasValidSession) {
                    notificationRepository?.generarBienvenida()
                }
            }

            // Observar notificaciones
            val notifications = notificationRepository
                ?.notifications
                ?.collectAsStateWithLifecycle()
                ?.value ?: emptyList()

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
                // Impedir restaurar pantallas privadas sin sesión
                val screenSaver = Saver<String, String>(
                    save = { it },
                    restore = { savedScreen ->
                        if (
                            savedScreen in protectedScreens &&
                            !hasValidSession
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

                // Validación centralizada de navegación
                val canAccessPrivateScreens =
                    hasValidSession &&
                            preferencesRepository != null &&
                            notificationRepository != null

                val activeScreen = if (
                    currentScreen in protectedScreens &&
                    !canAccessPrivateScreens
                ) {
                    "login"
                } else {
                    currentScreen
                }

                // Limpiar una ruta privada que ya no es válida
                LaunchedEffect(
                    currentScreen,
                    canAccessPrivateScreens
                ) {
                    if (
                        currentScreen in protectedScreens &&
                        !canAccessPrivateScreens
                    ) {
                        cerrarSesion()
                        currentScreen = "login"
                    }
                }

                // Datos de la sesión actual
                val userName = authViewModel.loggedUserName
                val userEmail = authViewModel.loggedUserEmail
                val userPhoto = authViewModel.loggedUserPhoto
                val userRole = authViewModel.loggedUserRole

                val userInitial = userName.trim()
                    .firstOrNull()?.uppercase() ?: "U"

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Separar el estado visual de cada sesión
                    key(
                        if (activeScreen in protectedScreens) {
                            sessionVersion
                        } else {
                            -1L
                        }
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

                            // Catálogo y progreso
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

                                            // Comprobar la sesión del evento
                                            onPracticeCompleted = { nombreSena ->
                                                if (
                                                    authViewModel.sessionState
                                                        .esSesionActual(
                                                            userId,
                                                            sessionVersion
                                                        )
                                                ) {
                                                    notificationRepository
                                                        .notificarPracticaExitosa(
                                                            nombreSena
                                                        )
                                                }
                                            }
                                        )
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
                                                registrarTraduccion(
                                                    nombreSena = nombreSena,
                                                    userId = userId,
                                                    sessionVersion =
                                                        sessionVersion
                                                )
                                            }
                                        )
                                    }
                                }
                            }

                            // Mi cuenta
                            "perfil" -> {
                                ProfileScreen(
                                    userName = userName,
                                    userEmail = userEmail,
                                    userPhoto = userPhoto,

                                    // Rol recibido del servidor
                                    userRole = userRole,

                                    notificationCount =
                                        notificationCount,

                                    darkModeEnabled =
                                        darkModeEnabled,

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
                                        profileViewModel
                                            .resetProfileUpdateState()

                                        profileViewModel
                                            .resetPhotoUpdateState()

                                        currentScreen = "edit_profile"
                                    }
                                )
                            }

                            // Notificaciones
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
                                }
                            }

                            // Configuración
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
                                }
                            }

                            // Guía de uso
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
                                        profileViewModel
                                            .resetPasswordChangeState()

                                        profileViewModel
                                            .resetPhotoUpdateState()

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
    }

    // Registrar una traducción de la sesión actual
    private fun registrarTraduccion(
        nombreSena: String,
        userId: Int,
        sessionVersion: Long
    ) {
        val nombre = nombreSena.trim()

        if (nombre.isBlank()) return

        // No iniciar peticiones de una sesión anterior
        if (
            !authViewModel.sessionState.esSesionActual(
                userId,
                sessionVersion
            )
        ) return

        // Crear un grupo de peticiones vinculado a la actividad
        val parentJob = translationRegistrationJob
            ?.takeIf { it.isActive }
            ?: SupervisorJob(
                lifecycleScope.coroutineContext[Job]
            ).also {
                translationRegistrationJob = it
            }

        lifecycleScope.launch(parentJob) {
            try {
                val respuesta = RetrofitClient.apiService
                    .registrarTraduccion(
                        mapOf("sena" to nombre)
                    )

                respuesta.errorBody()?.close()

                // Ignorar respuestas de sesiones anteriores
                if (
                    !authViewModel.sessionState.esSesionActual(
                        userId,
                        sessionVersion
                    )
                ) return@launch

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
                if (
                    authViewModel.sessionState.esSesionActual(
                        userId,
                        sessionVersion
                    )
                ) {
                    Toast.makeText(
                        this@MainActivity,
                        "Error al guardar la traducción",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // Cerrar sesión y cancelar operaciones pendientes
    private fun cerrarSesion() {
        translationRegistrationJob?.cancel()
        translationRegistrationJob = null

        translationViewModel.cerrarSesionTraductor()
        profileViewModel.clearProfileOperations()

        authViewModel.logout()
    }
}