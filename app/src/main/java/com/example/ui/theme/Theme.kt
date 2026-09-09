package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val IslamicDarkColorScheme = darkColorScheme(
    primary = EmeraldLight,
    onPrimary = Color(0xFF003732),
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = OnEmeraldContainer,
    secondary = GoldAccent,
    onSecondary = Color(0xFF3F2E00),
    secondaryContainer = GoldContainer,
    onSecondaryContainer = OnGoldContainer,
    tertiary = EmeraldPrimary,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = DarkSurfaceBorder,
    outlineVariant = GoldBorder,
    error = RedAlert,
    onError = Color.White
)

@Composable
fun KarachiNamazTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = IslamicDarkColorScheme,
        typography = Typography,
        content = content
    )
}
