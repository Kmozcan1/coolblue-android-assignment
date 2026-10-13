package com.coolblue.assignment.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val DeviceCondensedSans = FontFamily(Font(DeviceFontFamilyName("sans-serif-condensed"), FontWeight.Bold))

private const val TABULAR_FIGURES = "tnum"

private val PriceStyle = TextStyle(
    fontFamily = DeviceCondensedSans,
    fontWeight = FontWeight.Bold,
    fontSize = 26.sp,
    lineHeight = 28.sp,
    letterSpacing = (-0.25).sp,
    fontFeatureSettings = TABULAR_FIGURES,
)

private val SheetProductNameStyle = TextStyle(fontWeight = FontWeight.Medium, fontSize = 20.sp, lineHeight = 26.sp, letterSpacing = 0.sp)

private val ProductNameStyle = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = 0.sp)

private val SearchFieldStyle = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = 0.sp)

private val PromoKickerStyle = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.2.sp)

internal val CoolblueTypography = Typography(
    headlineSmall = PriceStyle,
    titleLarge = SheetProductNameStyle,
    titleMedium = ProductNameStyle,
    bodyLarge = SearchFieldStyle,
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.sp),
    bodySmall = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.sp),
    labelMedium = PromoKickerStyle,
)
