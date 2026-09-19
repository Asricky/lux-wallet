package com.luxwallet.app.core.ui.theme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val LuxGold = Color(0xFFD8B66A)
val LuxInk = Color(0xFF141413)
private val light = lightColorScheme(
    primary = Color(0xFF76571D), onPrimary = Color.White,
    primaryContainer = Color(0xFFF2E5C5), onPrimaryContainer = LuxInk,
    secondary = Color(0xFF72644A), onSecondary = Color.White,
    secondaryContainer = Color(0xFFECE3D0), onSecondaryContainer = LuxInk,
    background = Color(0xFFF6F4EF), onBackground = LuxInk,
    surface = Color(0xFFFFFDF8), onSurface = LuxInk,
    surfaceVariant = Color(0xFFEFECE4), onSurfaceVariant = Color(0xFF656057),
    outline = Color(0xFF827A6D), outlineVariant = Color(0xFFDED8CB)
)
private val dark = darkColorScheme(
    primary = LuxGold, onPrimary = LuxInk,
    primaryContainer = Color(0xFF3B3220), onPrimaryContainer = Color(0xFFF3DFC0),
    secondary = LuxGold, onSecondary = LuxInk,
    secondaryContainer = Color(0xFF353023), onSecondaryContainer = LuxGold,
    background = Color(0xFF101110), onBackground = Color(0xFFF5F1E8),
    surface = Color(0xFF191A18), onSurface = Color(0xFFF5F1E8),
    surfaceVariant = Color(0xFF232420), onSurfaceVariant = Color(0xFFBBB7AC),
    outline = Color(0xFF8D8779), outlineVariant = Color(0xFF393A33)
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
