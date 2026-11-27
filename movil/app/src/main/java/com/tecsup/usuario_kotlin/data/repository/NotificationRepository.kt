package com.tecsup.usuario_kotlin.data.repository

import android.content.Context
import com.tecsup.usuario_kotlin.data.model.AppNotification
import com.tecsup.usuario_kotlin.data.model.NotificationType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class NotificationRepository(
    context: Context,
    userId: Int
) {
    companion object {
        private const val PREFS_NAME = "senasapp_notifications"
        const val MAX_NOTIFICATIONS = 50
    }

    init {
        require(userId > 0) {
            "Se requiere un usuario autenticado"
        }
    }

    private val preferences = context.applicationContext
        .getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

    private val storageKey = "notifications_user_$userId"
    private val welcomeKey = "welcome_sent_$userId"

    private val _notifications = MutableStateFlow(
        cargarNotificaciones()
    )

    val notifications = _notifications.asStateFlow()

    // Generar la bienvenida una sola vez por usuario
    fun generarBienvenida() {
        if (preferences.getBoolean(welcomeKey, false)) {
            return
        }

        val notification = AppNotification(
            titulo = "¡Bienvenido a SeñasApp!",
            mensaje = "Explora el catálogo, practica señas " +
                    "y comienza tu aprendizaje.",
            tipo = NotificationType.WELCOME
        )

        guardarNotificaciones(
            lista = listOf(notification) + _notifications.value,
            guardarBienvenida = true
        )
    }

    // Notificar una práctica completada
    fun notificarPracticaExitosa(nombreSena: String) {
        val nombre = nombreSena.trim()

        if (nombre.isBlank()) return

        agregarNotificacion(
            AppNotification(
                titulo = "¡Práctica completada!",
                mensaje = "Has practicado correctamente " +
                        "la seña $nombre.",
                tipo = NotificationType.PRACTICE
            )
        )
    }

    // Agregar una notificación
    private fun agregarNotificacion(
        notification: AppNotification
    ) {
        guardarNotificaciones(
            listOf(notification) + _notifications.value
        )
    }

    // Marcar una notificación como leída
    fun marcarComoLeida(id: String) {
        if (_notifications.value.none {
                it.id == id && !it.leida
            }
        ) return

        val actualizadas = _notifications.value.map {
            if (it.id == id) {
                it.copy(leida = true)
            } else {
                it
            }
        }

        guardarNotificaciones(actualizadas)
    }

    // Marcar todas como leídas
    fun marcarTodasComoLeidas() {
        if (_notifications.value.none { !it.leida }) {
            return
        }

        guardarNotificaciones(
            _notifications.value.map {
                it.copy(leida = true)
            }
        )
    }

    // Eliminar una notificación individual
    fun eliminar(id: String) {
        guardarNotificaciones(
            _notifications.value.filterNot {
                it.id == id
            }
        )
    }

    // Eliminar todas las notificaciones de esta cuenta
    fun eliminarTodas() {
        guardarNotificaciones(emptyList())
    }

    // Persistir las notificaciones
    private fun guardarNotificaciones(
        lista: List<AppNotification>,
        guardarBienvenida: Boolean = false
    ) {
        val limitadas = lista
            .distinctBy { it.id }
            .take(MAX_NOTIFICATIONS)

        val array = JSONArray()

        limitadas.forEach { notification ->
            val objeto = JSONObject().apply {
                put("id", notification.id)
                put("titulo", notification.titulo)
                put("mensaje", notification.mensaje)
                put("tipo", notification.tipo.name)
                put("fechaMillis", notification.fechaMillis)
                put("leida", notification.leida)
            }

            array.put(objeto)
        }

        val editor = preferences.edit()
            .putString(storageKey, array.toString())

        if (guardarBienvenida) {
            editor.putBoolean(welcomeKey, true)
        }

        editor.apply()

        _notifications.value = limitadas.toList()
    }

    // Recuperar las notificaciones guardadas
    private fun cargarNotificaciones(): List<AppNotification> {
        val contenido = preferences.getString(
            storageKey,
            null
        ) ?: return emptyList()

        return try {
            val array = JSONArray(contenido)

            buildList {
                for (i in 0 until array.length()) {
                    val objeto = array.optJSONObject(i)
                        ?: continue

                    val id = objeto.optString("id")
                    val titulo = objeto.optString("titulo")
                    val mensaje = objeto.optString("mensaje")
                    val fecha = objeto.optLong("fechaMillis")

                    val tipo = NotificationType.values()
                        .firstOrNull {
                            it.name == objeto.optString("tipo")
                        } ?: continue

                    if (
                        id.isBlank() ||
                        titulo.isBlank() ||
                        mensaje.isBlank() ||
                        fecha <= 0L
                    ) {
                        continue
                    }

                    add(
                        AppNotification(
                            id = id,
                            titulo = titulo,
                            mensaje = mensaje,
                            tipo = tipo,
                            fechaMillis = fecha,
                            leida = objeto.optBoolean(
                                "leida",
                                false
                            )
                        )
                    )
                }
            }.sortedByDescending {
                it.fechaMillis
            }.distinctBy {
                it.id
            }.take(MAX_NOTIFICATIONS)

        } catch (e: JSONException) {
            emptyList()
        }
    }
}