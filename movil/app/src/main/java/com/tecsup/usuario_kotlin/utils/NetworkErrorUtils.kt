package com.tecsup.usuario_kotlin.utils

import com.google.gson.JsonParseException
import com.google.gson.stream.MalformedJsonException
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.EOFException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

object NetworkErrorUtils {

    // Convertir excepciones en mensajes comprensibles
    fun mensaje(
        error: Throwable,
        predeterminado: String = "No se pudo completar la operación"
    ): String {
        return when (error) {

            is CancellationException -> throw error

            is SocketTimeoutException ->
                "El servidor tardó demasiado en responder. " +
                        "Inténtalo nuevamente."

            is UnknownHostException ->
                "No se pudo localizar el servidor. " +
                        "Comprueba tu conexión."

            is ConnectException ->
                "No se pudo conectar con el servidor. " +
                        "Comprueba que esté disponible."

            is SSLException ->
                "No se pudo establecer una conexión segura."

            is JsonParseException,
            is MalformedJsonException,
            is EOFException ->
                "El servidor devolvió una respuesta no válida."

            is HttpException ->
                mensajeHttp(
                    error.code(),
                    predeterminado
                )

            is RespuestaInvalidaException ->
                "El servidor devolvió una respuesta incompleta."

            is IOException ->
                "Se interrumpió la conexión con el servidor. " +
                        "Inténtalo nuevamente."

            else -> predeterminado
        }
    }

    // Interpretar los códigos HTTP
    fun mensajeHttp(
        code: Int,
        predeterminado: String = "No se pudo completar la operación"
    ): String {
        return when (code) {

            400 -> "Los datos enviados no son válidos."

            401 -> "No se pudo verificar tu autorización."

            403 -> "No tienes permiso para realizar esta acción."

            404 -> "El servicio solicitado no está disponible."

            408 -> "Se agotó el tiempo de espera."

            409 -> "Existe un conflicto con los datos registrados."

            413 -> "Los datos enviados superan el tamaño permitido."

            422 -> "No se pudieron procesar los datos enviados."

            429 -> "Demasiadas solicitudes. Inténtalo más tarde."

            in 500..599 ->
                "El servidor presenta un problema temporal. " +
                        "Inténtalo nuevamente."

            else -> predeterminado
        }
    }
}

// Excepción para respuestas exitosas sin contenido requerido
class RespuestaInvalidaException : Exception()