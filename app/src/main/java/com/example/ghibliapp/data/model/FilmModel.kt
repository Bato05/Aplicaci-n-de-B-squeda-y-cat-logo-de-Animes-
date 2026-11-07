package com.example.ghibliapp.data.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo de una película de Studio Ghibli. Se usa en las tres capas:
 *  - Retrofit/Gson lo llena desde el JSON de la API.
 *  - FilmRepository lo guarda y lo lee de la tabla SQLite "favorites" (CRUD).
 *  - El Adapter (XML) y la pantalla de detalle (Compose) lo muestran.
 *
 * La API devuelve cada película así (se muestran solo los campos que usa la app):
 *
 * {
 *   "id": "58611129-2dbc-4a81-a72f-77ddfc1b1b49",
 *   "title": "My Neighbor Totoro",
 *   "image": "https://...jpg",          -> póster (lista)
 *   "movie_banner": "https://...jpg",   -> imagen grande (detalle)
 *   "description": "Two sisters move to the country...",
 *   "director": "Hayao Miyazaki",
 *   "producer": "Hayao Miyazaki",
 *   "release_date": "1988",
 *   "running_time": "86",
 *   "rt_score": "93"
 * }
 *
 * Todos los valores llegan como texto (incluso el año, la duración y el puntaje), por eso
 * todas las propiedades son String. Los campos que el JSON trae y este modelo no declara
 * (people, species, locations, vehicles, url...) Gson simplemente los ignora.
 *
 * Los campos que la documentación de la API no marca como obligatorios son nullables (String?).
 *
 * [isFavorite] NO viene de la API: lo calcula el MainViewModel cruzando la lista de la API
 * con los favoritos guardados en la base local. Está marcado @Transient para que Gson lo ignore.
 */
data class FilmModel(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("image") val image: String?,
    @SerializedName("movie_banner") val movieBanner: String?,
    @SerializedName("description") val description: String,
    @SerializedName("director") val director: String,
    @SerializedName("producer") val producer: String,
    @SerializedName("release_date") val releaseDate: String,
    @SerializedName("running_time") val runningTime: String?,
    @SerializedName("rt_score") val rtScore: String?,
    @Transient val isFavorite: Boolean = false
)
