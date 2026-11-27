package com.tecsup.usuario_kotlin.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel as composeViewModel
import com.tecsup.usuario_kotlin.data.model.CategoriaCatalogoDto
import com.tecsup.usuario_kotlin.data.model.SenaCatalogoDto
import com.tecsup.usuario_kotlin.data.remote.RetrofitClient
import com.tecsup.usuario_kotlin.data.repository.PracticeProgressRepository
import com.tecsup.usuario_kotlin.ui.home.components.SenaCatalogItemCard
import com.tecsup.usuario_kotlin.ui.theme.ProfileColors
import com.tecsup.usuario_kotlin.ui.translation.SenasBottomBar
import com.tecsup.usuario_kotlin.viewmodel.PracticeViewModel
import com.tecsup.usuario_kotlin.viewmodel.TranslationViewModel
import kotlinx.coroutines.CancellationException

@Composable
fun HomeScreen(
    username: String,
    userId: Int,
    onNavigateToTranslator: () -> Unit,
    onNavigateToPerfil: () -> Unit = {},
    viewModel: TranslationViewModel
) {
    val context = LocalContext.current

    // Repositorio de progreso del usuario autenticado
    val progressRepository = remember(
        context.applicationContext,
        userId
    ) {
        PracticeProgressRepository(
            context.applicationContext,
            userId
        )
    }

    val progress by progressRepository.progress
        .collectAsStateWithLifecycle()

    // Navegación hacia el progreso
    var showProgress by rememberSaveable(userId) {
        mutableStateOf(false)
    }

    // Estados del catálogo
    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    var categorias by remember {
        mutableStateOf<List<CategoriaCatalogoDto>>(
            emptyList()
        )
    }

    var senas by remember {
        mutableStateOf<List<SenaCatalogoDto>>(
            emptyList()
        )
    }

    // Estados de búsqueda y filtrado
    var selectedCategoryId by rememberSaveable {
        mutableStateOf<Int?>(null)
    }

    var searchQuery by rememberSaveable {
        mutableStateOf("")
    }

    // Seña seleccionada
    var selectedSenaId by rememberSaveable {
        mutableStateOf<Int?>(null)
    }

    // Navegación hacia la práctica
    var showPractice by rememberSaveable {
        mutableStateOf(false)
    }

    // Posición del desplazamiento
    val catalogListState = rememberLazyListState()
    val categoryListState = rememberLazyListState()

    // Dirección del servidor
    var apiUrl by remember {
        mutableStateOf<String?>(null)
    }

    // Actualización del catálogo
    var refreshVersion by remember {
        mutableIntStateOf(0)
    }

    val cargarCatalogo: () -> Unit = {
        if (!isLoading) {
            refreshVersion++
        }
    }

    // Cargar catálogo desde Spring Boot
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

                // Dirección real de la petición
                apiUrl = response.raw()
                    .request
                    .url
                    .toString()

                // Verificar categoría seleccionada
                if (
                    selectedCategoryId != null &&
                    categorias.none {
                        it.id == selectedCategoryId
                    }
                ) {
                    selectedCategoryId = null
                }

                // Verificar seña seleccionada
                if (
                    selectedSenaId != null &&
                    senas.none {
                        it.id == selectedSenaId
                    }
                ) {
                    selectedSenaId = null
                    showPractice = false
                }

                if (selectedSenaId == null) {
                    showPractice = false
                }

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

    // Pantalla de progreso
    if (showProgress) {
        val catalogIds = if (
            !isLoading && errorMessage == null
        ) {
            senas.map { it.id }.toSet()
        } else {
            null
        }

        TrainingProgressScreen(
            progress = progress,
            catalogIds = catalogIds,
            onBackClick = {
                showProgress = false
            }
        )

        return
    }

    // Obtener información de la seña seleccionada
    val selectedSena = remember(
        senas,
        selectedSenaId
    ) {
        senas.firstOrNull {
            it.id == selectedSenaId
        }
    }

    // Navegación interna del catálogo
    if (
        selectedSena != null &&
        !isLoading &&
        errorMessage == null
    ) {
        if (showPractice) {

            // ViewModel de práctica
            val practiceViewModel: PracticeViewModel =
                composeViewModel()

            // Registrar el éxito vinculado a la seña actual
            DisposableEffect(
                practiceViewModel,
                progressRepository,
                selectedSena.id,
                selectedSena.nombre
            ) {
                practiceViewModel.setOnPracticeCompleted {
                        nombreReconocido ->

                    if (
                        nombreReconocido ==
                        selectedSena.nombre.trim()
                    ) {
                        progressRepository.registrarExito(
                            senaId = selectedSena.id,
                            nombreSena = selectedSena.nombre
                        )
                    }
                }

                onDispose {
                    practiceViewModel.setOnPracticeCompleted(
                        null
                    )
                }
            }

            // Pantalla de práctica existente
            SenaPracticeScreen(
                sena = selectedSena,
                apiUrl = apiUrl,
                practiceViewModel = practiceViewModel,
                onBackClick = {
                    showPractice = false
                }
            )

        } else {
            // Detalles de la seña
            SenaDetailScreen(
                sena = selectedSena,
                apiUrl = apiUrl,

                onBackClick = {
                    showPractice = false
                    selectedSenaId = null
                },

                onPracticeClick = {
                    showPractice = true
                }
            )
        }

        return
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
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {
            // Cabecera del catálogo
            CatalogHeader(
                totalSenas = senasFiltradas.size,
                isLoading = isLoading,
                onRefresh = cargarCatalogo
            )

            // Acceso al progreso del usuario
            OutlinedButton(
                onClick = {
                    showProgress = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    1.dp,
                    ProfileColors.AccentCyan.copy(alpha = 0.6f)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = ProfileColors.CardDark,
                    contentColor = ProfileColors.AccentCyan
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "Mi progreso",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "${progress.size} señas",
                    color = ProfileColors.TextSecondary,
                    fontSize = 12.sp
                )
            }

            // Buscador
            CatalogSearchField(
                searchQuery = searchQuery,
                onSearchChange = {
                    searchQuery = it
                }
            )

            // Filtros de categorías
            if (categorias.isNotEmpty()) {
                LazyRow(
                    state = categoryListState,
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        CategoryChip(
                            label = "Todas",
                            selected =
                                selectedCategoryId == null,
                            onClick = {
                                selectedCategoryId = null
                            }
                        )
                    }

                    items(categorias) { categoria ->
                        CategoryChip(
                            label =
                                "${categoria.nombre} (${categoria.total})",
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
                        state = catalogListState,
                        verticalArrangement =
                            Arrangement.spacedBy(10.dp),
                        contentPadding =
                            PaddingValues(bottom = 16.dp)
                    ) {
                        items(
                            items = senasFiltradas,
                            key = { sena -> sena.id }
                        ) { sena ->

                            SenaCatalogItemCard(
                                sena = sena,
                                apiUrl = apiUrl,

                                onPracticeClick = {
                                    showPractice = false
                                    selectedSenaId = sena.id
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Cabecera del catálogo
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
        Column(
            modifier = Modifier.weight(1f)
        ) {
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
            focusedContainerColor = ProfileColors.CardDark,
            unfocusedContainerColor = ProfileColors.CardDark,
            focusedBorderColor = ProfileColors.AccentCyan,
            unfocusedBorderColor = ProfileColors.CardElevated,
            focusedTextColor = ProfileColors.TextPrimary,
            unfocusedTextColor = ProfileColors.TextPrimary
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

// Estado de error del catálogo
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
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                color = ProfileColors.TextSecondary,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(10.dp))

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