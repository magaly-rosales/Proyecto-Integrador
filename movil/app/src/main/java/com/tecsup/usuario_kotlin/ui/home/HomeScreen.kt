package com.tecsup.usuario_kotlin.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.usuario_kotlin.data.model.CategoriaCatalogoDto
import com.tecsup.usuario_kotlin.data.model.SenaCatalogoDto
import com.tecsup.usuario_kotlin.data.remote.RetrofitClient
import com.tecsup.usuario_kotlin.ui.home.components.SenaCatalogItemCard
import com.tecsup.usuario_kotlin.ui.theme.ProfileColors
import com.tecsup.usuario_kotlin.ui.translation.SenasBottomBar
import com.tecsup.usuario_kotlin.viewmodel.TranslationViewModel
import kotlinx.coroutines.CancellationException

@Composable
fun HomeScreen(
    username: String,
    onNavigateToTranslator: () -> Unit,
    onNavigateToPerfil: () -> Unit = {},
    viewModel: TranslationViewModel
) {
    // Estados del catálogo
    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    var categorias by remember {
        mutableStateOf<List<CategoriaCatalogoDto>>(emptyList())
    }

    var senas by remember {
        mutableStateOf<List<SenaCatalogoDto>>(emptyList())
    }

    // Estados de búsqueda y filtrado
    var selectedCategoryId by remember {
        mutableStateOf<Int?>(null)
    }

    var searchQuery by remember {
        mutableStateOf("")
    }

    // Dirección del servidor obtenida desde Retrofit
    var apiUrl by remember {
        mutableStateOf<String?>(null)
    }

    // Controlar las actualizaciones del catálogo
    var refreshVersion by remember {
        mutableIntStateOf(0)
    }

    val cargarCatalogo: () -> Unit = {
        if (!isLoading) {
            refreshVersion++
        }
    }

    // Cargar el catálogo desde Spring Boot
    LaunchedEffect(refreshVersion) {

        isLoading = true
        errorMessage = null

        try {

            val response =
                RetrofitClient.apiService.obtenerCatalogo()

            val body = response.body()

            if (response.isSuccessful && body != null) {

                categorias = body.categorias
                senas = body.senas

                // Obtener la URL real de la petición
                apiUrl = response.raw()
                    .request
                    .url
                    .toString()

            } else {

                errorMessage =
                    "Error al cargar catálogo (${response.code()})"
            }

        } catch (e: CancellationException) {

            throw e

        } catch (e: Exception) {

            errorMessage =
                "Sin conexión con el servidor: " +
                        (e.localizedMessage ?: "Error desconocido")

        } finally {

            isLoading = false
        }
    }

    // Filtrar señas por categoría y búsqueda
    val senasFiltradas = remember(
        senas,
        selectedCategoryId,
        searchQuery
    ) {
        senas.filter { sena ->

            val matchesCat =
                selectedCategoryId == null ||
                        sena.idCategoria == selectedCategoryId

            val matchesText =
                searchQuery.isBlank() ||
                        sena.nombre.contains(
                            searchQuery,
                            ignoreCase = true
                        ) ||
                        (
                                sena.descripcion?.contains(
                                    searchQuery,
                                    ignoreCase = true
                                ) == true
                                )

            matchesCat && matchesText
        }
    }

    Scaffold(
        containerColor = ProfileColors.BgDark,
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
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cabecera del catálogo
            CatalogHeader(
                totalSenas = senasFiltradas.size,
                isLoading = isLoading,
                onRefresh = cargarCatalogo
            )

            // Buscador de señas
            CatalogSearchField(
                searchQuery = searchQuery,
                onSearchChange = {
                    searchQuery = it
                }
            )

            // Categorías disponibles
            if (categorias.isNotEmpty()) {

                LazyRow(
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        CategoryChip(
                            label = "Todas",
                            selected = selectedCategoryId == null,
                            onClick = {
                                selectedCategoryId = null
                            }
                        )
                    }

                    items(categorias) { categoria ->

                        CategoryChip(
                            label = "${categoria.nombre} (${categoria.total})",
                            selected =
                                selectedCategoryId == categoria.id,
                            onClick = {
                                selectedCategoryId = categoria.id
                            }
                        )
                    }
                }
            }

            // Contenido principal
            when {

                isLoading -> {

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = ProfileColors.AccentCyan
                        )
                    }
                }

                errorMessage != null -> {

                    CatalogError(
                        message = errorMessage
                            ?: "Error al cargar catálogo",
                        onRetry = cargarCatalogo
                    )
                }

                senasFiltradas.isEmpty() -> {

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron señas registradas",
                            color = ProfileColors.TextSecondary
                        )
                    }
                }

                else -> {

                    LazyColumn(
                        verticalArrangement =
                            Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(
                            bottom = 16.dp
                        )
                    ) {
                        items(
                            items = senasFiltradas,
                            key = { sena -> sena.id }
                        ) { sena ->

                            SenaCatalogItemCard(
                                sena = sena,
                                apiUrl = apiUrl,
                                onPracticeClick = {

                                    viewModel.updateTranslation(
                                        sena.nombre
                                    )

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

// Cabecera y botón de actualización
@Composable
private fun CatalogHeader(
    totalSenas: Int,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Diccionario de Señas",
                color = ProfileColors.TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "$totalSenas señas disponibles",
                color = ProfileColors.TextSecondary,
                fontSize = 12.sp
            )
        }

        IconButton(
            onClick = onRefresh,
            enabled = !isLoading
        ) {
            Icon(
                imageVector = Icons.Rounded.Refresh,
                contentDescription = "Actualizar catálogo",
                tint = ProfileColors.AccentCyan
            )
        }
    }
}

// Campo de búsqueda
@Composable
private fun CatalogSearchField(
    searchQuery: String,
    onSearchChange: (String) -> Unit
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = "Buscar seña o frase...",
                color = ProfileColors.TextSecondary
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = ProfileColors.AccentCyan
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor =
                ProfileColors.CardDark,
            unfocusedContainerColor =
                ProfileColors.CardDark,
            focusedBorderColor =
                ProfileColors.AccentCyan,
            unfocusedBorderColor =
                ProfileColors.CardElevated,
            focusedTextColor =
                ProfileColors.TextPrimary,
            unfocusedTextColor =
                ProfileColors.TextPrimary
        )
    )
}

// Filtro individual de categoría
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
        color = if (selected) {
            ProfileColors.AccentCyan
        } else {
            ProfileColors.CardDark
        },
        border = BorderStroke(
            1.dp,
            if (selected) {
                ProfileColors.AccentCyan
            } else {
                ProfileColors.CardElevated
            }
        )
    ) {
        Text(
            text = label,
            color = if (selected) {
                ProfileColors.BgDark
            } else {
                ProfileColors.TextPrimary
            },
            fontSize = 12.sp,
            fontWeight = if (selected) {
                FontWeight.Bold
            } else {
                FontWeight.Medium
            },
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 8.dp
            )
        )
    }
}

// Estado de error y opción de reintentar
@Composable
private fun CatalogError(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                color = ProfileColors.TextSecondary,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ProfileColors.AccentCyan,
                    contentColor = ProfileColors.BgDark
                )
            ) {
                Text(
                    text = "Reintentar",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}