package com.tecsup.usuario_kotlin.utils

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

object CatalogMediaUtils {

    private const val MEDIA_PORT = 8000
    private const val MEDIA_PATH = "/media/"

    private val localHosts = setOf(
        "localhost",
        "127.0.0.1",
        "10.0.2.2",
        "::1"
    )

    // Obtener la dirección del servidor de archivos multimedia
    fun obtenerBaseMedia(apiUrl: String): HttpUrl? {

        val api = apiUrl.toHttpUrlOrNull()
            ?: return null

        return api.newBuilder()
            .port(MEDIA_PORT)
            .encodedPath(MEDIA_PATH)
            .query(null)
            .fragment(null)
            .build()
    }

    // Convertir la ruta del catálogo en una URL válida
    fun resolverUrlMedia(
        ruta: String?,
        apiUrl: String
    ): String? {

        val valor = ruta?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: return null

        val api = apiUrl.toHttpUrlOrNull()
            ?: return null

        // Si el backend devuelve una URL completa
        val urlAbsoluta = valor.toHttpUrlOrNull()

        if (urlAbsoluta != null) {

            if (
                urlAbsoluta.username.isNotEmpty() ||
                urlAbsoluta.password.isNotEmpty()
            ) {
                return null
            }

            // Adaptar las direcciones locales al dispositivo
            if (urlAbsoluta.host in localHosts) {

                return urlAbsoluta.newBuilder()
                    .host(api.host)
                    .build()
                    .toString()
            }

            return urlAbsoluta.toString()
        }

        // Rechazar protocolos diferentes de HTTP y HTTPS
        if (
            valor.contains("://") ||
            valor.startsWith("//") ||
            valor.startsWith("data:", ignoreCase = true)
        ) {
            return null
        }

        val baseMedia = obtenerBaseMedia(apiUrl)
            ?: return null

        // Evitar duplicar la carpeta media
        val rutaLimpia = valor
            .trimStart('/')
            .removePrefix("media/")

        if (rutaLimpia.isBlank()) {
            return null
        }

        val resultado = baseMedia.resolve("./$rutaLimpia")
            ?: return null

        // Mantener las rutas relativas dentro de /media/
        if (
            resultado.host != baseMedia.host ||
            resultado.port != baseMedia.port ||
            !resultado.encodedPath.startsWith(MEDIA_PATH)
        ) {
            return null
        }

        return resultado.toString()
    }

    // Obtener imágenes por orden de prioridad
    fun obtenerImagenes(
        imagenSena: String?,
        imagenCategoria: String?,
        apiUrl: String
    ): List<String> {

        return listOfNotNull(
            resolverUrlMedia(imagenSena, apiUrl),
            resolverUrlMedia(imagenCategoria, apiUrl)
        ).distinct()
    }
}