package com.sathsara.ecbtracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    outline = BorderDark,
    primary = CyanPrimary,
    secondary = GreenSuccess,
    tertiary = PurpleAccent,
    error = RedDanger,
    onPrimary = Color(0xFF07090F),
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSub,
)

private val LightColorScheme = lightColorScheme(
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    outline = BorderLight,
    primary = CyanPrimary,
    secondary = GreenSuccess,
    tertiary = PurpleAccent,
    error = RedDanger,
    onPrimary = Color(0xFF07090F),
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSubLight,
)

val EcbShapes = Shapes(
    small = RoundedCornerShape(6.dp),    // Chips
    medium = RoundedCornerShape(8.dp),   // Buttons
    large = RoundedCornerShape(12.dp),   // Cards
)

@Composable
fun EcbTrackerTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = EcbShapes,
        content = content
    )
}
