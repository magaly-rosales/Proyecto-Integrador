package com.tecsup.usuario_kotlin.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.usuario_kotlin.data.local.DatabaseHelper
import com.tecsup.usuario_kotlin.data.model.TranslationHistoryDto
import com.tecsup.usuario_kotlin.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TranslationViewModel : ViewModel() {

    private var dbHelper: DatabaseHelper? = null

    var currentPhrase by mutableStateOf("Necesito ayuda")
        private set

    var confidence by mutableDoubleStateOf(0.92)
        private set

    var handDetected by mutableStateOf(true)
        private set

    var isSpeaking by mutableStateOf(false)
        private set

    var selectedNavTab by mutableIntStateOf(0)
        private set

    private val _recentPhrases = MutableStateFlow<List<TranslationHistoryDto>>(
        listOf(
            TranslationHistoryDto(1, "Buenos días, ¿cómo estás?", "Hace 5 min", 0.95),
            TranslationHistoryDto(2, "Necesito ir al baño", "Hace 20 min", 0.89),
            TranslationHistoryDto(3, "Gracias por tu ayuda", "Ayer", 0.94)
        )
    )
    val recentPhrases: StateFlow<List<TranslationHistoryDto>> = _recentPhrases

    fun initDatabase(context: Context) {
        if (dbHelper == null) {
            dbHelper = DatabaseHelper(context.applicationContext)
            fetchRecentTranslations()
        }
    }

    fun fetchRecentTranslations() {
        viewModelScope.launch {
            try {
                val dbTranslations = dbHelper?.getAllTranslations()
                if (!dbTranslations.isNullOrEmpty()) {
                    _recentPhrases.value = dbTranslations
                    return@launch
                }

                val response = RetrofitClient.apiService.getRecentTranslations()
                if (response.isSuccessful && response.body() != null) {
                    _recentPhrases.value = response.body()!!
                }
            } catch (e: Exception) {
                // Mantiene los datos locales de SQLite
            }
        }
    }

    fun speakPhrase() {
        if (currentPhrase.isNotBlank()) {
            isSpeaking = true
        }
    }

    fun clearPhrase() {
        currentPhrase = ""
    }

    fun replayPhrase(phrase: String) {
        currentPhrase = phrase
        speakPhrase()
    }

    fun selectTab(index: Int) {
        selectedNavTab = index
    }

    fun updateTranslation(newSign: String, newConfidence: Double = 0.92) {
        currentPhrase = newSign
        confidence = newConfidence
        handDetected = true
        if (newSign.isNotBlank()) {
            dbHelper?.insertTranslation(newSign, "Ahora", newConfidence)
            fetchRecentTranslations()
        }
    }
}
