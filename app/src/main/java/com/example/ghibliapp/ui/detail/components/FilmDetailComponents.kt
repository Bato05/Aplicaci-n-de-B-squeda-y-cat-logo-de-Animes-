package com.example.ghibliapp.ui.detail.components

import android.widget.ImageView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ghibliapp.R
import com.example.ghibliapp.ui.theme.GhibliAppTheme
import com.squareup.picasso.Picasso

/**
 * Imagen de red cargada con PICASSO dentro de Compose.
 *
 * Picasso trabaja con un ImageView (vista clásica), y Compose no tiene ImageView.
 * AndroidView es el "puente": permite meter una vista tradicional dentro de Compose.
 *  - factory: se ejecuta UNA vez y crea el ImageView.
 *  - update:  se ejecuta al dibujarse y le pide a Picasso que cargue la imagen (igual que en el Adapter).
 *
 * Si imageUrl es null, Picasso muestra directamente la imagen de placeholder.
 * No tiene @Preview: el Preview de Android Studio no tiene internet ni puede iniciar Picasso.
 */
@Composable
fun PicassoImage(imageUrl: String?, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context ->
            ImageView(context).apply {
                scaleType = ImageView.ScaleType.FIT_CENTER // Muestra la imagen completa, sin recortar.
            }
        },
        update = { imageView ->
            Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.ic_film_placeholder)
                .error(R.drawable.ic_film_placeholder)
                .into(imageView)
        },
        modifier = modifier,
    )
}

/**
 * Tarjeta con los datos cortos de la película en una fila: puntaje, año y duración.
 * Recibe los textos ya armados: el componente solo dibuja.
 */
@Composable
fun FilmInfoCard(score: String, year: String, duration: String) {
    // Card: Tarjeta de Material Design 3 con bordes redondeados.
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        // Row: los tres datos uno al lado del otro; weight(1f) les da el mismo ancho a cada uno.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            InfoItem(label = "Puntaje", value = score, modifier = Modifier.weight(1f))
            InfoItem(label = "Año", value = year, modifier = Modifier.weight(1f))
            InfoItem(label = "Duración", value = duration, modifier = Modifier.weight(1f))
        }
    }
}

/** Un dato de la tarjeta: etiqueta arriba y valor en negrita abajo (Column = uno debajo del otro). */
@Composable
private fun InfoItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Text(text = value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    }
}

/** Sección del detalle con título y texto ("Director", "Productor", "Sinopsis"). */
@Composable
fun DetailSection(title: String, text: String) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}

// ---------------- Previews ----------------

@Preview(showBackground = true, name = "Tarjeta de datos")
@Composable
private fun FilmInfoCardPreview() {
    GhibliAppTheme {
        FilmInfoCard(score = "93 / 100", year = "1988", duration = "86 min")
    }
}

@Preview(showBackground = true, name = "Sección de texto")
@Composable
private fun DetailSectionPreview() {
    GhibliAppTheme {
        DetailSection(
            title = "Sinopsis",
            text = "Dos hermanas se mudan al campo con su padre para estar cerca de su madre " +
                "enferma, y descubren los espíritus del bosque que viven cerca de su casa.",
        )
    }
}
