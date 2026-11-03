package com.tecsup.usuario_kotlin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.usuario_kotlin.data.model.LoginRequest
import com.tecsup.usuario_kotlin.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Estados posibles para el inicio de sesión
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val username: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = RetrofitClient.apiService.login(LoginRequest(email, pass))
                if (response.isSuccessful && response.body() != null) {
                    val username = response.body()!!.username
                    _authState.value = AuthState.Success(username)
                } else {
                    _authState.value = AuthState.Error("Correo o contraseña incorrectos")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Error de conexión: ${e.localizedMessage}")
            }
        }
    }
}