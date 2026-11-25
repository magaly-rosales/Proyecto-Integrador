
package com.tecsup.usuario_kotlin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.usuario_kotlin.data.model.CategoriaCatalogoDto
import com.tecsup.usuario_kotlin.data.model.PuntoManoDto
import com.tecsup.usuario_kotlin.data.model.ReconocimientoResponse
import com.tecsup.usuario_kotlin.data.model.SenaCatalogoDto
import com.tecsup.usuario_kotlin.data.repository.SenasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

class TranslationViewModel(
    private val repository: SenasRepository = SenasRepository()
) : ViewModel() {

    private val _translatedSign = MutableStateFlow("Esperando seña...")
    val translatedSign: StateFlow<String> = _translatedSign.asStateFlow()

    private val _confidence = MutableStateFlow<Int?>(null)
    val confidence: StateFlow<Int?> = _confidence.asStateFlow()

    private val _handLandmarks = MutableStateFlow<List<PuntoManoDto>>(emptyList())
    val handLandmarks: StateFlow<List<PuntoManoDto>> = _handLandmarks.asStateFlow()

    private val _senasList = MutableStateFlow<List<SenaCatalogoDto>>(emptyList())
    val senasList: StateFlow<List<SenaCatalogoDto>> = _senasList.asStateFlow()

    private val _categoriasList = MutableStateFlow<List<CategoriaCatalogoDto>>(emptyList())
    val categoriasList: StateFlow<List<CategoriaCatalogoDto>> = _categoriasList.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var lastRequestTime = 0L
    private var isClassifying = false

    init {
        cargarCatalogo()
    }

    fun cargarCatalogo() {
        viewModelScope.launch {
            repository.getCatalogo()
                .onSuccess { catalogo ->
                    _senasList.value = catalogo.senas
                    _categoriasList.value = catalogo.categorias
                    _errorMessage.value = null
                }
                .onFailure { error ->
                    _errorMessage.value = error.localizedMessage
                        ?: "Error al conectar con Spring Boot (8081)"
                }
        }
    }

    /**
     * Jeff llamará a esta función desde MediaPipe en cada frame detectado.
     */
    fun onHandDetected(puntos: List<PuntoManoDto>) {
        _handLandmarks.value = puntos

        if (puntos.isEmpty()) {
            _confidence.value = null
            return
        }

        val now = System.currentTimeMillis()

        if (!isClassifying && (now - lastRequestTime) >= 600L) {
            lastRequestTime = now
            clasificarPuntos(puntos)
        }
    }

    private fun esSenaReconocida(
        resultado: ReconocimientoResponse
    ): Boolean {
        val nombre = resultado.sena ?: resultado.nombre

        return !nombre.isNullOrBlank() &&
                nombre.lowercase() !in setOf(
            "desconocida",
            "dudosa",
            "sin_muestras"
        )
    }

    private fun rotarPuntos(
        puntos: List<PuntoManoDto>,
        grados: Double
    ): List<PuntoManoDto> {

        val radianes = Math.toRadians(grados)
        val coseno = cos(radianes).toFloat()
        val seno = sin(radianes).toFloat()

        val origen = puntos.first()

        return puntos.map { punto ->
            val dx = punto.x - origen.x
            val dy = punto.y - origen.y

            PuntoManoDto(
                x = origen.x + dx * coseno - dy * seno,
                y = origen.y + dx * seno + dy * coseno,
                z = punto.z
            )
        }
    }

    fun clasificarPuntos(puntos: List<PuntoManoDto>) {
        if (isClassifying) return

        isClassifying = true

        viewModelScope.launch {
            try {
                val respuestaInicial =
                    repository.enviarPuntosMano(puntos)

                respuestaInicial.onFailure { error ->
                    _errorMessage.value = error.localizedMessage
                }

                var resultado =
                    respuestaInicial.getOrNull() ?: return@launch

                if (
                    puntos.size == 21 &&
                    !esSenaReconocida(resultado)
                ) {
                    val angulos = listOf(
                        -15.0,
                        15.0,
                        -30.0,
                        30.0
                    )

                    for (angulo in angulos) {
                        val puntosRotados =
                            rotarPuntos(puntos, angulo)

                        val alternativa =
                            repository.enviarPuntosMano(
                                puntosRotados
                            ).getOrNull() ?: continue

                        if (esSenaReconocida(alternativa)) {
                            val mejorDistancia =
                                resultado.distancia
                                    ?: Double.MAX_VALUE

                            val nuevaDistancia =
                                alternativa.distancia
                                    ?: Double.MAX_VALUE

                            if (
                                !esSenaReconocida(resultado) ||
                                nuevaDistancia < mejorDistancia
                            ) {
                                resultado = alternativa
                            }

                            if (nuevaDistancia <= 0.45) {
                                break
                            }
                        }
                    }
                }

                val nombreSena =
                    resultado.sena ?: resultado.nombre

                if (!nombreSena.isNullOrBlank()) {
                    _translatedSign.value = nombreSena

                    _confidence.value =
                        resultado.confianza?.let {
                            if (it <= 1.0) {
                                (it * 100).toInt()
                            } else {
                                it.toInt()
                            }
                        }
                }

                _errorMessage.value = null

            } finally {
                isClassifying = false
            }
        }
    }

    fun updateTranslation(
        newSign: String,
        confidencePercent: Int? = null
    ) {
        _translatedSign.value = newSign
        _confidence.value = confidencePercent
    }
}