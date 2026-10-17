package com.autospanish.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Indigo = Color(0xFF4F46E5)
private val IndigoDark = Color(0xFF6366F1)
private val Amber = Color(0xFFF59E0B)

private val LightColors = lightColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3E1FC),
    onPrimaryContainer = Color(0xFF1B1464),
    secondary = Amber,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE8C2),
    onSecondaryContainer = Color(0xFF4A2E00),
    tertiary = Color(0xFF0EA5A5),
    onTertiary = Color.White,
    error = Color(0xFFDC2626),
    onError = Color.White,
    errorContainer = Color(0xFFFBD5D5),
    onErrorContainer = Color(0xFF5F1414),
    background = Color(0xFFF7F7FB),
    onBackground = Color(0xFF1C1C24),
    surface = Color.White,
    onSurface = Color(0xFF1C1C24),
    surfaceVariant = Color(0xFFEDEDF7),
    onSurfaceVariant = Color(0xFF4B4B57),
    outline = Color(0xFFC7C7D6),
    outlineVariant = Color(0xFFDEDEEA),
)

private val DarkColors = darkColorScheme(
    primary = IndigoDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF373082),
    onPrimaryContainer = Color(0xFFE3E1FC),
    secondary = Amber,
    onSecondary = Color(0xFF3A2400),
    secondaryContainer = Color(0xFF5C3D00),
    onSecondaryContainer = Color(0xFFFFE8C2),
    tertiary = Color(0xFF5EEAD4),
    onTertiary = Color(0xFF00382F),
    error = Color(0xFFEF5350),
    onError = Color(0xFF3A0A0A),
    errorContainer = Color(0xFF6B1A1A),
    onErrorContainer = Color(0xFFFBD5D5),
    background = Color(0xFF121218),
    onBackground = Color(0xFFE7E7EF),
    surface = Color(0xFF1C1C24),
    onSurface = Color(0xFFE7E7EF),
    surfaceVariant = Color(0xFF2A2A35),
    onSurfaceVariant = Color(0xFFC7C7D6),
    outline = Color(0xFF4B4B57),
    outlineVariant = Color(0xFF34343F),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

private val AppTypography = Typography(
    headlineSmall = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium),
)

@Composable
fun AutoSpanishTheme(darkModeOverride: Int = 0, content: @Composable () -> Unit) {
    val useDark = when (darkModeOverride) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (useDark) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
