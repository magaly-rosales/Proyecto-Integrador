package com.tecsup.usuario_kotlin.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tecsup.usuario_kotlin.data.model.FotoRequest
import com.tecsup.usuario_kotlin.data.model.PasswordRequest
import com.tecsup.usuario_kotlin.data.model.PerfilDatosRequest
import com.tecsup.usuario_kotlin.data.remote.RetrofitClient
import com.tecsup.usuario_kotlin.utils.ValidationUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class ProfileViewModel(
    private val sessionState: SessionState
) : ViewModel() {

    private val api = RetrofitClient.apiService

    // Datos compartidos de la sesión
    val loginSuccess get() = sessionState.loginSuccess
    val loggedUserId get() = sessionState.loggedUserId
    val loggedUserName get() = sessionState.loggedUserName
    val loggedUserEmail get() = sessionState.loggedUserEmail
    val loggedUserRole get() = sessionState.loggedUserRole
    val loggedUserPhoto get() = sessionState.loggedUserPhoto

    // Operaciones disponibles
    private enum class Operation {
        PROFILE,
        PASSWORD,
        PHOTO
    }

    private var activeOperation by
    mutableStateOf<Operation?>(null)

    private var operationJob: Job? = null
    private var operationVersion = 0L

    val isBusy: Boolean
        get() = activeOperation != null

    val isUpdatingProfile: Boolean
        get() = activeOperation == Operation.PROFILE

    val isChangingPassword: Boolean
        get() = activeOperation == Operation.PASSWORD

    val isUpdatingPhoto: Boolean
        get() = activeOperation == Operation.PHOTO

    // Estados de edición del perfil
    var profileUpdateSuccess by mutableStateOf(false)
        private set

    var profileErrorMessage by mutableStateOf<String?>(null)
        private set

    // Estados del cambio de contraseña
    var passwordChangeSuccess by mutableStateOf(false)
        private set

    var passwordErrorMessage by mutableStateOf<String?>(null)
        private set

    // Estados de actualización de fotografía
    var photoUpdateSuccess by mutableStateOf(false)
        private set

    var photoErrorMessage by mutableStateOf<String?>(null)
        private set

    var photoSuccessMessage by mutableStateOf<String?>(null)
        private set

    // Actualizar los datos personales
    fun actualizarPerfil(nombre: String, email: String) {

        if (isBusy) return

        resetProfileUpdateState()

        val id = loggedUserId

        if (id == null || !loginSuccess) {
            profileErrorMessage =
                "Debes iniciar sesión para actualizar tu perfil"
            return
        }

        ValidationUtils.validarPerfil(
            nombre = nombre,
            email = email,
            nombreActual = loggedUserName,
            emailActual = loggedUserEmail
        )?.let {
            profileErrorMessage = it
            return
        }

        ejecutarOperacion(
            operation = Operation.PROFILE,
            id = id,
            request = {
                api.actualizarDatosPerfil(
                    idUsuario = id,
                    request = PerfilDatosRequest(
                        nombre = nombre.trim(),
                        email = email.trim()
                    )
                )
            },
            onSuccess = { perfil ->

                if (
                    perfil == null ||
                    perfil.idUsuario != id ||
                    perfil.nombre.isNullOrBlank() ||
                    perfil.email.isNullOrBlank()
                ) {
                    profileErrorMessage =
                        "El servidor devolvió datos de perfil incompletos"

                } else {

                    sessionState.actualizarDatos(
                        id = id,
                        nombre = perfil.nombre,
                        email = perfil.email,
                        rol = perfil.rol,
                        foto = perfil.foto
                    )

                    profileUpdateSuccess = true
                }
            },
            onError = {
                profileErrorMessage = it
            }
        )
    }

    // Cambiar la contraseña
    fun cambiarPassword(
        actual: String,
        nueva: String,
        confirmacion: String
    ) {

        if (isBusy) return

        resetPasswordChangeState()

        val id = loggedUserId

        if (id == null || !loginSuccess) {
            passwordErrorMessage =
                "Debes iniciar sesión para cambiar tu contraseña"
            return
        }

        ValidationUtils.validarCambioPassword(
            actual = actual,
            nueva = nueva,
            confirmacion = confirmacion
        )?.let {
            passwordErrorMessage = it
            return
        }

        ejecutarOperacion(
            operation = Operation.PASSWORD,
            id = id,
            request = {
                api.cambiarPassword(
                    idUsuario = id,
                    request = PasswordRequest(
                        actual = actual,
                        nueva = nueva
                    )
                )
            },
            onSuccess = {
                passwordChangeSuccess = true
            },
            onError = {
                passwordErrorMessage = it
            }
        )
    }

    // Actualizar la fotografía
    fun actualizarFotoPerfil(foto: String) {

        if (isBusy) return

        resetPhotoUpdateState()

        val id = loggedUserId

        if (id == null || !loginSuccess) {
            photoErrorMessage =
                "Debes iniciar sesión para actualizar tu foto"
            return
        }

        ValidationUtils.validarFoto(
            foto = foto,
            fotoActual = loggedUserPhoto
        )?.let {
            photoErrorMessage = it
            return
        }

        val eliminarFoto = foto.isEmpty()

        ejecutarOperacion(
            operation = Operation.PHOTO,
            id = id,
            request = {
                api.actualizarFotoPerfil(
                    idUsuario = id,
                    request = FotoRequest(foto)
                )
            },
            onSuccess = { perfil ->

                when {

                    perfil == null || perfil.idUsuario != id -> {
                        photoErrorMessage =
                            "El servidor devolvió una respuesta no válida"
                    }

                    eliminarFoto &&
                            !perfil.foto.isNullOrBlank() -> {
                        photoErrorMessage =
                            "No se pudo eliminar la foto de perfil"
                    }

                    !eliminarFoto && perfil.foto != foto -> {
                        photoErrorMessage =
                            "El servidor no confirmó la nueva fotografía"
                    }

                    else -> {

                        sessionState.actualizarFoto(
                            id = id,
                            foto = perfil.foto
                        )

                        photoSuccessMessage = if (eliminarFoto) {
                            "Foto de perfil eliminada correctamente"
                        } else {
                            "Foto de perfil actualizada correctamente"
                        }

                        photoUpdateSuccess = true
                    }
                }
            },
            onError = {
                photoErrorMessage = it
            }
        )
    }

    // Eliminar fotografía
    fun eliminarFotoPerfil() {
        actualizarFotoPerfil("")
    }

    // Mostrar errores de procesamiento de imágenes
    fun mostrarErrorFoto(mensaje: String) {

        if (isBusy) return

        photoUpdateSuccess = false
        photoSuccessMessage = null
        photoErrorMessage = mensaje
    }

    // Ejecutar cualquier operación de perfil
    private fun <T> ejecutarOperacion(
        operation: Operation,
        id: Int,
        request: suspend () -> Response<T>,
        onSuccess: (T?) -> Unit,
        onError: (String) -> Unit
    ) {

        if (isBusy) return

        val sessionVersion = sessionState.generation
        val version = ++operationVersion

        activeOperation = operation

        operationJob = viewModelScope.launch {

            fun operacionVigente(): Boolean {
                return version == operationVersion &&
                        sessionState.esSesionActual(
                            id,
                            sessionVersion
                        )
            }

            try {

                val response = request()

                if (!operacionVigente()) return@launch

                if (response.isSuccessful) {

                    onSuccess(response.body())

                } else {

                    val mensaje = mensajeHttp(
                        operation,
                        response.code()
                    )

                    val cuerpoError = response.errorBody()
                        ?.use { it.string() }

                    onError(
                        obtenerMensajeError(
                            cuerpoError,
                            mensaje
                        )
                    )
                }

            } catch (e: CancellationException) {

                throw e

            } catch (e: Exception) {

                if (operacionVigente()) {
                    onError(
                        "No se pudo conectar con el servidor"
                    )
                }

            } finally {

                if (version == operationVersion) {
                    activeOperation = null
                }
            }
        }
    }

    // Mensajes según la respuesta del servidor
    private fun mensajeHttp(
        operation: Operation,
        code: Int
    ): String {

        return when (code) {

            400 -> when (operation) {
                Operation.PROFILE ->
                    "Los datos ingresados no son válidos"

                Operation.PASSWORD ->
                    "La nueva contraseña no cumple los requisitos"

                Operation.PHOTO ->
                    "La imagen no es válida o es demasiado grande"
            }

            401 -> if (operation == Operation.PASSWORD) {
                "La contraseña actual no es correcta"
            } else {
                "No tienes autorización para realizar esta acción"
            }

            403 -> "No tienes autorización para realizar esta acción"

            404 -> "Usuario no encontrado"

            409 -> if (operation == Operation.PROFILE) {
                "El correo electrónico ya está registrado"
            } else {
                "No se pudo completar la actualización"
            }

            413 -> "La imagen supera el tamaño permitido"

            429 -> "Demasiados intentos. Inténtalo más tarde"

            500 -> "Error interno del servidor"

            else -> when (operation) {
                Operation.PROFILE ->
                    "No se pudo actualizar el perfil"

                Operation.PASSWORD ->
                    "No se pudo actualizar la contraseña"

                Operation.PHOTO ->
                    "No se pudo actualizar la foto de perfil"
            }
        }
    }

    // Recuperar el mensaje enviado por Spring Boot
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

    // Limpiar estados de edición del perfil
    fun resetProfileUpdateState() {

        if (isUpdatingProfile) return

        profileUpdateSuccess = false
        profileErrorMessage = null
    }

    // Limpiar estados del cambio de contraseña
    fun resetPasswordChangeState() {

        if (isChangingPassword) return

        passwordChangeSuccess = false
        passwordErrorMessage = null
    }

    // Limpiar estados de fotografía
    fun resetPhotoUpdateState() {

        if (isUpdatingPhoto) return

        photoUpdateSuccess = false
        photoErrorMessage = null
        photoSuccessMessage = null
    }

    // Cancelar peticiones y limpiar estados al cerrar sesión
    fun clearProfileOperations() {

        operationVersion++

        operationJob?.cancel()
        operationJob = null

        activeOperation = null

        resetProfileUpdateState()
        resetPasswordChangeState()
        resetPhotoUpdateState()
    }

    // Crear el ViewModel con la sesión compartida
    companion object {

        fun factory(
            sessionState: SessionState
        ): ViewModelProvider.Factory {

            return object : ViewModelProvider.Factory {

                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>
                ): T {

                    if (
                        modelClass.isAssignableFrom(
                            ProfileViewModel::class.java
                        )
                    ) {
                        return ProfileViewModel(
                            sessionState
                        ) as T
                    }

                    throw IllegalArgumentException(
                        "ViewModel desconocido"
                    )
                }
            }
        }
    }
}