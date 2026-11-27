package com.tecsup.usuario_kotlin.viewmodel

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.usuario_kotlin.data.model.PuntoManoDto
import com.tecsup.usuario_kotlin.data.repository.SenasRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PracticeViewModel(
    private val repository: SenasRepository = SenasRepository()
) : ViewModel() {

    companion object {

        // Tiempo mínimo entre solicitudes
        private const val REQUEST_INTERVAL_MS = 700L

        // Coincidencias consecutivas necesarias
        const val REQUIRED_CONFIRMATIONS = 2

        // Respuestas que no representan una seña reconocida
        private val INVALID_RESULTS = setOf(
            "desconocida",
            "dudosa",
            "sin_muestras"
        )
    }

    // Estado observable de la práctica
    private val _uiState = MutableStateFlow(
        PracticeUiState()
    )

    val uiState: StateFlow<PracticeUiState> =
        _uiState.asStateFlow()

    // Control de solicitudes
    private var requestJob: Job? = null

    private var lastRequestTime = -REQUEST_INTERVAL_MS

    // Identificar cada sesión de práctica
    private var generation = 0L

    // Iniciar una práctica nueva
    fun iniciarPractica(nombreSena: String) {

        cancelarSolicitud()

        val objetivo = nombreSena.trim()

        if (objetivo.isBlank()) {

            _uiState.value = PracticeUiState(
                status = PracticeStatus.ERROR,
                errorMessage = "No se ha seleccionado una seña"
            )

            return
        }

        lastRequestTime = -REQUEST_INTERVAL_MS

        _uiState.value = PracticeUiState(
            targetSign = objetivo,
            status = PracticeStatus.WAITING_HAND,
            isActive = true
        )
    }

    // Recibir los puntos detectados por MediaPipe
    fun onHandsDetected(
        puntos: List<PuntoManoDto>
    ) {

        val estado = _uiState.value

        // Ignorar resultados fuera de una práctica activa
        if (!estado.isActive) return

        // Validar una o dos manos completas
        val puntosValidos =
            puntos.size in setOf(21, 42) &&
                    puntos.all { punto ->
                        punto.x.isFinite() &&
                                punto.y.isFinite() &&
                                punto.z.isFinite()
                    }

        if (!puntosValidos) {

            limpiarDeteccion()

            return
        }

        // Actualizar puntos para el overlay
        _uiState.value = estado.copy(
            handLandmarks = puntos.toList()
        )

        val tiempoActual =
            SystemClock.elapsedRealtime()

        // Evitar peticiones simultáneas
        if (requestJob?.isActive == true) {
            return
        }

        // Limitar las solicitudes al servidor
        if (
            tiempoActual - lastRequestTime <
            REQUEST_INTERVAL_MS
        ) {
            return
        }

        lastRequestTime = tiempoActual

        analizarSena(puntos.toList())
    }

    // Clasificar los puntos utilizando Spring Boot
    private fun analizarSena(
        puntos: List<PuntoManoDto>
    ) {

        if (!_uiState.value.isActive) return

        val version = generation

        val estado = _uiState.value

        _uiState.value = estado.copy(
            isAnalyzing = true,
            errorMessage = null,
            status = if (
                estado.status ==
                PracticeStatus.WAITING_HAND
            ) {
                PracticeStatus.ANALYZING
            } else {
                estado.status
            }
        )

        requestJob = viewModelScope.launch {

            try {

                // Reutilizar el reconocimiento existente
                val resultado =
                    repository.enviarPuntosMano(puntos)

                currentCoroutineContext().ensureActive()

                // Ignorar respuestas de prácticas anteriores
                if (!esPracticaActual(version)) {
                    return@launch
                }

                val respuesta = resultado.getOrThrow()

                val nombreDetectado =
                    respuesta.sena
                        ?.takeIf { it.isNotBlank() }
                        ?: respuesta.nombre

                procesarResultado(
                    nombreDetectado
                )

            } catch (e: CancellationException) {

                throw e

            } catch (e: Exception) {

                if (esPracticaActual(version)) {

                    _uiState.value =
                        _uiState.value.copy(
                            status = PracticeStatus.ERROR,
                            isActive = false,
                            isAnalyzing = false,
                            confirmations = 0,
                            detectedSign = null,
                            handLandmarks = emptyList(),
                            errorMessage =
                                "No se pudo completar el reconocimiento. " +
                                        "Comprueba la conexión e inténtalo nuevamente."
                        )
                }

            } finally {

                if (version == generation) {

                    _uiState.value =
                        _uiState.value.copy(
                            isAnalyzing = false
                        )

                    requestJob = null
                }
            }
        }
    }

    // Comparar la seña reconocida con la seña objetivo
    private fun procesarResultado(
        nombreDetectado: String?
    ) {

        val estado = _uiState.value

        if (!estado.isActive) return

        val detectada = nombreDetectado
            ?.trim()
            .orEmpty()

        // El servidor no reconoció una seña válida
        if (
            detectada.isBlank() ||
            detectada.lowercase() in INVALID_RESULTS
        ) {

            _uiState.value = estado.copy(
                status = PracticeStatus.NOT_RECOGNIZED,
                detectedSign = null,
                confirmations = 0
            )

            return
        }

        // Comparar nombres sin distinguir mayúsculas
        val coincide = detectada.equals(
            estado.targetSign,
            ignoreCase = true
        )

        if (!coincide) {

            _uiState.value = estado.copy(
                status = PracticeStatus.DIFFERENT_SIGN,
                detectedSign = detectada,
                confirmations = 0
            )

            return
        }

        // Registrar una coincidencia consecutiva
        val confirmaciones =
            (estado.confirmations + 1)
                .coerceAtMost(REQUIRED_CONFIRMATIONS)

        val completada =
            confirmaciones >= REQUIRED_CONFIRMATIONS

        _uiState.value = estado.copy(

            detectedSign = detectada,

            confirmations = confirmaciones,

            status = if (completada) {

                PracticeStatus.SUCCESS

            } else {

                PracticeStatus.CONFIRMING
            },

            // Detener el reconocimiento al completar la práctica
            isActive = !completada
        )
    }

    // Limpiar los resultados cuando no hay manos visibles
    private fun limpiarDeteccion() {

        val estado = _uiState.value

        if (!estado.isActive) return

        // Evitar reiniciar innecesariamente cada fotograma
        if (
            estado.handLandmarks.isEmpty() &&
            estado.status == PracticeStatus.WAITING_HAND &&
            !estado.isAnalyzing
        ) {
            return
        }

        cancelarSolicitud()

        _uiState.value = estado.copy(
            handLandmarks = emptyList(),
            detectedSign = null,
            confirmations = 0,
            isAnalyzing = false,
            status = PracticeStatus.WAITING_HAND
        )
    }

    // Reiniciar la práctica con el mismo objetivo
    fun reiniciarPractica() {

        val objetivo = _uiState.value.targetSign

        iniciarPractica(objetivo)
    }

    // Limpiar los puntos al cambiar de cámara
    fun limpiarManos() {
        limpiarDeteccion()
    }

    // Gestionar errores de cámara
    fun mostrarErrorCamara(mensaje: String) {

        if (!_uiState.value.isActive) return

        cancelarSolicitud()

        _uiState.value = _uiState.value.copy(
            status = PracticeStatus.ERROR,
            isActive = false,
            isAnalyzing = false,
            handLandmarks = emptyList(),
            detectedSign = null,
            confirmations = 0,
            errorMessage = mensaje
        )
    }

    // Detener la práctica y conservar la seña objetivo
    fun detenerPractica() {

        cancelarSolicitud()

        _uiState.value = PracticeUiState(
            targetSign = _uiState.value.targetSign
        )
    }

    // Limpiar completamente la sesión de práctica
    fun limpiarPractica() {

        cancelarSolicitud()

        _uiState.value = PracticeUiState()
    }

    // Cancelar solicitudes pendientes
    private fun cancelarSolicitud() {

        generation++

        requestJob?.cancel()

        requestJob = null
    }

    // Verificar que la respuesta corresponda a la sesión actual
    private fun esPracticaActual(
        version: Long
    ): Boolean {

        return version == generation &&
                _uiState.value.isActive
    }

    override fun onCleared() {

        cancelarSolicitud()

        super.onCleared()
    }
}