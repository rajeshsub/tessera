package io.github.rajeshsub.tessera.feature.vault.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val TesseraPrimary = Color(0xFF1B6E5B)
private val TesseraOnPrimary = Color(0xFFFFFFFF)
private val TesseraSecondary = Color(0xFF4A7C59)
private val TesseraBackground = Color(0xFFF6FBF9)
private val TesseraSurface = Color(0xFFF6FBF9)

private val LightColorScheme =
    lightColorScheme(
        primary = TesseraPrimary,
        onPrimary = TesseraOnPrimary,
        secondary = TesseraSecondary,
        background = TesseraBackground,
        surface = TesseraSurface,
    )

private val DarkColorScheme =
    darkColorScheme(
        primary = Color(0xFF80D9C0),
        onPrimary = Color(0xFF003829),
        secondary = Color(0xFF9FCFB2),
        background = Color(0xFF0E1512),
        surface = Color(0xFF0E1512),
    )

@Composable
fun TesseraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && darkTheme -> dynamicDarkColorScheme(LocalContext.current)
            dynamicColor && !darkTheme -> dynamicLightColorScheme(LocalContext.current)
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
