package com.example.ghibliapp.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton (object) que construye UNA sola instancia de Retrofit para toda la app.
 * "by lazy" hace que se cree recién la primera vez que alguien usa apiService.
 */
object RetrofitClient {

    private const val BASE_URL = "https://ghibliapi.vercel.app/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)                                  // Todas las rutas parten de esta URL
            .addConverterFactory(GsonConverterFactory.create()) // JSON -> objetos Kotlin con Gson
            .build()
            .create(ApiService::class.java)                     // Implementación de ApiService
    }
}
