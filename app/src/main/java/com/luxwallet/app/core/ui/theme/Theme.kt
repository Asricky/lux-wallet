package com.luxwallet.app.core.ui.theme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val LuxGold = Color(0xFFF5B942)
val LuxInk = Color(0xFF16324F)
private val light = lightColorScheme(
    primary = Color(0xFF087A78), onPrimary = Color.White,
    primaryContainer = Color(0xFFDDF8F6), onPrimaryContainer = LuxInk,
    secondary = Color(0xFF087A78), onSecondary = Color.White,
    secondaryContainer = Color(0xFFEAFBF7), onSecondaryContainer = LuxInk,
    background = Color(0xFFF7FAFC), onBackground = LuxInk,
    surface = Color(0xFFFFFFFF), onSurface = LuxInk,
    surfaceVariant = Color(0xFFEDF5F6), onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFF748A91), outlineVariant = Color(0xFFDCE7EC)
)
private val dark = darkColorScheme(
    primary = Color(0xFF4FD1C5), onPrimary = LuxInk,
    primaryContainer = Color(0xFF12413F), onPrimaryContainer = Color(0xFFDDF8F6),
    secondary = Color(0xFF4FD1C5), onSecondary = LuxInk,
    secondaryContainer = Color(0xFF233F3D), onSecondaryContainer = LuxGold,
    background = Color(0xFF0D1A22), onBackground = Color(0xFFEAF4F6),
    surface = Color(0xFF142731), onSurface = Color(0xFFEAF4F6),
    surfaceVariant = Color(0xFF20353F), onSurfaceVariant = Color(0xFFB1C7CE),
    outline = Color(0xFF859FA8), outlineVariant = Color(0xFF334C57)
)
enum class LuxThemePreference { SYSTEM, LIGHT, DARK }
@Composable fun LuxWalletTheme(themePreference: LuxThemePreference = LuxThemePreference.SYSTEM, content: @Composable () -> Unit) {
    val useDark = when (themePreference) {
        LuxThemePreference.SYSTEM -> isSystemInDarkTheme()
        LuxThemePreference.LIGHT -> false
        LuxThemePreference.DARK -> true
    }
    CompositionLocalProvider(LocalLuxSemanticColors provides if (useDark) DarkSemanticColors else LightSemanticColors) {
        MaterialTheme(colorScheme = if (useDark) dark else light, typography = LuxTypography,
            shapes = Shapes(small = RoundedCornerShape(12.dp), medium = RoundedCornerShape(20.dp), large = RoundedCornerShape(28.dp)), content = content)
    }
}
