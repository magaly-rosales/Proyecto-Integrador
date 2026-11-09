package com.tecsup.usuario_kotlin.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.usuario_kotlin.data.model.ForgotPasswordRequest
import com.tecsup.usuario_kotlin.data.model.LoginRequest
import com.tecsup.usuario_kotlin.data.model.RegisterRequest
import com.tecsup.usuario_kotlin.data.remote.RetrofitClient
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var loginSuccess by mutableStateOf(false)
        private set
    var registerSuccess by mutableStateOf(false)
        private set
    var forgotPasswordSuccess by mutableStateOf(false)
        private set
    var forgotPasswordMessage by mutableStateOf<String?>(null)
        private set

    fun login(usernameOrEmail: String, password: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = RetrofitClient.apiService.login(LoginRequest(usernameOrEmail, password))
                if (response.isSuccessful && response.body() != null) {
                    loginSuccess = true
                } else {
                    errorMessage = "Credenciales incorrectas o error en el servidor"
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = RetrofitClient.apiService.register(RegisterRequest(username, email, password))
                if (response.isSuccessful) {
                    registerSuccess = true
                } else {
                    errorMessage = "Error al registrar usuario o datos ya existentes"
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            forgotPasswordMessage = null
            try {
                val response = RetrofitClient.apiService.forgotPassword(ForgotPasswordRequest(email))
                if (response.isSuccessful) {
                    forgotPasswordSuccess = true
                    forgotPasswordMessage = "Enlace enviado a tu correo"
                } else {
                    errorMessage = "Correo no encontrado o error en el servidor"
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun resetRegisterSuccess() {
        registerSuccess = false
    }

    fun resetForgotPasswordSuccess() {
        forgotPasswordSuccess = false
        forgotPasswordMessage = null
    }
}
