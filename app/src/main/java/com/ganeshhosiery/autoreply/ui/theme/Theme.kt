package com.ganeshhosiery.autoreply.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = DeepBlue,
    onPrimary = SurfaceWhite,
    primaryContainer = DeepBlueLight,
    onPrimaryContainer = SurfaceWhite,
    secondary = Saffron,
    onSecondary = SurfaceWhite,
    secondaryContainer = SaffronLight,
    background = Cream,
    onBackground = TextDark,
    surface = SurfaceWhite,
    onSurface = TextDark,
    error = ErrorRed
)

private val DarkColors = darkColorScheme(
    primary = SaffronLight,
    onPrimary = Color4(),
    secondary = Saffron,
    background = Color4(0xFF14181F),
    surface = Color4(0xFF1E232C),
    error = Color4(0xFFCF6679)
)

private fun Color4(value: Long = 0xFF000000) = androidx.compose.ui.graphics.Color(value)

@Composable
fun GaneshHosieryTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (useDarkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content
    )
}
