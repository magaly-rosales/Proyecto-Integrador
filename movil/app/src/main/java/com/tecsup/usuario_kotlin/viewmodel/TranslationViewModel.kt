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
import com.tecsup.usuario_kotlin.utils.PhraseBuilder
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
        private const val WAITING_SIGN = "Esperando seña..."
    }

    // Componentes reutilizables
    private val staticRecognizer = StaticSignRecognizer(repository)
    private val dynamicRecognizer = DynamicSignRecognizer(repository)
    private val phraseBuilder = PhraseBuilder()

    // Palabras que conforman la frase
    val phraseWords = phraseBuilder.words

    // Resultado del reconocimiento
    private val _translatedSign = MutableStateFlow(WAITING_SIGN)
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

    // Reconocimiento dinámico
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

    // Recibir los puntos detectados por la cámara
    fun onHandDetected(
        puntos: List<PuntoManoDto>,
        onSignAccepted: (String) -> Unit = {}
    ) {
        val now = SystemClock.elapsedRealtime()

        if (!RecognitionUtils.puntosValidos(puntos)) {
            if (_handLandmarks.value.isNotEmpty()) {
                detenerReconocimiento()
            }

            // Permitir repetir una seña después de retirar las manos
            phraseBuilder.registrarAusencia(now)
            return
        }

        phraseBuilder.registrarPresencia()

        val actuales = puntos.toList()
        _handLandmarks.value = actuales

        // Capturar movimiento continuamente
        if (dynamicRecognizer.registrar(actuales, now)) {
            cancelarSolicitud()
            phraseBuilder.limpiarCandidato()
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

                ejecutarReconocimiento(
                    dinamico = true,
                    onSignAccepted = onSignAccepted
                ) {
                    dynamicRecognizer.reconocer(cuadros)
                }
            }

            staticAvailable -> {
                lastStaticRequest = now
                lastRequestWasDynamic = false

                ejecutarReconocimiento(
                    dinamico = false,
                    onSignAccepted = onSignAccepted
                ) {
                    staticRecognizer.reconocer(actuales)
                }
            }
        }
    }

    // Ejecutar una sola solicitud a la vez
    private fun ejecutarReconocimiento(
        dinamico: Boolean,
        onSignAccepted: (String) -> Unit,
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

                val nuevaSena = if (dinamico) {
                    procesarDinamico(resultado)
                } else {
                    procesarEstatico(resultado)
                }

                _errorMessage.value = null

                // Registrar únicamente señas agregadas a la frase
                nuevaSena?.let(onSignAccepted)

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                if (version == generation) {
                    if (dinamico) {
                        dynamicRecognizer.limpiarConfirmaciones()
                    } else {
                        phraseBuilder.limpiarCandidato()
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

    // Procesar una seña estática
    private fun procesarEstatico(
        resultado: ReconocimientoResponse
    ): String? {
        val now = SystemClock.elapsedRealtime()

        // Evitar reemplazar inmediatamente un resultado dinámico
        if (
            lastDynamicAcceptedAt?.let {
                now - it < DYNAMIC_HOLD_MS
            } == true
        ) {
            return null
        }

        publicarResultado(resultado, dinamico = false)

        // PhraseBuilder exige tres confirmaciones
        return phraseBuilder.registrarEstatica(
            RecognitionUtils.obtenerNombre(resultado)
        )
    }

    // Procesar una seña dinámica
    private fun procesarDinamico(
        resultado: ReconocimientoResponse
    ): String? {
        if (!dynamicRecognizer.confirmar(resultado)) {
            return null
        }

        publicarResultado(resultado, dinamico = true)

        lastDynamicAcceptedAt = SystemClock.elapsedRealtime()

        val nuevaSena = phraseBuilder.registrarDinamica(
            RecognitionUtils.obtenerNombre(resultado)
        )

        dynamicRecognizer.limpiar()
        _sequenceFrameCount.value = 0

        return nuevaSena
    }

    // Actualizar el resultado visible de la cámara
    private fun publicarResultado(
        resultado: ReconocimientoResponse,
        dinamico: Boolean
    ) {
        _translatedSign.value =
            RecognitionUtils.obtenerNombre(resultado) ?: WAITING_SIGN

        _confidence.value =
            RecognitionUtils.obtenerConfianza(resultado)

        _isDynamicRecognition.value = dinamico
    }

    // GESTIÓN DE FRASES

    fun obtenerFrase(): String {
        return phraseBuilder.obtenerFrase()
    }

    fun borrarUltimaPalabra() {
        detenerReconocimiento()
        phraseBuilder.borrarUltima()
    }

    fun limpiarFrase() {
        detenerReconocimiento()
        phraseBuilder.limpiarFrase()
    }

    fun cargarFrase(palabras: List<String>) {
        detenerReconocimiento()
        phraseBuilder.cargarFrase(palabras)
    }

    // CONTROL DEL RECONOCIMIENTO

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

    // Detener reconocimiento sin eliminar la frase
    fun detenerReconocimiento() {
        cancelarSolicitud()
        dynamicRecognizer.limpiar()
        phraseBuilder.reiniciarDeteccion()

        _sequenceFrameCount.value = 0
        _handLandmarks.value = emptyList()
        _translatedSign.value = WAITING_SIGN
        _confidence.value = null
        _isDynamicRecognition.value = false
        _errorMessage.value = null
    }

    // Limpiar la sesión al abandonar el traductor
    fun cerrarSesionTraductor() {
        detenerReconocimiento()
        phraseBuilder.reiniciarSesion()
    }

    // Mantener compatibilidad con las funciones anteriores
    fun updateTranslation(
        newSign: String,
        confidencePercent: Int? = null
    ) {
        detenerReconocimiento()

        _translatedSign.value = newSign
        _confidence.value = confidencePercent
    }

    override fun onCleared() {
        cancelarSolicitud()
        dynamicRecognizer.limpiar()
        phraseBuilder.reiniciarSesion()
        super.onCleared()
    }
}