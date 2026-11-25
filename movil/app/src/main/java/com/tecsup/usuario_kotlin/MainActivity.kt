
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.tecsup.usuario_kotlin.data.remote.RetrofitClient
import com.tecsup.usuario_kotlin.ui.auth.ForgotPasswordScreen
import com.tecsup.usuario_kotlin.ui.auth.LoginScreen
import com.tecsup.usuario_kotlin.ui.auth.RegisterScreen
import com.tecsup.usuario_kotlin.ui.home.HomeScreen
import com.tecsup.usuario_kotlin.ui.home.ProfileScreen
import com.tecsup.usuario_kotlin.ui.theme.UsuariokotlinTheme
import com.tecsup.usuario_kotlin.ui.translation.TranslationScreen
import com.tecsup.usuario_kotlin.ui.viewmodel.AuthViewModel
import com.tecsup.usuario_kotlin.viewmodel.TranslationViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val translationViewModel: TranslationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            UsuariokotlinTheme {
                var currentScreen by remember { mutableStateOf("login") }

                // Extrae la inicial real del usuario logueado (ej. "Ana" -> "A")
                val userName = authViewModel.loggedUserName
                val userInitial = userName.trim().firstOrNull()?.uppercase() ?: "U"

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (currentScreen) {
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

                        "forgot_password" -> {
                            ForgotPasswordScreen(
                                authViewModel = authViewModel,
                                onBackClick = {
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
                                                RetrofitClient.apiService.registrarTraduccion(
                                                    mapOf("sena" to nombreSena)
                                                )

                                            if (!respuesta.isSuccessful) {
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    "No se pudo guardar la traducción",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }

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
                                    authViewModel.logout()
                                    currentScreen = "login"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
