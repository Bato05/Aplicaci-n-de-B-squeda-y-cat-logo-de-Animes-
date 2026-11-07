package com.example.ghibliapp.data.remote

import com.example.ghibliapp.data.model.FilmModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Endpoints de Studio Ghibli API que usa la app.
 * Retrofit genera la implementación de esta interfaz en tiempo de ejecución.
 */
interface ApiService {

    /**
     * GET https://ghibliapi.vercel.app/films
     * Devuelve TODAS las películas (no hay paginación) como un array JSON: [ {...}, {...} ]
     * Por eso la respuesta es directamente una List<FilmModel>.
     */
    @GET("films")
    suspend fun getFilms(): Response<List<FilmModel>>

    /**
     * GET https://ghibliapi.vercel.app/films/{id}
     * Devuelve UNA película como objeto JSON: { ... }
     * @Path reemplaza {id} en la URL por el id recibido (un texto tipo UUID).
     */
    @GET("films/{id}")
    suspend fun getFilmById(@Path("id") id: String): Response<FilmModel>
}
