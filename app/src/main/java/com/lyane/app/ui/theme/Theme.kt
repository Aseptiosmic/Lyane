package com.lyane.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = LyaneCyan,
    onPrimary = LyaneBlack,
    primaryContainer = LyaneCardSurface,
    onPrimaryContainer = LyaneCyanGlow,
    secondary = LyanePurple,
    onSecondary = TextPrimary,
    secondaryContainer = LyaneDarkSurface,
    onSecondaryContainer = LyanePurple,
    tertiary = LyaneMagenta,
    onTertiary = TextPrimary,
    background = LyaneBlack,
    onBackground = TextPrimary,
    surface = LyaneDarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = LyaneCardSurface,
    onSurfaceVariant = TextSecondary,
    outline = LyaneBorder
)

@Composable
fun LyaneTheme(
    darkTheme: Boolean = true, // Lyane is always deep dark by design
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = LyaneTypography,
        content = content
    )
}
