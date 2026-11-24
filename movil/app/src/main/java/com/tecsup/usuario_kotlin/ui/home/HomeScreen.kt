package com.tecsup.usuario_kotlin.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.PanTool
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.usuario_kotlin.data.model.CategoriaCatalogoDto
import com.tecsup.usuario_kotlin.data.model.SenaCatalogoDto
import com.tecsup.usuario_kotlin.data.remote.RetrofitClient
import com.tecsup.usuario_kotlin.ui.translation.SenasBottomBar
import com.tecsup.usuario_kotlin.viewmodel.TranslationViewModel
import kotlinx.coroutines.launch

private val BgDark = Color(0xFF08141A)
private val CardDark = Color(0xFF0F2129)
private val CardElevated = Color(0xFF132A34)
private val AccentCyan = Color(0xFF22B8CF)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF8A9BA8)
private val BadgeYellow = Color(0xFFF5B82E)

@Composable
fun HomeScreen(
    username: String,
    onNavigateToTranslator: () -> Unit,
    onNavigateToPerfil: () -> Unit = {},
    viewModel: TranslationViewModel
) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var categorias by remember { mutableStateOf<List<CategoriaCatalogoDto>>(emptyList()) }
    var senas by remember { mutableStateOf<List<SenaCatalogoDto>>(emptyList()) }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    fun cargarCatalogo() {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = RetrofitClient.apiService.obtenerCatalogo()
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    categorias = body.categorias
                    senas = body.senas
                } else {
                    errorMessage = "Error al cargar catálogo (${response.code()})"
                }
            } catch (e: Exception) {
                errorMessage = "Sin conexión con el servidor: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        cargarCatalogo()
    }

    val senasFiltradas = remember(senas, selectedCategoryId, searchQuery) {
        senas.filter { sena ->
            val matchesCat = selectedCategoryId == null || sena.idCategoria == selectedCategoryId
            val matchesText = searchQuery.isBlank() ||
                    sena.nombre.contains(searchQuery, ignoreCase = true) ||
                    (sena.descripcion?.contains(searchQuery, ignoreCase = true) == true)
            matchesCat && matchesText
        }
    }

    Scaffold(
        containerColor = BgDark,
        bottomBar = {
            SenasBottomBar(
                selectedTab = 1,
                onSelectTraducir = onNavigateToTranslator,
                onSelectSenas = {},
                onSelectPerfil = onNavigateToPerfil
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Título + recargar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Diccionario de Señas",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${senasFiltradas.size} señas disponibles",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                IconButton(onClick = { cargarCatalogo() }) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Actualizar catálogo",
                        tint = AccentCyan
                    )
                }
            }

            // Buscador
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar seña o frase...", color = TextSecondary) },
                leadingIcon = {
                    Icon(Icons.Outlined.Search, contentDescription = null, tint = AccentCyan)
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CardDark,
                    unfocusedContainerColor = CardDark,
                    focusedBorderColor = AccentCyan,
                    unfocusedBorderColor = CardElevated,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            // Chips de categorías desde PostgreSQL
            if (categorias.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        CategoryChip(
                            label = "Todas",
                            selected = selectedCategoryId == null,
                            onClick = { selectedCategoryId = null }
                        )
                    }
                    items(categorias) { cat ->
                        CategoryChip(
                            label = "${cat.nombre} (${cat.total})",
                            selected = selectedCategoryId == cat.id,
                            onClick = { selectedCategoryId = cat.id }
                        )
                    }
                }
            }

            // Contenido principal
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentCyan)
                    }
                }
                errorMessage != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = errorMessage!!, color = TextSecondary, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { cargarCatalogo() },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = BgDark)
                            ) {
                                Text("Reintentar", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                senasFiltradas.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No se encontraron señas registradas", color = TextSecondary)
                    }
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(senasFiltradas) { sena ->
                            SenaCatalogItemCard(
                                sena = sena,
                                onPracticeClick = {
                                    viewModel.updateTranslation(sena.nombre)
                                    onNavigateToTranslator()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = if (selected) AccentCyan else CardDark,
        border = BorderStroke(1.dp, if (selected) AccentCyan else CardElevated)
    ) {
        Text(
            text = label,
            color = if (selected) BgDark else TextPrimary,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun SenaCatalogItemCard(
    sena: SenaCatalogoDto,
    onPracticeClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPracticeClick),
        shape = RoundedCornerShape(16.dp),
        color = CardDark,
        border = BorderStroke(1.dp, CardElevated)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardElevated)
                    .border(1.dp, AccentCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PanTool,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = sena.nombre,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (sena.nueva) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(BadgeYellow)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "NUEVA",
                                color = BgDark,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (!sena.descripcion.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = sena.descripcion,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        maxLines = 2
                    )
                }

                if (!sena.categoria.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = sena.categoria,
                        color = AccentCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}