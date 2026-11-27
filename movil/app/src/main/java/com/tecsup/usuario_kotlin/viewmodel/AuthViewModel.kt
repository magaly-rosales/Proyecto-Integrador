package com.tecsup.usuario_kotlin.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.usuario_kotlin.data.model.ForgotPasswordRequest
import com.tecsup.usuario_kotlin.data.model.LoginRequest
import com.tecsup.usuario_kotlin.data.model.PerfilDatosRequest
import com.tecsup.usuario_kotlin.data.model.RegisterRequest
import com.tecsup.usuario_kotlin.data.remote.RetrofitClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject

class AuthViewModel : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var loginSuccess by mutableStateOf(false)
        private set

    // Datos del usuario autenticado

    var loggedUserId by mutableStateOf<Int?>(null)
        private set

    var loggedUserName by mutableStateOf("Usuario")
        private set

    var loggedUserEmail by mutableStateOf("")
        private set

    var loggedUserRole by mutableStateOf<String?>(null)
        private set

    var loggedUserPhoto by mutableStateOf<String?>(null)
        private set

    // Estados del registro

    var registerSuccess by mutableStateOf(false)
        private set

    // Estados de recuperación de contraseña

    var forgotPasswordSuccess by mutableStateOf(false)
        private set

    var forgotPasswordMessage by mutableStateOf<String?>(null)
        private set

    // Estados de actualización del perfil

    var isUpdatingProfile by mutableStateOf(false)
        private set

    var profileUpdateSuccess by mutableStateOf(false)
        private set

    var profileErrorMessage by mutableStateOf<String?>(null)
        private set

    private var profileUpdateJob: Job? = null

    // Inicio de sesión

    fun login(usernameOrEmail: String, password: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                val response = RetrofitClient.apiService.login(
                    LoginRequest(usernameOrEmail, password)
                )

                val user = response.body()

                if (response.isSuccessful && user != null) {

                    // Guardar los datos del usuario autenticado
                    loggedUserId = user.idUsuario
                    loggedUserName = user.nombre ?: user.email ?: "Usuario"
                    loggedUserEmail = user.email
                        ?: if (usernameOrEmail.contains("@")) {
                            usernameOrEmail
                        } else {
                            ""
                        }

                    loggedUserRole = user.rol
                    loggedUserPhoto = user.foto

                    loginSuccess = true

                } else {
                    errorMessage = "Correo o contraseña incorrectos"
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.localizedMessage}"

            } finally {
                isLoading = false
            }
        }
    }

    // Registro de usuario

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                val response = RetrofitClient.apiService.register(
                    RegisterRequest(username, email, password)
                )

                if (response.isSuccessful) {
                    registerSuccess = true
                } else {
                    errorMessage =
                        "Error al registrar usuario o correo ya existente"
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.localizedMessage}"

            } finally {
                isLoading = false
            }
        }
    }

    // Recuperación de contraseña

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            forgotPasswordMessage = null

            try {
                val response = RetrofitClient.apiService.forgotPassword(
                    ForgotPasswordRequest(email)
                )

                if (response.isSuccessful) {
                    forgotPasswordSuccess = true
                    forgotPasswordMessage = "Enlace enviado a tu correo"
                } else {
                    errorMessage =
                        "Correo no encontrado o error en el servidor"
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.localizedMessage}"

            } finally {
                isLoading = false
            }
        }
    }

    // Actualización de datos personales

    fun actualizarPerfil(nombre: String, email: String) {

        // Evitar solicitudes duplicadas
        if (isUpdatingProfile) return

        profileErrorMessage = null
        profileUpdateSuccess = false

        val idUsuario = loggedUserId
        val nombreLimpio = nombre.trim()
        val emailLimpio = email.trim()

        // Comprobar que exista una sesión activa
        if (!loginSuccess || idUsuario == null) {
            profileErrorMessage =
                "Debes iniciar sesión para actualizar tu perfil"
            return
        }

        // Validar nombre obligatorio
        if (nombreLimpio.isBlank()) {
            profileErrorMessage = "El nombre es obligatorio"
            return
        }

        // Validar longitud del nombre
        if (nombreLimpio.length > 50) {
            profileErrorMessage =
                "El nombre no puede superar los 50 caracteres"
            return
        }

        // Validar correo obligatorio
        if (emailLimpio.isBlank()) {
            profileErrorMessage = "El correo electrónico es obligatorio"
            return
        }

        // Validar formato del correo
        val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

        if (!emailRegex.matches(emailLimpio)) {
            profileErrorMessage = "Ingresa un correo electrónico válido"
            return
        }

        // Verificar si existen cambios
        if (
            nombreLimpio == loggedUserName &&
            emailLimpio.equals(loggedUserEmail, ignoreCase = true)
        ) {
            profileErrorMessage = "No has realizado ningún cambio"
            return
        }

        isUpdatingProfile = true

        profileUpdateJob = viewModelScope.launch {

            try {
                val request = PerfilDatosRequest(
                    nombre = nombreLimpio,
                    email = emailLimpio
                )

                val response =
                    RetrofitClient.apiService.actualizarDatosPerfil(
                        idUsuario = idUsuario,
                        request = request
                    )

                val perfil = response.body()

                // Verificar que la sesión siga correspondiendo al usuario
                if (!loginSuccess || loggedUserId != idUsuario) {
                    return@launch
                }

                if (response.isSuccessful) {

                    // Comprobar que la respuesta sea válida
                    if (
                        perfil == null ||
                        perfil.idUsuario != idUsuario ||
                        perfil.nombre.isNullOrBlank() ||
                        perfil.email.isNullOrBlank()
                    ) {
                        profileErrorMessage =
                            "El servidor devolvió datos de perfil incompletos"
                        return@launch
                    }

                    // Actualizar los datos de la sesión actual
                    loggedUserName = perfil.nombre
                    loggedUserEmail = perfil.email
                    loggedUserRole = perfil.rol ?: loggedUserRole
                    loggedUserPhoto = perfil.foto

                    profileUpdateSuccess = true

                } else {

                    // Mensaje alternativo según el código HTTP
                    val mensajePredeterminado = when (response.code()) {

                        400 -> "Los datos ingresados no son válidos"

                        401, 403 ->
                            "No tienes autorización para actualizar el perfil"

                        404 -> "Usuario no encontrado"

                        409 -> "El correo electrónico ya está registrado"

                        500 -> "Error interno del servidor"

                        else ->
                            "No se pudo actualizar el perfil"
                    }

                    // Recuperar el mensaje enviado por Spring Boot
                    val cuerpoError = response.errorBody()?.string()

                    profileErrorMessage = obtenerMensajeError(
                        cuerpoError,
                        mensajePredeterminado
                    )
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                profileErrorMessage =
                    "No se pudo conectar con el servidor"

            } finally {
                isUpdatingProfile = false
            }
        }
    }

    // Obtener mensajes de error del backend

    private fun obtenerMensajeError(
        cuerpoError: String?,
        mensajePredeterminado: String
    ): String {

        if (cuerpoError.isNullOrBlank()) {
            return mensajePredeterminado
        }

        return try {

            JSONObject(cuerpoError)
                .optString("error")
                .ifBlank { mensajePredeterminado }

        } catch (e: Exception) {
            mensajePredeterminado
        }
    }

    // Restablecer estados de la edición del perfil

    fun resetProfileUpdateState() {

        if (isUpdatingProfile) return

        profileUpdateSuccess = false
        profileErrorMessage = null
    }

    // Cierre de sesión y limpieza de datos

    fun logout() {

        // Cancelar una actualización de perfil pendiente
        profileUpdateJob?.cancel()
        profileUpdateJob = null

        loggedUserId = null
        loggedUserName = "Usuario"
        loggedUserEmail = ""
        loggedUserRole = null
        loggedUserPhoto = null

        loginSuccess = false
        errorMessage = null

        isUpdatingProfile = false
        profileUpdateSuccess = false
        profileErrorMessage = null
    }

    fun resetRegisterSuccess() {
        registerSuccess = false
    }

    fun resetForgotPasswordSuccess() {
        forgotPasswordSuccess = false
        forgotPasswordMessage = null
    }
}