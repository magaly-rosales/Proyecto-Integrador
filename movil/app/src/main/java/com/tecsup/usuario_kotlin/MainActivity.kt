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
import com.tecsup.usuario_kotlin.data.remote.RetrofitClient
import com.tecsup.usuario_kotlin.data.repository.TranslationPreferencesRepository
import com.tecsup.usuario_kotlin.ui.auth.ForgotPasswordScreen
import com.tecsup.usuario_kotlin.ui.auth.LoginScreen
import com.tecsup.usuario_kotlin.ui.auth.RegisterScreen
import com.tecsup.usuario_kotlin.ui.home.ChangePasswordScreen
import com.tecsup.usuario_kotlin.ui.home.EditProfileScreen
import com.tecsup.usuario_kotlin.ui.home.HomeScreen
import com.tecsup.usuario_kotlin.ui.home.ProfileScreen
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
        "change_password"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            // Preferencias del usuario autenticado
            val userId = authViewModel.loggedUserId

            val preferencesRepository = remember(userId) {
                userId?.takeIf { it > 0 }?.let {
                    TranslationPreferencesRepository(
                        applicationContext, it
                    )
                }
            }

            val darkModeEnabled = preferencesRepository
                ?.darkMode
                ?.collectAsStateWithLifecycle()
                ?.value ?: true

            UsuariokotlinTheme(
                darkTheme = darkModeEnabled
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

                val activeScreen = if (
                    currentScreen in protectedScreens &&
                    !authViewModel.loginSuccess
                ) {
                    "login"
                } else {
                    currentScreen
                }

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

                        "forgot_password" -> {
                            ForgotPasswordScreen(
                                authViewModel = authViewModel,
                                onBackClick = {
                                    authViewModel.resetForgotPasswordSuccess()
                                    currentScreen = "login"
                                }
                            )
                        }

                        "home" -> {
                            HomeScreen(
                                username = userName,
                                onNavigateToTranslator = {
                                    currentScreen = "translation"
                                },
                                onNavigateToPerfil = {
                                    currentScreen = "perfil"
                                },
                                viewModel = translationViewModel
                            )
                        }

                        "translation" -> {
                            if (userId != null && userId > 0) {
                                key(userId) {
                                    TranslationScreen(
                                        viewModel = translationViewModel,
                                        userId = userId,
                                        userInitial = userInitial,
                                        onNavigateToSenas = {
                                            currentScreen = "home"
                                        },
                                        onNavigateToPerfil = {
                                            currentScreen = "perfil"
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

                        "perfil" -> {
                            ProfileScreen(
                                userName = userName,
                                userEmail = userEmail,
                                userPhoto = userPhoto,

                                darkModeEnabled = darkModeEnabled,
                                onDarkModeChange = { enabled ->
                                    preferencesRepository
                                        ?.cambiarModoOscuro(enabled)
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

                        "change_password" -> {
                            ChangePasswordScreen(
                                profileViewModel = profileViewModel,
                                onBackClick = {
                                    currentScreen = "edit_profile"
                                }
                            )
                        }

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

    private fun cerrarSesion() {
        translationViewModel.cerrarSesionTraductor()
        profileViewModel.clearProfileOperations()
        authViewModel.logout()
    }
}