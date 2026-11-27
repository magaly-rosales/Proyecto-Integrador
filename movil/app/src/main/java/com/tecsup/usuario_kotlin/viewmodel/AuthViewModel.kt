package com.tecsup.usuario_kotlin.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.usuario_kotlin.data.model.ForgotPasswordRequest
import com.tecsup.usuario_kotlin.data.model.LoginRequest
import com.tecsup.usuario_kotlin.data.model.PasswordRequest
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

    // Estados del cambio de contraseña

    var isChangingPassword by mutableStateOf(false)
        private set

    var passwordChangeSuccess by mutableStateOf(false)
        private set

    var passwordErrorMessage by mutableStateOf<String?>(null)
        private set

    private var passwordChangeJob: Job? = null

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

    // Cambio de contraseña

    fun cambiarPassword(
        actual: String,
        nueva: String,
        confirmacion: String
    ) {

        // Evitar solicitudes duplicadas
        if (isChangingPassword) return

        passwordErrorMessage = null
        passwordChangeSuccess = false

        val idUsuario = loggedUserId

        // Verificar que exista una sesión activa
        if (!loginSuccess || idUsuario == null) {
            passwordErrorMessage =
                "Debes iniciar sesión para cambiar tu contraseña"
            return
        }

        // Validar contraseña actual
        if (actual.isBlank()) {
            passwordErrorMessage =
                "Ingresa tu contraseña actual"
            return
        }

        // Validar nueva contraseña
        if (nueva.isBlank()) {
            passwordErrorMessage =
                "Ingresa tu nueva contraseña"
            return
        }

        // Validar longitud mínima
        if (nueva.length < 6) {
            passwordErrorMessage =
                "La contraseña debe tener al menos 6 caracteres"
            return
        }

        // Validar longitud máxima
        if (nueva.length > 30) {
            passwordErrorMessage =
                "La contraseña no puede superar los 30 caracteres"
            return
        }

        // Evitar reutilizar la contraseña actual
        if (actual == nueva) {
            passwordErrorMessage =
                "La nueva contraseña debe ser diferente a la actual"
            return
        }

        // Validar campo de confirmación
        if (confirmacion.isBlank()) {
            passwordErrorMessage =
                "Confirma tu nueva contraseña"
            return
        }

        // Verificar coincidencia de contraseñas
        if (nueva != confirmacion) {
            passwordErrorMessage =
                "Las contraseñas no coinciden"
            return
        }

        isChangingPassword = true

        passwordChangeJob = viewModelScope.launch {

            try {

                // Enviar únicamente las contraseñas necesarias
                val request = PasswordRequest(
                    actual = actual,
                    nueva = nueva
                )

                val response =
                    RetrofitClient.apiService.cambiarPassword(
                        idUsuario = idUsuario,
                        request = request
                    )

                // Verificar que la sesión continúe activa
                if (!loginSuccess || loggedUserId != idUsuario) {
                    return@launch
                }

                if (response.isSuccessful) {

                    passwordChangeSuccess = true

                } else {

                    val mensajePredeterminado = when (response.code()) {

                        400 ->
                            "La nueva contraseña no cumple los requisitos"

                        401 ->
                            "La contraseña actual no es correcta"

                        403 ->
                            "No tienes autorización para realizar esta acción"

                        404 ->
                            "Usuario no encontrado"

                        429 ->
                            "Demasiados intentos. Inténtalo más tarde"

                        500 ->
                            "Error interno del servidor"

                        else ->
                            "No se pudo actualizar la contraseña"
                    }

                    val cuerpoError = response.errorBody()?.string()

                    passwordErrorMessage = obtenerMensajeError(
                        cuerpoError,
                        mensajePredeterminado
                    )
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {

                passwordErrorMessage =
                    "No se pudo conectar con el servidor"

            } finally {

                isChangingPassword = false
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

    // Restablecer estados de edición del perfil

    fun resetProfileUpdateState() {

        if (isUpdatingProfile) return

        profileUpdateSuccess = false
        profileErrorMessage = null
    }

    // Restablecer estados del cambio de contraseña

    fun resetPasswordChangeState() {

        if (isChangingPassword) return

        passwordChangeSuccess = false
        passwordErrorMessage = null
    }

    // Cierre de sesión y limpieza de datos

    fun logout() {

        // Cancelar actualizaciones pendientes
        profileUpdateJob?.cancel()
        profileUpdateJob = null

        passwordChangeJob?.cancel()
        passwordChangeJob = null

        // Limpiar datos del usuario autenticado
        loggedUserId = null
        loggedUserName = "Usuario"
        loggedUserEmail = ""
        loggedUserRole = null
        loggedUserPhoto = null

        loginSuccess = false
        errorMessage = null

        // Limpiar estados de edición del perfil
        isUpdatingProfile = false
        profileUpdateSuccess = false
        profileErrorMessage = null

        // Limpiar estados del cambio de contraseña
        isChangingPassword = false
        passwordChangeSuccess = false
        passwordErrorMessage = null
    }

    fun resetRegisterSuccess() {
        registerSuccess = false
    }

    fun resetForgotPasswordSuccess() {
        forgotPasswordSuccess = false
        forgotPasswordMessage = null
    }
}