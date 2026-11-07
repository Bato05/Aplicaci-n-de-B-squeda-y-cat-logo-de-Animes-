package com.example.ghibliapp.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Base de datos local SQLite donde se persisten las películas favoritas.
 *
 * Esta clase SOLO define la estructura de la base:
 *  - El nombre de la tabla y de sus columnas (constantes públicas del companion object).
 *  - Su ciclo de vida: onCreate (crear la tabla) y onUpgrade (cambio de versión).
 *
 * Las operaciones sobre la tabla (insertar, leer, borrar) las hace FilmRepository.
 */
class FilmDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        // Privadas: solo se usan acá, para crear la base.
        private const val DATABASE_NAME = "ghibli_app.db"
        private const val DATABASE_VERSION = 1

        // Públicas: el repositorio las necesita para armar las consultas del CRUD.
        const val TABLE_FAVORITES = "favorites"
        const val COL_ID = "id"
        const val COL_TITLE = "title"
        const val COL_IMAGE = "image"       // Póster que muestra el RecyclerView
        const val COL_RT_SCORE = "rt_score" // Puntaje de Rotten Tomatoes
    }

    /**
     * Se ejecuta UNA sola vez, la primera vez que se abre la base: crea la tabla.
     *
     * Se guardan los datos que muestra la lista de Favoritos (póster, título y puntaje),
     * así se puede consultar sin internet.
     * El id es el mismo que devuelve la API (un texto tipo UUID), por eso es TEXT y no INTEGER.
     * Al ser PRIMARY KEY, no puede haber dos filas con la misma película.
     * image y rt_score no llevan NOT NULL porque la API no los marca como obligatorios.
     */
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_FAVORITES (
                $COL_ID TEXT PRIMARY KEY,
                $COL_TITLE TEXT NOT NULL,
                $COL_IMAGE TEXT,
                $COL_RT_SCORE TEXT
            )
            """.trimIndent()
        )
    }

    /**
     * Se ejecuta cuando sube DATABASE_VERSION (cambió la estructura de la tabla).
     * Estrategia simple: se borra la tabla vieja y se crea de nuevo.
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FAVORITES")
        onCreate(db)
    }
}
