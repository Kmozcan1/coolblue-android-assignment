package com.coolblue.assignment.core.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/** Brand colours, as published. */
object CoolblueColors {
    /** Coolblue blue. Only 3.2:1 against light paper, so in light mode it marks the focused search field and nothing else. */
    val Blue = Color(0xFF0090E3)

    /** The image CDN renders every product on pure white. The tile matches, so the photo's edge disappears. */
    val Plate = Color(0xFFFFFFFF)
}

// Light palette. OKLCH, tinted toward the brand hue (245°): paper at 97.5 %, ink at 21 %.
// Blue, orange and green are darkened until they pass 4.5:1 as text on paper.
internal val LightColors = lightColorScheme(
    primary = Color(0xFF006DC3),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFFC84000),
    tertiary = Color(0xFF007900),
    background = Color(0xFFF4F7FB),
    onBackground = Color(0xFF111920),
    surface = Color(0xFFF4F7FB),
    onSurface = Color(0xFF111920),
    surfaceVariant = Color(0xFFE9EDF2),
    onSurfaceVariant = Color(0xFF535C65),
    surfaceContainerLowest = Color(0xFFF4F7FB),
    surfaceContainerLow = Color(0xFFF4F7FB),
    surfaceContainer = Color(0xFFE9EDF2),
    surfaceContainerHigh = Color(0xFFDAE0E6),
    surfaceContainerHighest = Color(0xFFDAE0E6),
    outline = Color(0xFFD2D8DE),
    outlineVariant = Color(0xFFD2D8DE),
)

// Dark palette: paper at 16 %, ink at 94 %. Accents lighten and lose chroma; the brand blue reads as text here.
internal val DarkColors = darkColorScheme(
    primary = CoolblueColors.Blue,
    onPrimary = Color(0xFF090E12),
    secondary = Color(0xFFFF9358),
    tertiary = Color(0xFF7CD775),
    background = Color(0xFF090E12),
    onBackground = Color(0xFFE7ECF0),
    surface = Color(0xFF090E12),
    onSurface = Color(0xFFE7ECF0),
    surfaceVariant = Color(0xFF11171C),
    onSurfaceVariant = Color(0xFF98A0A7),
    surfaceContainerLowest = Color(0xFF090E12),
    surfaceContainerLow = Color(0xFF11171C),
    surfaceContainer = Color(0xFF11171C),
    surfaceContainerHigh = Color(0xFF1C2228),
    surfaceContainerHighest = Color(0xFF1C2228),
    outline = Color(0xFF272C31),
    outlineVariant = Color(0xFF272C31),
)
