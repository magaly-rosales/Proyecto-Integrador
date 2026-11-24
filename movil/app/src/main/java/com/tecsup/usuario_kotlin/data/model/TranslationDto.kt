package com.tecsup.usuario_kotlin.data.model

// Respuesta completa de GET /api/catalogo
data class CatalogoResponse(
    val total: Int,
    val categorias: List<CategoriaCatalogoDto>,
    val senas: List<SenaCatalogoDto>
)

data class CategoriaCatalogoDto(
    val id: Int?,
    val nombre: String,
    val color: String?,
    val icono: String?,
    val imagen: String?,
    val total: Int,
    val hayNuevas: Boolean
)

data class SenaCatalogoDto(
    val id: Int,
    val nombre: String,
    val descripcion: String?,
    val imagen: String?,
    val fecha: String?,
    val nueva: Boolean,
    val idCategoria: Int?,
    val categoria: String?,
    val color: String?,
    val icono: String?,
    val imagenCategoria: String?,
    val video: String?
)

// DTO para enviar los 21 o 42 landmarks de la mano a /api/reconocimiento/puntos
data class PuntoManoDto(
    val x: Float,
    val y: Float,
    val z: Float
)

data class ReconocimientoResponse(
    val sena: String? = null,
    val nombre: String? = null,
    val confianza: Double? = null,
    val mensaje: String? = null
)