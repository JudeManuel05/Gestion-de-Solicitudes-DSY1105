package cl.duoc.gestionsolicitudes.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AzulPrimario = Color(0xFF1E5AA8)
private val AzulClaro = Color(0xFFD6E3FF)
private val VerdeSecundario = Color(0xFF2E7D5B)

private val LightColors = lightColorScheme(
    primary = AzulPrimario,
    onPrimary = Color.White,
    primaryContainer = AzulClaro,
    onPrimaryContainer = Color(0xFF001B3E),
    secondary = VerdeSecundario,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB5F1D0),
    onSecondaryContainer = Color(0xFF002114)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA9C7FF),
    onPrimary = Color(0xFF003063),
    primaryContainer = Color(0xFF00468C),
    onPrimaryContainer = AzulClaro,
    secondary = Color(0xFF99D5B4),
    secondaryContainer = Color(0xFF0F5137)
)

@Composable
fun GestionSolicitudesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}