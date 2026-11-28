package com.tecsup.usuario_kotlin.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// Datos del usuario autenticado
data class SessionUser(
    val id: Int,
    val nombre: String,
    val email: String,
    val rol: String? = null,
    val foto: String? = null
)

// Estado compartido de la sesión
class SessionState {

    var user by mutableStateOf<SessionUser?>(null)
        private set

    // Identificar cada inicio de sesión
    internal var generation = 0L
        private set

    val loginSuccess: Boolean
        get() = user != null

    val loggedUserId: Int?
        get() = user?.id

    val loggedUserName: String
        get() = user?.nombre ?: "Usuario"

    val loggedUserEmail: String
        get() = user?.email ?: ""

    val loggedUserRole: String?
        get() = user?.rol

    val loggedUserPhoto: String?
        get() = user?.foto

    // Iniciar una nueva sesión
    internal fun iniciarSesion(usuario: SessionUser) {
        generation++
        user = usuario
    }

    // Actualizar los datos personales
    internal fun actualizarDatos(
        id: Int,
        nombre: String,
        email: String,
        rol: String?,
        foto: String?
    ) {
        val usuario = user ?: return

        if (usuario.id != id) return

        user = usuario.copy(
            nombre = nombre,
            email = email,
            rol = rol ?: usuario.rol,
            foto = foto
        )
    }

    // Actualizar únicamente la fotografía
    internal fun actualizarFoto(
        id: Int,
        foto: String?
    ) {
        val usuario = user ?: return

        if (usuario.id != id) return

        user = usuario.copy(
            foto = foto?.takeIf { it.isNotBlank() }
        )
    }

    // Verificar que una petición corresponda a la sesión activa
    internal fun esSesionActual(
        id: Int,
        version: Long
    ): Boolean {
        return user?.id == id && generation == version
    }

    // Cerrar sesión y limpiar los datos
    internal fun cerrarSesion() {
        generation++
        user = null
    }
}