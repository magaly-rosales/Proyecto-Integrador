package com.tecsup.usuario_kotlin.viewmodel

import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.usuario_kotlin.data.model.PuntoManoDto
import com.tecsup.usuario_kotlin.data.model.ReconocimientoResponse
import com.tecsup.usuario_kotlin.data.recognition.DynamicSignRecognizer
import com.tecsup.usuario_kotlin.data.recognition.StaticSignRecognizer
import com.tecsup.usuario_kotlin.data.repository.SenasRepository
import com.tecsup.usuario_kotlin.utils.RecognitionUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PracticeViewModel(
    private val repository: SenasRepository = SenasRepository()
) : ViewModel() {

    companion object {
        private const val REQUEST_INTERVAL_MS = 700L
        const val REQUIRED_CONFIRMATIONS = 2
    }

    // Reconocedores compartidos
    private val staticRecognizer = StaticSignRecognizer(repository)
    private val dynamicRecognizer = DynamicSignRecognizer(repository)

    // Estado de la práctica
    private val _uiState = MutableStateFlow(PracticeUiState())

    val uiState = _uiState.asStateFlow()

    // Control de solicitudes
    private var requestJob: Job? = null
    private var generation = 0L
    private var lastStaticRequest = -REQUEST_INTERVAL_MS
    private var lastDynamicRequest = -REQUEST_INTERVAL_MS
    private var lastRequestWasDynamic = false

    // Confirmaciones independientes
    private var staticConfirmations = 0

    // Callback para registrar prácticas exitosas
    private var completionListener: ((String) -> Unit)? = null

    fun setOnPracticeCompleted(
        listener: ((String) -> Unit)?
    ) {
        completionListener = listener
    }

    // Iniciar una práctica
    fun iniciarPractica(nombreSena: String) {
        reiniciarCaptura()

        val objetivo = nombreSena.trim()

        if (objetivo.isBlank()) {
            _uiState.value = PracticeUiState(
                status = PracticeStatus.ERROR,
                errorMessage = "No se ha seleccionado una seña"
            )
            return
        }

        _uiState.value = PracticeUiState(
            targetSign = objetivo,
            status = PracticeStatus.WAITING_HAND,
            isActive = true
        )
    }

    // Recibir puntos de MediaPipe
    fun onHandsDetected(puntos: List<PuntoManoDto>) {
        if (!_uiState.value.isActive) return

        if (!RecognitionUtils.puntosValidos(puntos)) {
            limpiarDeteccion()
            return
        }

        val now = SystemClock.elapsedRealtime()
        val actuales = puntos.toList()

        _uiState.value = _uiState.value.copy(
            handLandmarks = actuales
        )

        // Capturar movimiento de forma continua
        if (dynamicRecognizer.registrar(actuales, now)) {
            cancelarSolicitud()
            staticConfirmations = 0

            _uiState.value = _uiState.value.copy(
                detectedSign = null,
                confirmations = 0,
                isAnalyzing = false,
                status = PracticeStatus.WAITING_HAND
            )
        }

        if (requestJob?.isActive == true) return

        val staticAvailable =
            now - lastStaticRequest >= REQUEST_INTERVAL_MS

        val dynamicAvailable =
            dynamicRecognizer.secuenciaPendiente != null &&
                    now - lastDynamicRequest >= REQUEST_INTERVAL_MS

        // Alternar ambos tipos de reconocimiento
        when {
            dynamicAvailable &&
                    (!lastRequestWasDynamic || !staticAvailable) -> {

                val cuadros = dynamicRecognizer.tomarSecuencia()
                    ?: return

                lastDynamicRequest = now
                lastRequestWasDynamic = true

                analizarSena(dinamico = true) {
                    dynamicRecognizer.reconocer(cuadros)
                }
            }

            staticAvailable -> {
                lastStaticRequest = now
                lastRequestWasDynamic = false

                analizarSena(dinamico = false) {
                    staticRecognizer.reconocer(actuales)
                }
            }
        }
    }

    // Ejecutar reconocimiento
    private fun analizarSena(
        dinamico: Boolean,
        peticion: suspend () -> ReconocimientoResponse
    ) {
        if (!_uiState.value.isActive) return
        if (requestJob?.isActive == true) return

        val version = generation
        val estado = _uiState.value

        _uiState.value = estado.copy(
            isAnalyzing = true,
            errorMessage = null,
            status = if (
                estado.status == PracticeStatus.WAITING_HAND
            ) {
                PracticeStatus.ANALYZING
            } else {
                estado.status
            }
        )

        requestJob = viewModelScope.launch(
            start = CoroutineStart.LAZY
        ) {
            try {
                val resultado = peticion()

                if (
                    version != generation ||
                    !_uiState.value.isActive
                ) {
                    return@launch
                }

                procesarResultado(resultado, dinamico)

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                if (version == generation) {
                    if (dinamico) {
                        dynamicRecognizer.limpiarConfirmaciones()
                    } else {
                        staticConfirmations = 0
                    }

                    dynamicRecognizer.limpiar()

                    _uiState.value = _uiState.value.copy(
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
                    _uiState.value = _uiState.value.copy(
                        isAnalyzing = false
                    )

                    requestJob = null
                }
            }
        }

        requestJob?.start()
    }

    // Evaluar si el resultado coincide con el objetivo
    private fun procesarResultado(
        resultado: ReconocimientoResponse,
        dinamico: Boolean
    ) {
        val estado = _uiState.value

        if (!estado.isActive) return

        val detectada = RecognitionUtils.obtenerNombre(
            resultado
        )

        val coincide = RecognitionUtils.coincidenSenas(
            detectada = detectada,
            objetivo = estado.targetSign
        )

        // Confirmaciones independientes por reconocimiento
        if (dinamico) {
            if (coincide) {
                dynamicRecognizer.confirmar(resultado)
            } else {
                dynamicRecognizer.limpiarConfirmaciones()
            }
        } else {
            staticConfirmations = if (coincide) {
                (staticConfirmations + 1)
                    .coerceAtMost(REQUIRED_CONFIRMATIONS)
            } else {
                0
            }
        }

        val confirmaciones = maxOf(
            staticConfirmations,
            dynamicRecognizer.confirmaciones
        )

        val completada =
            confirmaciones >= REQUIRED_CONFIRMATIONS

        val nuevoEstado = when {
            completada ->
                PracticeStatus.SUCCESS

            confirmaciones > 0 ->
                PracticeStatus.CONFIRMING

            RecognitionUtils.esNombreValido(detectada) ->
                PracticeStatus.DIFFERENT_SIGN

            else ->
                PracticeStatus.NOT_RECOGNIZED
        }

        _uiState.value = estado.copy(
            detectedSign = if (confirmaciones > 0) {
                estado.targetSign
            } else {
                detectada?.takeIf {
                    RecognitionUtils.esNombreValido(it)
                }
            },
            confirmations = confirmaciones,
            status = nuevoEstado,
            isActive = !completada,
            isAnalyzing = false
        )

        if (completada) {
            dynamicRecognizer.limpiar()
            staticConfirmations = 0

            // Registrar el éxito una sola vez por intento
            try {
                completionListener?.invoke(
                    estado.targetSign
                )
            } catch (e: Exception) {
                Log.e(
                    "PracticeViewModel",
                    "No se pudo registrar el progreso",
                    e
                )
            }
        }
    }

    // Limpiar al perder la detección de las manos
    private fun limpiarDeteccion() {
        val estado = _uiState.value

        if (!estado.isActive) return

        if (
            estado.handLandmarks.isEmpty() &&
            estado.status == PracticeStatus.WAITING_HAND &&
            !estado.isAnalyzing
        ) {
            return
        }

        reiniciarCaptura()

        _uiState.value = estado.copy(
            handLandmarks = emptyList(),
            detectedSign = null,
            confirmations = 0,
            isAnalyzing = false,
            status = PracticeStatus.WAITING_HAND
        )
    }

    fun reiniciarPractica() {
        iniciarPractica(_uiState.value.targetSign)
    }

    // Limpiar al cambiar de cámara
    fun limpiarManos() {
        limpiarDeteccion()
    }

    // Controlar errores de cámara
    fun mostrarErrorCamara(mensaje: String) {
        if (!_uiState.value.isActive) return

        reiniciarCaptura()

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

    // Detener el entrenamiento
    fun detenerPractica() {
        reiniciarCaptura()

        _uiState.value = PracticeUiState(
            targetSign = _uiState.value.targetSign
        )
    }

    // Limpiar la sesión de entrenamiento
    fun limpiarPractica() {
        reiniciarCaptura()
        _uiState.value = PracticeUiState()
    }

    private fun cancelarSolicitud() {
        generation++

        requestJob?.cancel()
        requestJob = null

        lastStaticRequest = -REQUEST_INTERVAL_MS
        lastDynamicRequest = -REQUEST_INTERVAL_MS
        lastRequestWasDynamic = false
    }

    private fun reiniciarCaptura() {
        cancelarSolicitud()
        dynamicRecognizer.limpiar()
        staticConfirmations = 0
    }

    override fun onCleared() {
        completionListener = null
        reiniciarCaptura()
        super.onCleared()
    }
}