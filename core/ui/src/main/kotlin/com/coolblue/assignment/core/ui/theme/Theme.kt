package com.coolblue.assignment.core.ui.theme

// Design notes. Neutrals are tinted toward the brand blue (OKLCH hue 245°) so paper, text and rules
// belong to the same family; the brand colours themselves stay as published, with text-safe variants
// where they fail WCAG. Two typefaces: the device's condensed sans for the price and the idle headline,
// the default sans for everything else. No illustration, no decoration: the product photos carry the screen.

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

// Dynamic colour is off on purpose: the brand colours should show.
// `secondary` is the merchandising orange; `tertiary` doubles as the success colour (delivery promise).
@Composable
fun CoolblueTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = CoolblueTypography,
        content = content,
    )
}
