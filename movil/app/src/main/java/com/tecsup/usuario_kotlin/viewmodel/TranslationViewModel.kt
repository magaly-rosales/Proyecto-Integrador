package com.tecsup.usuario_kotlin.viewmodel

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.usuario_kotlin.data.model.CategoriaCatalogoDto
import com.tecsup.usuario_kotlin.data.model.PuntoManoDto
import com.tecsup.usuario_kotlin.data.model.ReconocimientoResponse
import com.tecsup.usuario_kotlin.data.model.SenaCatalogoDto
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

class TranslationViewModel(
    private val repository: SenasRepository = SenasRepository()
) : ViewModel() {

    companion object {
        private const val STATIC_INTERVAL_MS = 600L
        private const val DYNAMIC_INTERVAL_MS = 700L
        private const val DYNAMIC_HOLD_MS = 1500L
    }

    // Reconocedores reutilizables
    private val staticRecognizer = StaticSignRecognizer(repository)
    private val dynamicRecognizer = DynamicSignRecognizer(repository)

    // Resultado de traducción
    private val _translatedSign = MutableStateFlow("Esperando seña...")
    val translatedSign = _translatedSign.asStateFlow()

    private val _confidence = MutableStateFlow<Int?>(null)
    val confidence = _confidence.asStateFlow()

    // Puntos de MediaPipe
    private val _handLandmarks =
        MutableStateFlow<List<PuntoManoDto>>(emptyList())
    val handLandmarks = _handLandmarks.asStateFlow()

    // Catálogo
    private val _senasList =
        MutableStateFlow<List<SenaCatalogoDto>>(emptyList())
    val senasList = _senasList.asStateFlow()

    private val _categoriasList =
        MutableStateFlow<List<CategoriaCatalogoDto>>(emptyList())
    val categoriasList = _categoriasList.asStateFlow()

    // Errores
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    // Estados del reconocimiento dinámico
    private val _sequenceFrameCount = MutableStateFlow(0)
    val sequenceFrameCount = _sequenceFrameCount.asStateFlow()

    private val _isRecognizingSequence = MutableStateFlow(false)
    val isRecognizingSequence = _isRecognizingSequence.asStateFlow()

    private val _isDynamicRecognition = MutableStateFlow(false)
    val isDynamicRecognition = _isDynamicRecognition.asStateFlow()

    // Control de solicitudes
    private var requestJob: Job? = null
    private var generation = 0L
    private var lastStaticRequest = -STATIC_INTERVAL_MS
    private var lastDynamicRequest = -DYNAMIC_INTERVAL_MS
    private var lastRequestWasDynamic = false
    private var lastDynamicAcceptedAt: Long? = null

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
                        ?: "Error al cargar el catálogo"
                }
        }
    }

    // Recibir puntos de la cámara
    fun onHandDetected(puntos: List<PuntoManoDto>) {
        if (!RecognitionUtils.puntosValidos(puntos)) {
            if (_handLandmarks.value.isNotEmpty()) {
                detenerReconocimiento()
            }
            return
        }

        val now = SystemClock.elapsedRealtime()
        val actuales = puntos.toList()

        _handLandmarks.value = actuales

        // Capturar movimiento continuamente
        if (dynamicRecognizer.registrar(actuales, now)) {
            cancelarSolicitud()
            _confidence.value = null
        }

        _sequenceFrameCount.value = dynamicRecognizer.frameCount

        if (requestJob?.isActive == true) return

        val staticAvailable =
            now - lastStaticRequest >= STATIC_INTERVAL_MS

        val dynamicAvailable =
            dynamicRecognizer.secuenciaPendiente != null &&
                    now - lastDynamicRequest >= DYNAMIC_INTERVAL_MS

        // Alternar reconocimiento estático y dinámico
        when {
            dynamicAvailable &&
                    (!lastRequestWasDynamic || !staticAvailable) -> {

                val cuadros = dynamicRecognizer.tomarSecuencia()
                    ?: return

                lastDynamicRequest = now
                lastRequestWasDynamic = true

                ejecutarReconocimiento(true) {
                    dynamicRecognizer.reconocer(cuadros)
                }
            }

            staticAvailable -> {
                lastStaticRequest = now
                lastRequestWasDynamic = false

                ejecutarReconocimiento(false) {
                    staticRecognizer.reconocer(actuales)
                }
            }
        }
    }

    // Ejecutar una sola solicitud a la vez
    private fun ejecutarReconocimiento(
        dinamico: Boolean,
        peticion: suspend () -> ReconocimientoResponse
    ) {
        if (requestJob?.isActive == true) return

        val version = generation
        _isRecognizingSequence.value = dinamico

        requestJob = viewModelScope.launch(start = CoroutineStart.LAZY) {
            try {
                val resultado = peticion()

                if (
                    version != generation ||
                    _handLandmarks.value.isEmpty()
                ) {
                    return@launch
                }

                if (dinamico) {
                    procesarDinamico(resultado)
                } else {
                    procesarEstatico(resultado)
                }

                _errorMessage.value = null

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                if (version == generation) {
                    if (dinamico) {
                        dynamicRecognizer.limpiarConfirmaciones()
                    }

                    _errorMessage.value = e.localizedMessage
                        ?: "Error durante el reconocimiento"
                }

            } finally {
                if (version == generation) {
                    _isRecognizingSequence.value = false
                    requestJob = null
                }
            }
        }

        requestJob?.start()
    }

    private fun procesarEstatico(resultado: ReconocimientoResponse) {
        val now = SystemClock.elapsedRealtime()

        // Proteger temporalmente un resultado dinámico confirmado
        if (
            lastDynamicAcceptedAt?.let {
                now - it < DYNAMIC_HOLD_MS
            } == true
        ) {
            return
        }

        publicarResultado(resultado, dinamico = false)
    }

    private fun procesarDinamico(resultado: ReconocimientoResponse) {
        if (!dynamicRecognizer.confirmar(resultado)) return

        publicarResultado(resultado, dinamico = true)

        lastDynamicAcceptedAt = SystemClock.elapsedRealtime()

        dynamicRecognizer.limpiar()
        _sequenceFrameCount.value = 0
    }

    private fun publicarResultado(
        resultado: ReconocimientoResponse,
        dinamico: Boolean
    ) {
        val nombre = RecognitionUtils.obtenerNombre(resultado)
            ?: return

        _translatedSign.value = nombre
        _confidence.value = RecognitionUtils.obtenerConfianza(resultado)
        _isDynamicRecognition.value = dinamico
    }

    private fun cancelarSolicitud() {
        generation++
        requestJob?.cancel()
        requestJob = null

        lastStaticRequest = -STATIC_INTERVAL_MS
        lastDynamicRequest = -DYNAMIC_INTERVAL_MS
        lastRequestWasDynamic = false
        lastDynamicAcceptedAt = null

        _isRecognizingSequence.value = false
    }

    // Detener cámara y reconocimiento
    fun detenerReconocimiento() {
        cancelarSolicitud()
        dynamicRecognizer.limpiar()

        _sequenceFrameCount.value = 0
        _handLandmarks.value = emptyList()
        _confidence.value = null
        _errorMessage.value = null
    }

    // Actualización manual del texto
    fun updateTranslation(
        newSign: String,
        confidencePercent: Int? = null
    ) {
        detenerReconocimiento()

        _translatedSign.value = newSign
        _confidence.value = confidencePercent
        _isDynamicRecognition.value = false
    }

    override fun onCleared() {
        cancelarSolicitud()
        dynamicRecognizer.limpiar()
        super.onCleared()
    }
}