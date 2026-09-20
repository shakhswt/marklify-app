package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = MarklifyBlue,
    onPrimary = Color.White,
    primaryContainer = MarklifyBlueContainerLight,
    onPrimaryContainer = MarklifyBlue,
    secondary = LightTextSecondary,
    onSecondary = Color.White,
    secondaryContainer = LightBorderSubtle,
    onSecondaryContainer = LightTextPrimary,
    tertiary = LightTextMuted,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightCard,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    outlineVariant = LightBorderSubtle,
    error = ErrorRed,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = MarklifyBlueDark,
    onPrimary = Color.White,
    primaryContainer = MarklifyBlueContainerDark,
    onPrimaryContainer = DarkTextPrimary,
    secondary = DarkTextSecondary,
    onSecondary = Color.Black,
    secondaryContainer = DarkBorderSubtle,
    onSecondaryContainer = DarkTextPrimary,
    tertiary = DarkTextMuted,
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorderSubtle,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun MarklifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) = MarklifyTheme(darkTheme, dynamicColor, content)
