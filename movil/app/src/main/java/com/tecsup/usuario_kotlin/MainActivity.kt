package com.tecsup.usuario_kotlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.tecsup.usuario_kotlin.ui.auth.ForgotPasswordScreen
import com.tecsup.usuario_kotlin.ui.auth.LoginScreen
import com.tecsup.usuario_kotlin.ui.auth.RegisterScreen
import com.tecsup.usuario_kotlin.ui.profile.ProfileScreen
import com.tecsup.usuario_kotlin.ui.theme.UsuariokotlinTheme
import com.tecsup.usuario_kotlin.ui.translation.TranslationScreen
import com.tecsup.usuario_kotlin.viewmodel.AuthViewModel
import com.tecsup.usuario_kotlin.viewmodel.TranslationViewModel

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val translationViewModel: TranslationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current

            // Inicializar base de datos SQLite local
            LaunchedEffect(Unit) {
                authViewModel.initDatabase(context)
                translationViewModel.initDatabase(context)
            }

            UsuariokotlinTheme(darkTheme = authViewModel.isDarkMode) {
                var currentScreen by remember { mutableStateOf("login") }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (currentScreen) {
                        "login" -> {
                            authViewModel.clearErrorMessage()
                            LoginScreen(
                                authViewModel = authViewModel,
                                onLoginSuccess = {
                                    currentScreen = "home"
                                },
                                onRegisterClick = {
                                    authViewModel.clearErrorMessage()
                                    currentScreen = "register"
                                },
                                onForgotPasswordClick = {
                                    authViewModel.clearErrorMessage()
                                    currentScreen = "forgot_password"
                                }
                            )
                        }
                        "register" -> {
                            RegisterScreen(
                                authViewModel = authViewModel,
                                onRegisterSuccess = {
                                    authViewModel.clearErrorMessage()
                                    currentScreen = "login"
                                },
                                onLoginClick = {
                                    authViewModel.clearErrorMessage()
                                    currentScreen = "login"
                                }
                            )
                        }
                        "forgot_password" -> {
                            ForgotPasswordScreen(
                                authViewModel = authViewModel,
                                onBackClick = {
                                    authViewModel.clearErrorMessage()
                                    currentScreen = "login"
                                }
                            )
                        }
                        "home" -> {
                            translationViewModel.selectTab(0)
                            TranslationScreen(
                                viewModel = translationViewModel,
                                onProfileClick = {
                                    currentScreen = "profile"
                                }
                            )
                        }
                        "profile" -> {
                            translationViewModel.selectTab(2)
                            ProfileScreen(
                                authViewModel = authViewModel,
                                onLogoutClick = {
                                    currentScreen = "login"
                                },
                                onNavigateToTab = { tabIndex ->
                                    if (tabIndex == 0) {
                                        currentScreen = "home"
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
