package com.tecsup.usuario_kotlin.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.tecsup.usuario_kotlin.viewmodel.AuthState
import com.tecsup.usuario_kotlin.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val authState by authViewModel.authState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Bienvenido de nuevo",
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { authViewModel.login(email, password) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Iniciar sesión")
            }

            // Manejo de estados reactivos
            when (authState) {
                is AuthState.Loading -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                }
                is AuthState.Success -> {
                    val username = (authState as AuthState.Success).username
                    LaunchedEffect(username) {
                        onLoginSuccess(username)
                    }
                }
                is AuthState.Error -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    val errorMsg = (authState as AuthState.Error).message
                    Text(text = errorMsg, color = MaterialTheme.colorScheme.error)
                }
                else -> {}
            }
        }
    }
}