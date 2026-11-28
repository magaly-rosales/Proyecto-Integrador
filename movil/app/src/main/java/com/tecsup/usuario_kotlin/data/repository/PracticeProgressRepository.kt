package com.tecsup.usuario_kotlin.data.repository

import android.content.Context
import com.tecsup.usuario_kotlin.data.model.PracticeProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class PracticeProgressRepository(
    context: Context,
    userId: Int
) {
    companion object {
        private const val PREFS_NAME = "senasapp_practice_progress"
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

    private val storageKey = "progress_user_$userId"

    // Estado observable del progreso
    private val _progress = MutableStateFlow(cargarProgreso())

    val progress = _progress.asStateFlow()

    // Registrar una práctica completada correctamente
    fun registrarExito(
        senaId: Int,
        nombreSena: String
    ) {
        val nombre = nombreSena.trim()

        if (senaId <= 0 || nombre.isBlank()) return

        val registros = _progress.value.toMutableList()

        val index = registros.indexOfFirst {
            it.senaId == senaId
        }

        val fechaActual = System.currentTimeMillis()

        if (index >= 0) {
            val anterior = registros[index]

            registros[index] = anterior.copy(
                nombreSena = nombre,
                practicasExitosas =
                    anterior.practicasExitosas + 1L,
                ultimaPracticaMillis = fechaActual
            )
        } else {
            registros.add(
                PracticeProgress(
                    senaId = senaId,
                    nombreSena = nombre,
                    practicasExitosas = 1L,
                    ultimaPracticaMillis = fechaActual
                )
            )
        }

        guardarProgreso(
            registros.sortedByDescending {
                it.ultimaPracticaMillis
            }
        )
    }

    // Guardar el progreso localmente
    private fun guardarProgreso(
        registros: List<PracticeProgress>
    ) {
        val array = JSONArray()

        registros.forEach { registro ->
            val objeto = JSONObject().apply {
                put("senaId", registro.senaId)
                put("nombreSena", registro.nombreSena)
                put(
                    "practicasExitosas",
                    registro.practicasExitosas
                )
                put(
                    "ultimaPracticaMillis",
                    registro.ultimaPracticaMillis
                )
            }

            array.put(objeto)
        }

        preferences.edit()
            .putString(storageKey, array.toString())
            .apply()

        _progress.value = registros.toList()
    }

    // Recuperar el progreso almacenado
    private fun cargarProgreso(): List<PracticeProgress> {
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

                    val senaId = objeto.optInt("senaId")
                    val nombre = objeto
                        .optString("nombreSena")
                        .trim()

                    val exitos = objeto.optLong(
                        "practicasExitosas"
                    )

                    val fecha = objeto.optLong(
                        "ultimaPracticaMillis"
                    )

                    if (
                        senaId <= 0 ||
                        nombre.isBlank() ||
                        exitos <= 0L ||
                        fecha <= 0L
                    ) {
                        continue
                    }

                    add(
                        PracticeProgress(
                            senaId = senaId,
                            nombreSena = nombre,
                            practicasExitosas = exitos,
                            ultimaPracticaMillis = fecha
                        )
                    )
                }
            }.sortedByDescending {
                it.ultimaPracticaMillis
            }.distinctBy {
                it.senaId
            }

        } catch (e: JSONException) {
            emptyList()
        }
    }
}