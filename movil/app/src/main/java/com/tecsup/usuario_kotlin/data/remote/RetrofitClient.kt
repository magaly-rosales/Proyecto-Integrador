package com.tecsup.usuario_kotlin.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Emulador: "http://10.0.2.2:8081/"
    // Celular físico (SM-A075M): reemplaza por tu IPv4 (ej. "http://10.200.168.XX:8081/")
    private const val BASE_URL = "http://10.200.173.171:8081/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}