package com.tecsup.usuario_kotlin.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TranslationViewModel : ViewModel() {
    private val _translatedSign = MutableStateFlow("Esperando seña...")
    val translatedSign: StateFlow<String> = _translatedSign

    fun updateTranslation(newSign: String) {
        _translatedSign.value = newSign
    }
}