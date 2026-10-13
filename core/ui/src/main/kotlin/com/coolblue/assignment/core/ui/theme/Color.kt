package com.coolblue.assignment.core.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object CoolblueColors {
    val Blue = Color(0xFF0090E3)

    val PhotoBackground = Color(0xFFFFFFFF)
}

private val LightPaper = Color(0xFFF4F7FB)
private val LightInk = Color(0xFF111920)
private val DarkPaper = Color(0xFF090E12)
private val DarkInk = Color(0xFFE7ECF0)

private val TextSafeBlue = Color(0xFF006DC3)
private val TextSafeOrange = Color(0xFFC84000)
private val TextSafeGreen = Color(0xFF007900)

internal val LightColors = lightColorScheme(
    primary = TextSafeBlue,
    onPrimary = Color(0xFFFFFFFF),
    secondary = TextSafeOrange,
    tertiary = TextSafeGreen,
    background = LightPaper,
    onBackground = LightInk,
    surface = LightPaper,
    onSurface = LightInk,
    surfaceVariant = Color(0xFFE9EDF2),
    onSurfaceVariant = Color(0xFF535C65),
    surfaceContainerLowest = LightPaper,
    surfaceContainerLow = LightPaper,
    surfaceContainer = Color(0xFFE9EDF2),
    surfaceContainerHigh = Color(0xFFDAE0E6),
    surfaceContainerHighest = Color(0xFFDAE0E6),
    outline = Color(0xFFD2D8DE),
    outlineVariant = Color(0xFFD2D8DE),
    inverseSurface = DarkPaper,
    inverseOnSurface = DarkInk,
    inversePrimary = CoolblueColors.Blue,
)

internal val DarkColors = darkColorScheme(
    primary = CoolblueColors.Blue,
    onPrimary = DarkPaper,
    secondary = Color(0xFFFF9358),
    tertiary = Color(0xFF7CD775),
    background = DarkPaper,
    onBackground = DarkInk,
    surface = DarkPaper,
    onSurface = DarkInk,
    surfaceVariant = Color(0xFF11171C),
    onSurfaceVariant = Color(0xFF98A0A7),
    surfaceContainerLowest = DarkPaper,
    surfaceContainerLow = Color(0xFF11171C),
    surfaceContainer = Color(0xFF11171C),
    surfaceContainerHigh = Color(0xFF1C2228),
    surfaceContainerHighest = Color(0xFF1C2228),
    outline = Color(0xFF272C31),
    outlineVariant = Color(0xFF272C31),
    inverseSurface = LightPaper,
    inverseOnSurface = LightInk,
    inversePrimary = TextSafeBlue,
)
