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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val api = RetrofitClient.apiService

    // Fuente compartida de los datos del usuario
    val sessionState = SessionState()

    // Datos del usuario autenticado
    val loginSuccess get() = sessionState.loginSuccess
    val loggedUserId get() = sessionState.loggedUserId
    val loggedUserName get() = sessionState.loggedUserName
    val loggedUserEmail get() = sessionState.loggedUserEmail
    val loggedUserRole get() = sessionState.loggedUserRole
    val loggedUserPhoto get() = sessionState.loggedUserPhoto

    // Estados generales
    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Estado del registro
    var registerSuccess by mutableStateOf(false)
        private set

    // Estados de recuperación de contraseña
    var forgotPasswordSuccess by mutableStateOf(false)
        private set

    var forgotPasswordMessage by mutableStateOf<String?>(null)
        private set

    private var authJob: Job? = null
    private var authVersion = 0L

    // Inicio de sesión
    fun login(
        usernameOrEmail: String,
        password: String
    ) = ejecutarAuth {

        val response = api.login(
            LoginRequest(
                email = usernameOrEmail,
                password = password
            )
        )

        currentCoroutineContext().ensureActive()

        val user = response.body()

        if (response.isSuccessful && user != null) {

            val id = user.idUsuario

            if (id == null || id <= 0) {
                errorMessage =
                    "El servidor devolvió una sesión no válida"
                return@ejecutarAuth
            }

            sessionState.iniciarSesion(
                SessionUser(
                    id = id,
                    nombre = user.nombre
                        ?: user.email
                        ?: "Usuario",
                    email = user.email
                        ?: if (usernameOrEmail.contains("@")) {
                            usernameOrEmail
                        } else {
                            ""
                        },
                    rol = user.rol,
                    foto = user.foto
                )
            )

        } else {

            errorMessage =
                "Correo o contraseña incorrectos"
        }
    }

    // Registro de usuario
    fun register(
        username: String,
        email: String,
        password: String
    ) = ejecutarAuth {

        registerSuccess = false

        val response = api.register(
            RegisterRequest(
                nombre = username,
                email = email,
                password = password
            )
        )

        currentCoroutineContext().ensureActive()

        if (response.isSuccessful) {

            registerSuccess = true

        } else {

            errorMessage =
                "Error al registrar usuario o correo ya existente"
        }
    }

    // Recuperación de contraseña
    fun forgotPassword(
        email: String
    ) = ejecutarAuth {

        forgotPasswordSuccess = false
        forgotPasswordMessage = null

        val response = api.forgotPassword(
            ForgotPasswordRequest(email)
        )

        currentCoroutineContext().ensureActive()

        if (response.isSuccessful) {

            forgotPasswordSuccess = true
            forgotPasswordMessage =
                "Enlace enviado a tu correo"

        } else {

            errorMessage =
                "Correo no encontrado o error en el servidor"
        }
    }

    // Centralizar la ejecución de peticiones de autenticación
    private fun ejecutarAuth(
        action: suspend () -> Unit
    ) {

        if (isLoading) return

        val version = ++authVersion

        isLoading = true
        errorMessage = null

        authJob = viewModelScope.launch {

            try {

                action()

            } catch (e: CancellationException) {

                throw e

            } catch (e: Exception) {

                if (version == authVersion) {
                    errorMessage =
                        "No se pudo conectar con el servidor"
                }

            } finally {

                if (version == authVersion) {
                    isLoading = false
                }
            }
        }
    }

    // Cierre de sesión
    fun logout() {

        authVersion++

        authJob?.cancel()
        authJob = null

        sessionState.cerrarSesion()

        isLoading = false
        errorMessage = null

        resetRegisterSuccess()
        resetForgotPasswordSuccess()
    }

    // Limpiar estado del registro
    fun resetRegisterSuccess() {
        registerSuccess = false
    }

    // Limpiar estado de recuperación de contraseña
    fun resetForgotPasswordSuccess() {
        forgotPasswordSuccess = false
        forgotPasswordMessage = null
    }
}