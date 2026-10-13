package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Rozgar Kashmir Theme
 * Designed for maximum accessibility, high contrast, and Kashmir heritage elegance.
 *
 * Uses consistent, verified light surfaces (#FFFFFF), deep pine green accents (#0A3D2A),
 * and dark slate text (#0F172A / #163A2A) to completely prevent Android/browser system
 * dark mode settings from creating unreadable black patches, black-on-dark text, or
 * white-on-white text collisions.
 */
private val ConsistentColorScheme = lightColorScheme(
    primary = KashmirDeepGreen,
    onPrimary = Color.White,
    primaryContainer = KashmirMint,
    onPrimaryContainer = KashmirDeepGreen,
    secondary = KashmirWarmGold,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF78350F),
    background = KashmirBackground,
    surface = Color.White,
    surfaceVariant = KashmirSurfaceVariant,
    onBackground = KashmirTextPrimary,
    onSurface = KashmirTextPrimary,
    onSurfaceVariant = KashmirTextSecondary,
    outline = KashmirCardBorder,
    outlineVariant = Color(0xFFE2E8F0),
    error = KashmirError,
    onError = Color.White
)

@Composable
fun RozgarKashmirTheme(
    darkTheme: Boolean = false, // Enforce consistent, accessible light palette to prevent OS dark mode text corruption
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ConsistentColorScheme,
        typography = Typography,
        content = content
    )
}
