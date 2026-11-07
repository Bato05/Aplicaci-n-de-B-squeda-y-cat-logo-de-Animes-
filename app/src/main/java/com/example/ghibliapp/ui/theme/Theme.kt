package com.example.ghibliapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/** Esquema de colores para el modo oscuro (Dark Theme). */
private val DarkColorScheme = darkColorScheme(
    primary = GhibliGreenLight,
    primaryContainer = GhibliGreenDarkContainer,
    secondaryContainer = GhibliGreenDarkContainer,
)

/** Esquema de colores para el modo claro (Light Theme). */
private val LightColorScheme = lightColorScheme(
    primary = GhibliGreen,
    primaryContainer = GhibliGreenContainer,
    secondaryContainer = GhibliGreenContainer,
)

/**
 * Tema de la pantalla de detalle (Compose).
 *
 * No se usan los colores dinámicos de Android 12+ (dynamicColor) para que el detalle
 * mantenga el mismo azul que las pantallas XML, sin importar el fondo de pantalla del usuario.
 */
@Composable
fun GhibliAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
