package com.tecsup.usuario_kotlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.tecsup.usuario_kotlin.ui.auth.LoginScreen
import com.tecsup.usuario_kotlin.ui.theme.UsuariokotlinTheme
import com.tecsup.usuario_kotlin.ui.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {

    // Inicializamos el AuthViewModel usando la librería de Jetpack
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UsuariokotlinTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreen(
                        authViewModel = authViewModel,
                        onLoginSuccess = {
                            // Aquí puedes navegar a la pantalla principal (Home) cuando el login sea exitoso
                        }
                    )
                }
            }
        }
    }
}