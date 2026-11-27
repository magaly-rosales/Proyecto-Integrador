package com.tecsup.usuario_kotlin.data.repository

import android.content.Context
import com.tecsup.usuario_kotlin.data.model.FraseReciente
import com.tecsup.usuario_kotlin.utils.RecognitionUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class RecentPhraseRepository(
    context: Context,
    userId: Int
) {
    companion object {
        const val MAX_PHRASES = 10

        private const val PREFS_NAME = "senasapp_recent_phrases"
    }

    init {
        require(userId > 0) {
            "Se requiere un usuario autenticado"
        }
    }

    // Almacenamiento local por usuario
    private val preferences = context.applicationContext
        .getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

    private val storageKey = "frases_usuario_$userId"

    // Historial observable
    private val _frases = MutableStateFlow(cargar())
    val frases = _frases.asStateFlow()

    // Guardar una nueva frase
    fun guardar(palabras: List<String>) {
        val validas = palabras
            .map { it.trim() }
            .filter { RecognitionUtils.esNombreValido(it) }

        if (validas.isEmpty()) return

        val nuevaFrase = FraseReciente(
            palabras = validas
        )

        // Evitar frases duplicadas
        val anteriores = _frases.value.filterNot {
            it.texto.equals(
                nuevaFrase.texto,
                ignoreCase = true
            )
        }

        val actualizadas = (
                listOf(nuevaFrase) + anteriores
                ).take(MAX_PHRASES)

        persistir(actualizadas)
    }

    // Eliminar una frase específica
    fun eliminar(id: String) {
        val actualizadas = _frases.value.filterNot {
            it.id == id
        }

        persistir(actualizadas)
    }

    // Limpiar el historial del usuario actual
    fun limpiar() {
        preferences.edit()
            .remove(storageKey)
            .apply()

        _frases.value = emptyList()
    }

    // Guardar el historial como JSON
    private fun persistir(lista: List<FraseReciente>) {
        val array = JSONArray()

        lista.forEach { frase ->
            val palabrasJson = JSONArray()

            frase.palabras.forEach { palabra ->
                palabrasJson.put(palabra)
            }

            val objeto = JSONObject()
                .put("id", frase.id)
                .put("palabras", palabrasJson)
                .put("fechaMillis", frase.fechaMillis)

            array.put(objeto)
        }

        preferences.edit()
            .putString(storageKey, array.toString())
            .apply()

        _frases.value = lista.toList()
    }

    // Recuperar las frases almacenadas
    private fun cargar(): List<FraseReciente> {
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

                    val palabrasJson =
                        objeto.optJSONArray("palabras")
                            ?: continue

                    val palabras = buildList {
                        for (j in 0 until palabrasJson.length()) {
                            val palabra = palabrasJson
                                .optString(j)
                                .trim()

                            if (
                                RecognitionUtils.esNombreValido(
                                    palabra
                                )
                            ) {
                                add(palabra)
                            }
                        }
                    }

                    val id = objeto.optString("id")
                    val fecha = objeto.optLong("fechaMillis")

                    if (
                        id.isBlank() ||
                        palabras.isEmpty() ||
                        fecha <= 0L
                    ) {
                        continue
                    }

                    add(
                        FraseReciente(
                            id = id,
                            palabras = palabras,
                            fechaMillis = fecha
                        )
                    )
                }
            }
                .sortedByDescending { it.fechaMillis }
                .distinctBy { it.texto.lowercase() }
                .take(MAX_PHRASES)

        } catch (e: JSONException) {
            emptyList()
        }
    }
}