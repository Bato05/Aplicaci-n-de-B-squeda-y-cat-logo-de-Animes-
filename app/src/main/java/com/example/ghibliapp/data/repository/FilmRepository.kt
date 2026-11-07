package com.example.ghibliapp.data.repository

import android.content.ContentValues
import android.content.Context
import com.example.ghibliapp.data.local.FilmDbHelper
import com.example.ghibliapp.data.model.FilmModel
import com.example.ghibliapp.data.remote.ApiService
import com.example.ghibliapp.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

/**
 * Patrón Repositorio: ÚNICA puerta de entrada a los datos para los ViewModels.
 *
 * Los ViewModels no saben si los datos vienen de internet (Retrofit) o de la base local
 * (SQLite): solo le piden cosas al repositorio.
 *  - Remoto: delega en ApiService (Retrofit).
 *  - Local: acá vive el CRUD de favoritos. FilmDbHelper solo define la tabla; las
 *    consultas (INSERT, SELECT, DELETE) se arman en esta clase.
 *
 * Todas las operaciones se ejecutan con withContext(Dispatchers.IO), así la red y la
 * base de datos nunca bloquean la UI.
 */
class FilmRepository(context: Context) {

    private val api: ApiService = RetrofitClient.apiService

    // El context llega desde el ViewModel (AndroidViewModel) y se lo pasamos al helper.
    private val dbHelper = FilmDbHelper(context.applicationContext)

    // ---------- Remoto (API) ----------

    suspend fun getFilms(): Response<List<FilmModel>> =
        withContext(Dispatchers.IO) { api.getFilms() }

    suspend fun getFilmDetail(id: String): Response<FilmModel> =
        withContext(Dispatchers.IO) { api.getFilmById(id) }

    // ---------- Local (SQLite) - CRUD de favoritos ----------

    /**
     * INSERT de la película en la tabla de favoritos.
     * ContentValues arma los pares columna -> valor que se van a insertar.
     * db.insert devuelve el id de la fila insertada, o -1 si falló.
     *
     * No hace falta pisar filas existentes: el MainViewModel primero consulta isFavorite()
     * y solo llama a este método si la película NO está guardada.
     */
    suspend fun addFavorite(film: FilmModel): Boolean = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put(FilmDbHelper.COL_ID, film.id)
            put(FilmDbHelper.COL_TITLE, film.title)
            put(FilmDbHelper.COL_IMAGE, film.image)
            put(FilmDbHelper.COL_RT_SCORE, film.rtScore)
        }
        val newRowId = dbHelper.writableDatabase.insert(FilmDbHelper.TABLE_FAVORITES, null, values)
        newRowId != -1L
    }

    /**
     * SELECT * de los favoritos, ordenados por título.
     * El cursor recorre la tabla fila por fila (moveToNext) y con cada fila se arma un FilmModel.
     */
    suspend fun getFavorites(): List<FilmModel> = withContext(Dispatchers.IO) {
        val favorites = mutableListOf<FilmModel>()
        dbHelper.readableDatabase.query(
            FilmDbHelper.TABLE_FAVORITES, null, null, null, null, null,
            "${FilmDbHelper.COL_TITLE} ASC"
        ).use { cursor ->
            // Índice (posición) de cada columna dentro del cursor.
            val idIndex = cursor.getColumnIndexOrThrow(FilmDbHelper.COL_ID)
            val titleIndex = cursor.getColumnIndexOrThrow(FilmDbHelper.COL_TITLE)
            val imageIndex = cursor.getColumnIndexOrThrow(FilmDbHelper.COL_IMAGE)
            val scoreIndex = cursor.getColumnIndexOrThrow(FilmDbHelper.COL_RT_SCORE)

            while (cursor.moveToNext()) {
                favorites.add(
                    FilmModel(
                        id = cursor.getString(idIndex),
                        title = cursor.getString(titleIndex),
                        // getString devuelve null si la celda está vacía (NULL).
                        image = cursor.getString(imageIndex),
                        rtScore = cursor.getString(scoreIndex),
                        // La lista de Favoritos no muestra estos datos: no se guardan en la tabla.
                        movieBanner = null,
                        description = "",
                        director = "",
                        producer = "",
                        releaseDate = "",
                        runningTime = null,
                        isFavorite = true
                    )
                )
            }
        }
        favorites
    }

    /** SELECT por id: ¿esta película está guardada en favoritos? */
    suspend fun isFavorite(id: String): Boolean = withContext(Dispatchers.IO) {
        dbHelper.readableDatabase.query(
            FilmDbHelper.TABLE_FAVORITES, arrayOf(FilmDbHelper.COL_ID),
            "${FilmDbHelper.COL_ID} = ?", arrayOf(id),
            null, null, null
        ).use { cursor -> cursor.moveToFirst() }
    }

    /** DELETE por id. Devuelve true si se borró al menos una fila. */
    suspend fun removeFavorite(id: String): Boolean = withContext(Dispatchers.IO) {
        dbHelper.writableDatabase.delete(
            FilmDbHelper.TABLE_FAVORITES, "${FilmDbHelper.COL_ID} = ?", arrayOf(id)
        ) > 0
    }
}
