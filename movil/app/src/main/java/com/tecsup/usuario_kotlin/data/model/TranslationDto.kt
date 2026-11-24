package com.tecsup.usuario_kotlin.data.model

import com.google.gson.annotations.SerializedName

data class SenaDto(
    val id: Int,
    val nombre: String,
    val categoria: CategoriaDto?, // Si el backend devuelve solo el texto, cámbialo a String?
    val descripcion: String?,
    @SerializedName("video_url") // Ajustaremos este nombre exacto cuando revisemos el backend
    val videoUrl: String?,
    val entrenado: Boolean = false
)

data class CategoriaDto(
    val id: Int,
    val nombre: String
)