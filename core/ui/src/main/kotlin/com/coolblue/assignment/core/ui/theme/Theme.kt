package com.coolblue.assignment.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Dynamic colour is off on purpose: the brand colours should show.
// `tertiary` doubles as the success colour (delivery promise); Material 3 has no success role.
private val LightColors = lightColorScheme(
    primary = CoolblueColors.Blue,
    onPrimary = Color.White,
    secondary = CoolblueColors.Orange,
    onSecondary = Color.White,
    secondaryContainer = CoolblueColors.OrangeLight,
    onSecondaryContainer = CoolblueColors.Orange,
    tertiary = CoolblueColors.Green,
)

private val DarkColors = darkColorScheme(
    primary = CoolblueColors.Blue,
    onPrimary = Color.White,
    secondary = CoolblueColors.Orange,
    onSecondary = Color.White,
    secondaryContainer = CoolblueColors.Orange,
    onSecondaryContainer = Color.White,
    tertiary = CoolblueColors.GreenLight,
)

@Composable
fun CoolblueTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
