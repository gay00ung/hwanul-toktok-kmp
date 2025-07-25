package net.ifmain.hwanultoktok.kmp.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Primary40,
    onPrimary = Color.White,
    primaryContainer = Primary80.copy(alpha = 0.3f),
    onPrimaryContainer = DeepBlue,

    secondary = Secondary40,
    onSecondary = Color.White,
    secondaryContainer = Secondary80.copy(alpha = 0.3f),
    onSecondaryContainer = Color(0xFF8B0000),

    tertiary = MintGreen,
    onTertiary = Color.Black,
    tertiaryContainer = MintGreen.copy(alpha = 0.2f),
    onTertiaryContainer = Color(0xFF004D40),

    background = Gray100,
    onBackground = Color.Black,

    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Gray200,
    onSurfaceVariant = Color(0xFF666666),

    error = Color(0xFFE53E3E),
    onError = Color.White,
    errorContainer = Color(0xFFFED7D7),
    onErrorContainer = Color(0xFF9B2C2C)
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color.Black,
    primaryContainer = Primary40.copy(alpha = 0.3f),
    onPrimaryContainer = Color.White,

    secondary = Secondary80,
    onSecondary = Color.Black,
    secondaryContainer = Secondary40.copy(alpha = 0.3f),
    onSecondaryContainer = Color.White,

    tertiary = NeonBlue,
    onTertiary = Color.Black,
    tertiaryContainer = NeonBlue.copy(alpha = 0.2f),
    onTertiaryContainer = Color.White,

    background = DarkBackground,
    onBackground = Color.White,

    surface = DarkSurface,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFB0B0B0),

    error = Color(0xFFEF5350),
    onError = Color.Black,
    errorContainer = Color(0xFF8B0000),
    onErrorContainer = Color(0xFFFFCDD2)
)

@Composable
fun HwanulTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}