package com.tecsup.usuario_kotlin.utils

object ValidationUtils {

    private const val MAX_NAME_LENGTH = 50
    private const val MIN_PASSWORD_LENGTH = 6
    private const val MAX_PASSWORD_LENGTH = 30
    private const val MAX_PHOTO_LENGTH = 300_000

    private val emailRegex =
        Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    // Validar nombre del usuario
    fun validarNombre(nombre: String): String? {

        return when {
            nombre.isBlank() ->
                "El nombre es obligatorio"

            nombre.trim().length > MAX_NAME_LENGTH ->
                "El nombre no puede superar los 50 caracteres"

            else -> null
        }
    }

    // Validar correo electrónico
    fun validarEmail(email: String): String? {

        return when {
            email.isBlank() ->
                "El correo electrónico es obligatorio"

            !emailRegex.matches(email.trim()) ->
                "Ingresa un correo electrónico válido"

            else -> null
        }
    }

    // Validar longitud de contraseña
    fun validarPassword(password: String): String? {

        return when {
            password.isBlank() ->
                "Ingresa tu nueva contraseña"

            password.length < MIN_PASSWORD_LENGTH ->
                "La contraseña debe tener al menos 6 caracteres"

            password.length > MAX_PASSWORD_LENGTH ->
                "La contraseña no puede superar los 30 caracteres"

            else -> null
        }
    }

    // Validar datos personales
    fun validarPerfil(
        nombre: String,
        email: String,
        nombreActual: String,
        emailActual: String
    ): String? {

        validarNombre(nombre)?.let { return it }

        validarEmail(email)?.let { return it }

        if (
            nombre.trim() == nombreActual &&
            email.trim().equals(
                emailActual,
                ignoreCase = true
            )
        ) {
            return "No has realizado ningún cambio"
        }

        return null
    }

    // Validar cambio de contraseña
    fun validarCambioPassword(
        actual: String,
        nueva: String,
        confirmacion: String
    ): String? {

        if (actual.isBlank()) {
            return "Ingresa tu contraseña actual"
        }

        validarPassword(nueva)?.let { return it }

        if (actual == nueva) {
            return "La nueva contraseña debe ser diferente a la actual"
        }

        if (confirmacion.isBlank()) {
            return "Confirma tu nueva contraseña"
        }

        if (nueva != confirmacion) {
            return "Las contraseñas no coinciden"
        }

        return null
    }

    // Validar fotografía de perfil
    fun validarFoto(
        foto: String,
        fotoActual: String?
    ): String? {

        val eliminarFoto = foto.isEmpty()

        if (
            !eliminarFoto &&
            !foto.startsWith("data:image/jpeg;base64,") &&
            !foto.startsWith("data:image/png;base64,")
        ) {
            return "El formato de la imagen no es válido"
        }

        if (foto.length > MAX_PHOTO_LENGTH) {
            return "La imagen es demasiado grande"
        }

        if (eliminarFoto && fotoActual.isNullOrBlank()) {
            return "No tienes una foto de perfil para eliminar"
        }

        if (!eliminarFoto && foto == fotoActual) {
            return "Esta imagen ya es tu foto de perfil"
        }

        return null
    }
}