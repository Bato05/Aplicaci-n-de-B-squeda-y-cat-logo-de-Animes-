package com.example.ghibliapp.ui.detail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ghibliapp.ui.theme.GhibliAppTheme
import com.example.ghibliapp.ui.viewmodel.DetailViewModel

/**
 * Pantalla de detalle, desarrollada 100% en Jetpack Compose.
 *
 * No usa XML: en lugar de setContentView(layout) se usa setContent { }, y adentro se
 * llama al tema y a la función @Composable de la pantalla.
 *
 * Recibe solamente el id de la película en el Intent; los datos los pide el DetailViewModel.
 */
class DetailActivity : ComponentActivity() {

    companion object {
        /** Clave del extra del Intent. La usan los Fragments al abrir esta pantalla. */
        const val EXTRA_FILM_ID = "extra_film_id"
    }

    private val viewModel: DetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Primera apertura: pedir la película. (Al rotar, el ViewModel ya la tiene cargada.)
        if (savedInstanceState == null) {
            // getStringExtra devuelve null si el extra no vino: en ese caso se usa "".
            val filmId = intent.getStringExtra(EXTRA_FILM_ID) ?: ""
            viewModel.loadFilm(filmId)
        }

        setContent {
            GhibliAppTheme {
                FilmDetailScreen(viewModel = viewModel)
            }
        }
    }
}
