package com.tecsup.usuario_kotlin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.usuario_kotlin.data.model.SenaDto
import com.tecsup.usuario_kotlin.data.repository.SenasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TranslationViewModel(
    private val repository: SenasRepository = SenasRepository()
) : ViewModel() {

    private val _translatedSign = MutableStateFlow("Esperando seña...")
    val translatedSign: StateFlow<String> = _translatedSign

    private val _senasList = MutableStateFlow<List<SenaDto>>(emptyList())
    val senasList: StateFlow<List<SenaDto>> = _senasList

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        cargarSenas()
    }

    fun cargarSenas() {
        viewModelScope.launch {
            repository.getSenasDisponibles()
                .onSuccess { lista ->
                    _senasList.value = lista
                    _errorMessage.value = null
                }
                .onFailure { error ->
                    _errorMessage.value = error.localizedMessage ?: "Error al conectar con el servidor"
                }
        }
    }

    fun updateTranslation(newSign: String) {
        _translatedSign.value = newSign
    }
}