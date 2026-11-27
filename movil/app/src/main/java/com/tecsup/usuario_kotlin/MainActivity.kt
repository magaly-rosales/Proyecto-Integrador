package com.tecsup.usuario_kotlin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.tecsup.usuario_kotlin.data.remote.RetrofitClient
import com.tecsup.usuario_kotlin.ui.auth.ForgotPasswordScreen
import com.tecsup.usuario_kotlin.ui.auth.LoginScreen
import com.tecsup.usuario_kotlin.ui.auth.RegisterScreen
import com.tecsup.usuario_kotlin.ui.home.EditProfileScreen
import com.tecsup.usuario_kotlin.ui.home.HomeScreen
import com.tecsup.usuario_kotlin.ui.home.ProfileScreen
import com.tecsup.usuario_kotlin.ui.theme.UsuariokotlinTheme
import com.tecsup.usuario_kotlin.ui.translation.TranslationScreen
import com.tecsup.usuario_kotlin.ui.viewmodel.AuthViewModel
import com.tecsup.usuario_kotlin.viewmodel.TranslationViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val translationViewModel: TranslationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            UsuariokotlinTheme {

                // Pantallas que requieren un inicio de sesión válido
                val protectedScreens = setOf(
                    "home",
                    "translation",
                    "perfil",
                    "edit_profile"
                )

                // Restaurar la navegación sin permitir el acceso
                // a pantallas protegidas cuando no existe una sesión
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

                // Conservar la pantalla ante cambios de configuración
                var currentScreen by rememberSaveable(
                    stateSaver = screenSaver
                ) {
                    mutableStateOf("login")
                }

                // Validar el acceso a las pantallas protegidas
                val activeScreen = if (
                    currentScreen in protectedScreens &&
                    !authViewModel.loginSuccess
                ) {
                    "login"
                } else {
                    currentScreen
                }

                // Obtener los datos del usuario autenticado
                val userName = authViewModel.loggedUserName

                val userInitial = userName
                    .trim()
                    .firstOrNull()
                    ?.uppercase() ?: "U"

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
                                    currentScreen = "register"
                                },
                                onForgotPasswordClick = {
                                    currentScreen = "forgot_password"
                                }
                            )
                        }

                        // Registro de usuarios
                        "register" -> {
                            RegisterScreen(
                                authViewModel = authViewModel,
                                onRegisterSuccess = {
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
                                    currentScreen = "login"
                                }
                            )
                        }

                        // Catálogo de señas
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

                        // Traductor de señas
                        "translation" -> {
                            TranslationScreen(
                                viewModel = translationViewModel,
                                userInitial = userInitial,
                                onNavigateToSenas = {
                                    currentScreen = "home"
                                },
                                onNavigateToPerfil = {
                                    currentScreen = "perfil"
                                },
                                onSpeakText = { nombreSena ->

                                    lifecycleScope.launch {
                                        try {

                                            val respuesta =
                                                RetrofitClient.apiService
                                                    .registrarTraduccion(
                                                        mapOf(
                                                            "sena" to nombreSena
                                                        )
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
                            )
                        }

                        // Perfil del usuario
                        "perfil" -> {
                            ProfileScreen(
                                userName = authViewModel.loggedUserName,
                                userEmail = authViewModel.loggedUserEmail,
                                onNavigateToTraducir = {
                                    currentScreen = "translation"
                                },
                                onNavigateToSenas = {
                                    currentScreen = "home"
                                },
                                onLogout = {

                                    // Limpiar los datos de la sesión
                                    authViewModel.logout()

                                    // Regresar al inicio de sesión
                                    currentScreen = "login"
                                },
                                onNavigateToEditProfile = {

                                    // Limpiar estados de ediciones anteriores
                                    authViewModel.resetProfileUpdateState()

                                    // Abrir la pantalla de edición
                                    currentScreen = "edit_profile"
                                }
                            )
                        }

                        // Edición de datos personales
                        "edit_profile" -> {
                            EditProfileScreen(
                                authViewModel = authViewModel,
                                onBackClick = {
                                    currentScreen = "perfil"
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
                                    currentScreen = "register"
                                },
                                onForgotPasswordClick = {
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