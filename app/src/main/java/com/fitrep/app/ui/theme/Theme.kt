package com.fitrep.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object RepColors {
    val Background = Color(0xFF090C0A)
    val Surface = Color(0xFF121813)
    val SurfaceHigh = Color(0xFF1A221B)
    val SurfaceSoft = Color(0xFF151C16)
    val Lime = Color(0xFFC8FF4D)
    val Mint = Color(0xFF57E0A0)
    val Cyan = Color(0xFF6BE9FF)
    val Coral = Color(0xFFFF7A5C)
    val Violet = Color(0xFF9E7BFF)
    val TextPrimary = Color(0xFFF2F6EE)
    val TextSecondary = Color(0xFFA5B0A6)
    val TextMuted = Color(0xFF6F7C71)
    val Outline = Color(0xFF26312A)
    val Danger = Color(0xFFFF5C6C)
    val Gold = Color(0xFFFFC94D)
}

private val RepColorScheme = darkColorScheme(
    primary = RepColors.Lime,
    onPrimary = Color(0xFF0B1400),
    primaryContainer = Color(0xFF2A3A12),
    onPrimaryContainer = RepColors.Lime,
    secondary = RepColors.Cyan,
    onSecondary = Color(0xFF001820),
    tertiary = RepColors.Coral,
    background = RepColors.Background,
    onBackground = RepColors.TextPrimary,
    surface = RepColors.Surface,
    onSurface = RepColors.TextPrimary,
    surfaceVariant = RepColors.SurfaceHigh,
    onSurfaceVariant = RepColors.TextSecondary,
    outline = RepColors.Outline,
    outlineVariant = Color(0xFF1D261F),
    error = RepColors.Danger,
    onError = Color(0xFF2B0005),
)

private val RepTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 52.sp,
        lineHeight = 54.sp,
        letterSpacing = (-1.5).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 38.sp,
        lineHeight = 42.sp,
        letterSpacing = (-1).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.6).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.3).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp,
        lineHeight = 24.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 21.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.2.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.8.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 13.sp,
        letterSpacing = 0.6.sp,
    ),
)

private val RepShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp),
)

@Composable
fun RepTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RepColorScheme,
        typography = RepTypography,
        shapes = RepShapes,
        content = content,
    )
}
