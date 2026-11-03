package com.tecsup.usuario_kotlin.ui.translation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tecsup.usuario_kotlin.viewmodel.TranslationViewModel

@Composable
fun TranslationScreen(viewModel: TranslationViewModel) {
    val currentSign by viewModel.translatedSign.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Traductor de Señas en Vivo",
                style = MaterialTheme.typography.headlineMedium
            )

            // Simulación del visor de la cámara / MediaPipe
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "[ Vista previa de la Cámara / MediaPipe ]")
                }
            }

            // Resultado de la traducción en tiempo real
            Text(
                text = "Seña detectada: $currentSign",
                style = MaterialTheme.typography.titleLarge
            )

            Button(
                onClick = { viewModel.updateTranslation("¡Hola!") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Probar seña")
            }
        }
    }
}