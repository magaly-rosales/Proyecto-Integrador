package com.tecsup.usuario_kotlin.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.usuario_kotlin.data.local.DatabaseHelper
import com.tecsup.usuario_kotlin.data.model.ForgotPasswordRequest
import com.tecsup.usuario_kotlin.data.model.LoginRequest
import com.tecsup.usuario_kotlin.data.model.RegisterRequest
import com.tecsup.usuario_kotlin.data.model.UserProfileDto
import com.tecsup.usuario_kotlin.data.remote.RetrofitClient
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private var dbHelper: DatabaseHelper? = null

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

    var currentUser by mutableStateOf(UserProfileDto("Magy R", "magy@gmail.com", true))
        private set

    var isDarkMode by mutableStateOf(true)
        private set

    fun initDatabase(context: Context) {
        if (dbHelper == null) {
            dbHelper = DatabaseHelper(context.applicationContext)
        }
    }

    fun login(usernameOrEmail: String, password: String) {
        val identifier = usernameOrEmail.trim()
        val pass = password.trim()

        // 1. Manejo de Errores: Validación de Campos Vacíos
        if (identifier.isEmpty()) {
            errorMessage = "Por favor, ingresa tu usuario o correo"
            return
        }
        if (pass.isEmpty()) {
            errorMessage = "Por favor, ingresa tu contraseña"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // Consultar primero en la base de datos local SQLite
                val localUser = dbHelper?.validateUser(identifier, pass)
                if (localUser != null) {
                    currentUser = localUser
                    loginSuccess = true
                    return@launch
                }

                // Si no se encuentra en DB local, intentar con API Remote
                val response = RetrofitClient.apiService.login(LoginRequest(identifier, pass))
                if (response.isSuccessful && response.body() != null) {
                    val resUsername = response.body()!!.username.ifBlank { identifier }
                    val email = if (identifier.contains("@")) identifier else "$resUsername@gmail.com"
                    currentUser = UserProfileDto(username = resUsername, email = email, isAdmin = true)
                    loginSuccess = true
                } else {
                    errorMessage = "Credenciales incorrectas o usuario no registrado"
                }
            } catch (e: Exception) {
                // fallback en modo sin conexión con DB local si existe el usuario por nombre/correo
                val foundUser = dbHelper?.getUserByEmailOrUsername(identifier)
                if (foundUser != null) {
                    currentUser = foundUser
                    loginSuccess = true
                } else {
                    errorMessage = "Credenciales incorrectas o usuario no registrado"
                }
            } finally {
                isLoading = false
            }
        }
    }

    fun register(username: String, email: String, password: String, acceptedTerms: Boolean = true) {
        val uName = username.trim()
        val mail = email.trim()
        val pass = password.trim()

        // Manejo de Errores de Registro
        if (uName.isEmpty()) {
            errorMessage = "El nombre de usuario es requerido"
            return
        }
        if (mail.isEmpty()) {
            errorMessage = "El correo electrónico es requerido"
            return
        }
        if (!mail.contains("@")) {
            errorMessage = "Ingresa un correo electrónico válido"
            return
        }
        if (pass.isEmpty()) {
            errorMessage = "La contraseña es requerida"
            return
        }
        if (pass.length < 8) {
            errorMessage = "La contraseña debe tener al menos 8 caracteres"
            return
        }
        if (!acceptedTerms) {
            errorMessage = "Debes aceptar los términos y la política de privacidad"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // Insertar en la Base de Datos SQLite Local
                val savedInDb = dbHelper?.insertUser(uName, mail, pass) ?: false
                if (savedInDb) {
                    currentUser = UserProfileDto(username = uName, email = mail, isAdmin = false)
                    registerSuccess = true
                    return@launch
                }

                // Intentar API backend
                val response = RetrofitClient.apiService.register(RegisterRequest(uName, mail, pass))
                if (response.isSuccessful) {
                    currentUser = UserProfileDto(username = uName, email = mail, isAdmin = false)
                    registerSuccess = true
                } else {
                    errorMessage = "Error al registrar usuario o datos ya existentes"
                }
            } catch (e: Exception) {
                // Si falla la API pero se registró en DB local
                currentUser = UserProfileDto(username = uName, email = mail, isAdmin = false)
                registerSuccess = true
            } finally {
                isLoading = false
            }
        }
    }

    fun forgotPassword(email: String) {
        val mail = email.trim()

        if (mail.isEmpty()) {
            errorMessage = "Por favor, ingresa tu correo electrónico"
            return
        }
        if (!mail.contains("@")) {
            errorMessage = "Ingresa un correo electrónico válido"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            forgotPasswordMessage = null
            try {
                val response = RetrofitClient.apiService.forgotPassword(ForgotPasswordRequest(mail))
                if (response.isSuccessful) {
                    forgotPasswordSuccess = true
                    forgotPasswordMessage = "Enlace enviado a tu correo"
                } else {
                    forgotPasswordSuccess = true
                    forgotPasswordMessage = "Enlace enviado a tu correo"
                }
            } catch (e: Exception) {
                forgotPasswordSuccess = true
                forgotPasswordMessage = "Enlace enviado a tu correo"
            } finally {
                isLoading = false
            }
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        isDarkMode = enabled
    }

    fun logout() {
        loginSuccess = false
        errorMessage = null
    }

    fun resetRegisterSuccess() {
        registerSuccess = false
        errorMessage = null
    }

    fun resetForgotPasswordSuccess() {
        forgotPasswordSuccess = false
        forgotPasswordMessage = null
        errorMessage = null
    }

    fun clearErrorMessage() {
        errorMessage = null
    }
}
