package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFFFFF),
    onPrimary = Color(0xFF060814),
    primaryContainer = GlassFillElevated,
    onPrimaryContainer = TextPrimary,
    secondary = Color(0xD9FFFFFF),
    onSecondary = Color(0xFF060814),
    secondaryContainer = GlassFill,
    onSecondaryContainer = TextPrimary,
    tertiary = Color(0x99FFFFFF),
    onTertiary = Color.White,
    background = VividBackdropBase,
    onBackground = TextPrimary,
    surface = GlassFill,
    onSurface = TextPrimary,
    surfaceVariant = GlassFillElevated,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder,
    outlineVariant = GlassBorderSubtle,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun MarklifyTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) = MarklifyTheme(darkTheme, dynamicColor, content)
