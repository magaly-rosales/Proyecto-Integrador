package com.tecsup.usuario_kotlin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.usuario_kotlin.data.model.CategoriaCatalogoDto
import com.tecsup.usuario_kotlin.data.model.PuntoManoDto
import com.tecsup.usuario_kotlin.data.model.SenaCatalogoDto
import com.tecsup.usuario_kotlin.data.repository.SenasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TranslationViewModel(
    private val repository: SenasRepository = SenasRepository()
) : ViewModel() {

    private val _translatedSign = MutableStateFlow("Esperando seña...")
    val translatedSign: StateFlow<String> = _translatedSign

    private val _senasList = MutableStateFlow<List<SenaCatalogoDto>>(emptyList())
    val senasList: StateFlow<List<SenaCatalogoDto>> = _senasList

    private val _categoriasList = MutableStateFlow<List<CategoriaCatalogoDto>>(emptyList())
    val categoriasList: StateFlow<List<CategoriaCatalogoDto>> = _categoriasList

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

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
                    _errorMessage.value = error.localizedMessage ?: "Error al conectar con Spring Boot (8081)"
                }
        }
    }

    fun clasificarPuntos(puntos: List<PuntoManoDto>) {
        viewModelScope.launch {
            repository.enviarPuntosMano(puntos)
                .onSuccess { resultado ->
                    _translatedSign.value = resultado.sena
                        ?: resultado.nombre
                                ?: "Seña no reconocida"
                }
                .onFailure { error ->
                    _errorMessage.value = error.localizedMessage
                }
        }
    }

    fun updateTranslation(newSign: String) {
        _translatedSign.value = newSign
    }
}