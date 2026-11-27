package com.tecsup.usuario_kotlin.utils

import org.junit.Assert.*
import org.junit.Test

class PhraseBuilderTest {

    @Test
    fun requiereTresConfirmacionesEstaticas() {
        val builder = PhraseBuilder()

        assertNull(
            builder.registrarEstatica("Hola")
        )

        assertNull(
            builder.registrarEstatica("Hola")
        )

        assertEquals(
            "Hola",
            builder.registrarEstatica("Hola")
        )

        assertEquals(
            listOf("Hola"),
            builder.words.value
        )
    }

    @Test
    fun evitaSenasDuplicadasConsecutivas() {
        val builder = PhraseBuilder()

        repeat(6) {
            builder.registrarEstatica("Hola")
        }

        assertEquals(
            listOf("Hola"),
            builder.words.value
        )
    }

    @Test
    fun permiteRepetirDespuesDeRetirarManos() {
        val builder = PhraseBuilder()

        repeat(3) {
            builder.registrarEstatica("Hola")
        }

        builder.registrarAusencia(0L)
        builder.registrarAusencia(1000L)

        builder.registrarPresencia()

        repeat(3) {
            builder.registrarEstatica("Hola")
        }

        assertEquals(
            listOf("Hola", "Hola"),
            builder.words.value
        )
    }

    @Test
    fun agregaSenaDinamicaConfirmada() {
        val builder = PhraseBuilder()

        builder.registrarDinamica("Buenos días")

        assertEquals(
            "Buenos días",
            builder.obtenerFrase()
        )
    }

    @Test
    fun rechazaResultadosInvalidos() {
        val builder = PhraseBuilder()

        builder.registrarEstatica(null)
        builder.registrarEstatica("desconocida")
        builder.registrarDinamica("dudosa")
        builder.registrarDinamica("sin_muestras")

        assertTrue(
            builder.words.value.isEmpty()
        )
    }

    @Test
    fun permiteEditarLaFrase() {
        val builder = PhraseBuilder()

        builder.registrarDinamica("Hola")
        builder.registrarDinamica("Gracias")

        assertEquals(
            "Hola Gracias",
            builder.obtenerFrase()
        )

        builder.borrarUltima()

        assertEquals(
            "Hola",
            builder.obtenerFrase()
        )

        builder.limpiarFrase()

        assertEquals(
            "",
            builder.obtenerFrase()
        )
    }

    @Test
    fun recuperaHistorialYReiniciaSesion() {
        val builder = PhraseBuilder()

        builder.cargarFrase(
            listOf(
                " Hola ",
                " ",
                "Gracias",
                "desconocida"
            )
        )

        assertEquals(
            listOf("Hola", "Gracias"),
            builder.words.value
        )

        builder.reiniciarSesion()

        assertTrue(
            builder.words.value.isEmpty()
        )
    }
}