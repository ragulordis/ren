package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
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

private val QuickNestShapes = Shapes(
  extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
  small = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
  medium = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
  large = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
  extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(26.dp),
)

private val DarkColorScheme =
  darkColorScheme(
    primary = Color(0xFF93C5FD),
    onPrimary = NavyPrimary,
    primaryContainer = NavyPrimary,
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = Color(0xFF60A5FA),
    onSecondary = Color(0xFF0B1726),
    secondaryContainer = Color(0xFF16324F),
    onSecondaryContainer = Color(0xFFEFF6FF),
    tertiary = AccentGold,
    onTertiary = NavyPrimary,
    tertiaryContainer = Color(0xFF332B1A),
    onTertiaryContainer = Color(0xFFFAF5EB),
    background = DarkCanvas,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = IvoryBackground,
    onSurface = IvoryBackground,
    onSurfaceVariant = SlateMutedText,
    outline = DarkCardBorder,
    outlineVariant = Color(0xFF1E3A5F)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = NavyPrimary, // Enterprise Navy #102A43
    onPrimary = SurfaceWhite,
    primaryContainer = BrandPrimaryContainer, // #DBEAFE
    onPrimaryContainer = NavyPrimary,
    secondary = BlueCorporate, // Royal Corporate Blue #2563EB
    onSecondary = SurfaceWhite,
    secondaryContainer = BrandSecondaryContainer, // #EFF6FF
    onSecondaryContainer = BrandOnSecondaryContainer, // #1D4ED8
    tertiary = AccentGold, // Champagne Brass Accent #C9A96E
    onTertiary = CharcoalNavyText,
    tertiaryContainer = Color(0xFFFAF5EB),
    onTertiaryContainer = Color(0xFF8C7335),
    background = IvoryBackground, // Warm Ivory Canvas #F8F7F4
    surface = SurfaceWhite, // Pure White #FFFFFF
    surfaceVariant = SurfaceIvoryTint, // Subtle Warm Tint #F1F0EC
    onBackground = CharcoalNavyText, // Deep Navy-Charcoal Text #172033
    onSurface = CharcoalNavyText,
    onSurfaceVariant = SlateSecondaryText, // Slate Secondary Text #64748B
    outline = CardBorder, // #E8E6DF
    outlineVariant = CardBorderSubtle, // #F0EEE8
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false,
  // Set dynamicColor to false by default to ensure consistent QuickNest brand visuals
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
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
    shapes = QuickNestShapes,
    content = content
  )
}
