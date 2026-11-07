package com.example.ghibliapp.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ghibliapp.data.model.FilmModel
import com.example.ghibliapp.ui.detail.components.DetailSection
import com.example.ghibliapp.ui.detail.components.FilmInfoCard
import com.example.ghibliapp.ui.detail.components.PicassoImage
import com.example.ghibliapp.ui.viewmodel.DetailViewModel

/**
 * Pantalla de detalle: conecta el ViewModel con la UI.
 *
 * collectAsState(): se suscribe en tiempo real a cada StateFlow del ViewModel.
 * 'by': delegado de Kotlin que extrae el valor directamente en la variable.
 * Cada vez que el ViewModel cambia un valor, Compose vuelve a dibujar (recomposición).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilmDetailScreen(viewModel: DetailViewModel) {
    val film by viewModel.film.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Scaffold: Estructura base de Material Design 3 (TopBar + contenido).
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de la película") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
    ) { innerPadding -> // innerPadding: espacio que deja el Scaffold para no tapar la TopBar.
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        // Según el estado del ViewModel se dibuja una cosa u otra.
        // Se copian a variables locales porque Kotlin no permite usar directamente como
        // "no null" una variable obtenida con 'by' (no puede asegurar que no cambie).
        val currentFilm = film
        val currentError = errorMessage
        if (isLoading) {
            // Box: centra el texto en toda la pantalla.
            Box(modifier = contentModifier, contentAlignment = Alignment.Center) {
                Text(text = "Cargando…", style = MaterialTheme.typography.bodyLarge)
            }
        } else if (currentError != null) {
            ErrorContent(
                message = currentError,
                onRetryClick = { viewModel.retry() },
                modifier = contentModifier,
            )
        } else if (currentFilm != null) {
            FilmDetailContent(film = currentFilm, modifier = contentModifier)
        }
    }
}

/** Mensaje de error + botón para reintentar. */
@Composable
private fun ErrorContent(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Column: el mensaje arriba y el botón abajo, centrados.
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge)
        Button(onClick = onRetryClick, modifier = Modifier.padding(top = 16.dp)) {
            Text("Reintentar")
        }
    }
}

/**
 * Datos de la película: imagen grande, título, puntaje, año, duración, director, productor
 * y sinopsis completa.
 *
 * LazyColumn: lista con desplazamiento vertical (el reemplazo de RecyclerView en Compose).
 * Acá se usa para poder hacer scroll cuando la sinopsis es larga. Cada item { } es un bloque
 * de la pantalla (en el ejemplo del docente se usa items(...) porque se recorre una lista).
 */
@Composable
private fun FilmDetailContent(film: FilmModel, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // 1. Imagen grande (movie_banner) cargada con Picasso. Si no viene, se usa el póster.
        item {
            PicassoImage(
                imageUrl = film.movieBanner ?: film.image,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(top = 16.dp),
            )
        }

        // 2. Título.
        item {
            Text(
                text = film.title,
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }

        // 3. Tarjeta con puntaje, año de estreno y duración.
        item {
            FilmInfoCard(
                score = if (film.rtScore != null) "${film.rtScore} / 100" else "Sin puntaje",
                year = film.releaseDate,
                duration = if (film.runningTime != null) "${film.runningTime} min" else "Desconocida",
            )
        }

        // 4. Director y productor.
        item { DetailSection(title = "Director", text = film.director) }
        item { DetailSection(title = "Productor", text = film.producer) }

        // 5. Sinopsis completa (description).
        item { DetailSection(title = "Sinopsis", text = film.description) }
    }
}
