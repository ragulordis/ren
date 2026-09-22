package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// Architectural Shape Language (10dp-12dp components, 18dp-22dp cards, 26dp hero surfaces)
val RenShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(26.dp),
)

private val DarkColorScheme = darkColorScheme(
    primary = RenGold,
    onPrimary = RenDeepNavy,
    primaryContainer = RenDarkNavy,
    onPrimaryContainer = RenDarkTextPrimary,
    secondary = RenSoftNavy,
    onSecondary = RenDarkTextPrimary,
    secondaryContainer = RenDarkSurfaceElevated,
    onSecondaryContainer = RenDarkTextPrimary,
    tertiary = RenGold,
    onTertiary = RenDeepNavy,
    tertiaryContainer = Color(0xFF262016),
    onTertiaryContainer = RenGold,
    background = RenDarkBackground,       // #0B1220
    surface = RenDarkSurface,             // #121C2D
    surfaceVariant = RenDarkSurfaceElevated,
    onBackground = RenDarkTextPrimary,    // #F5F2EA
    onSurface = RenDarkTextPrimary,
    onSurfaceVariant = RenDarkTextSecondary, // #AAB3C2
    outline = RenDarkBorder,              // #1E2D44
    outlineVariant = Color(0xFF162438),
    error = RenError,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = RenPrimaryNavy,             // #10233F
    onPrimary = RenSurfaceWhite,
    primaryContainer = RenSecondaryIvory, // #F2EFE8
    onPrimaryContainer = RenPrimaryNavy,
    secondary = RenSoftNavy,              // #243B5A
    onSecondary = RenSurfaceWhite,
    secondaryContainer = RenSecondaryIvory,
    onSecondaryContainer = RenPrimaryNavy,
    tertiary = RenGold,                   // #C9A96E
    onTertiary = RenPrimaryNavy,
    tertiaryContainer = RenGoldSurface,
    onTertiaryContainer = RenPrimaryNavy,
    background = RenIvory,                // #F8F6F1
    surface = RenSurfaceWhite,            // #FFFFFF
    surfaceVariant = RenSecondaryIvory,   // #F2EFE8
    onBackground = RenTextPrimary,        // #172033
    onSurface = RenTextPrimary,
    onSurfaceVariant = RenTextSecondary,  // #687386
    outline = RenBorder,                  // #E4E0D8
    outlineVariant = RenBorderSubtle,
    error = RenError,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = RenShapes,
        content = content
    )
}
