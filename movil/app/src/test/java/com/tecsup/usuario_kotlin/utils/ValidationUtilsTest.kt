package com.tecsup.usuario_kotlin.utils

import org.junit.Assert.*
import org.junit.Test

class ValidationUtilsTest {

    @Test
    fun validaNombreDelUsuario() {
        assertNull(
            ValidationUtils.validarNombre("Usuario")
        )

        assertNotNull(
            ValidationUtils.validarNombre("")
        )

        assertNotNull(
            ValidationUtils.validarNombre(
                "A".repeat(51)
            )
        )
    }

    @Test
    fun validaCorreoElectronico() {
        assertNull(
            ValidationUtils.validarEmail(
                "usuario@example.com"
            )
        )

        assertNotNull(
            ValidationUtils.validarEmail(
                "correo_invalido"
            )
        )

        assertNotNull(
            ValidationUtils.validarEmail("")
        )
    }

    @Test
    fun validaLongitudDePassword() {
        assertNull(
            ValidationUtils.validarPassword(
                "Ejemplo123"
            )
        )

        assertNotNull(
            ValidationUtils.validarPassword(
                "12345"
            )
        )

        assertNotNull(
            ValidationUtils.validarPassword(
                "A".repeat(31)
            )
        )
    }

    @Test
    fun validaCambioDePassword() {
        assertNull(
            ValidationUtils.validarCambioPassword(
                actual = "Anterior123",
                nueva = "Nueva456",
                confirmacion = "Nueva456"
            )
        )

        assertNotNull(
            ValidationUtils.validarCambioPassword(
                actual = "Anterior123",
                nueva = "Nueva456",
                confirmacion = "Diferente"
            )
        )

        assertNotNull(
            ValidationUtils.validarCambioPassword(
                actual = "Anterior123",
                nueva = "Anterior123",
                confirmacion = "Anterior123"
            )
        )
    }

    @Test
    fun detectaCambiosEnElPerfil() {
        assertNotNull(
            ValidationUtils.validarPerfil(
                nombre = "Usuario",
                email = "usuario@example.com",
                nombreActual = "Usuario",
                emailActual = "usuario@example.com"
            )
        )

        assertNull(
            ValidationUtils.validarPerfil(
                nombre = "Nuevo nombre",
                email = "usuario@example.com",
                nombreActual = "Usuario",
                emailActual = "usuario@example.com"
            )
        )
    }

    @Test
    fun validaFormatoYTamanoDeFoto() {
        assertNotNull(
            ValidationUtils.validarFoto(
                foto = "formato-invalido",
                fotoActual = null
            )
        )

        val fotoGrande =
            "data:image/jpeg;base64," +
                    "A".repeat(300_001)

        assertNotNull(
            ValidationUtils.validarFoto(
                foto = fotoGrande,
                fotoActual = null
            )
        )

        assertNotNull(
            ValidationUtils.validarFoto(
                foto = "",
                fotoActual = null
            )
        )
    }
}