package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Ivory & Navy: Professional Real Estate Enterprise Palette
// Trustworthy, Professional, Corporate, Clean, Established
val NavyPrimary = Color(0xFF102A43) // Deep Corporate Enterprise Navy
val BlueCorporate = Color(0xFF2563EB) // Vibrant Royal Tech Blue
val AccentGold = Color(0xFFC9A96E) // Warm Gold / Champagne Brass Accent
val IvoryBackground = Color(0xFFF8F7F4) // Warm Ivory Background Canvas
val SurfaceWhite = Color(0xFFFFFFFF) // Pure White Surface
val SurfaceIvoryTint = Color(0xFFF1F0EC) // Subtle Warm Ivory Surface Variant
val CharcoalNavyText = Color(0xFF172033) // High-contrast Deep Navy-Charcoal Text
val SlateSecondaryText = Color(0xFF64748B) // Clean Slate Secondary Text
val SlateMutedText = Color(0xFF94A3B8) // Muted Placeholder/Caption Text

// Semantic Brand Tokens
val BrandPrimary = NavyPrimary // Enterprise Navy
val BrandPrimaryDark = Color(0xFF93C5FD)
val BrandPrimaryContainer = Color(0xFFDBEAFE) // Soft Corporate Blue Tint
val BrandOnPrimaryContainer = Color(0xFF102A43)
val BrandSecondary = BlueCorporate // Royal Blue
val BrandSecondaryContainer = Color(0xFFEFF6FF)
val BrandOnSecondaryContainer = Color(0xFF1D4ED8)
val BrandTertiary = AccentGold // Champagne Gold Accent

// Speed & High-End Card Accents (Subtle, Established & Corporate)
val UrgencyFlame = Color(0xFFD97706) // Rich Corporate Amber
val UrgencyFlameContainer = Color(0xFFFEF3C7)
val UrgencyDarkCard = Color(0xFF102A43) // Enterprise Navy High-Priority Card
val UrgencyDarkBorder = Color(0xFF244466)
val FastSaleAmber = AccentGold
val FastSaleAmberContainer = Color(0xFFFAF5EB)
val NormalGreen = Color(0xFF059669)
val NormalGreenContainer = Color(0xFFECFDF5)
val PrivateSaleDark = Color(0xFF102A43)

// Verification & Trust Badges
val VerifiedGreen = Color(0xFF059669)
val VerifiedGreenContainer = Color(0xFFECFDF5)
val TrustGold = AccentGold
val TrustGoldContainer = Color(0xFFFAF5EB)

// Canvas, Surface & Borders
val BackgroundLight = IvoryBackground
val SurfaceLight = SurfaceWhite
val SurfaceVariantLight = SurfaceIvoryTint
val CardBorder = Color(0xFFE8E6DF) // Clean Subtle Ivory/Stone Border
val CardBorderSubtle = Color(0xFFF0EEE8)
val TextPrimary = CharcoalNavyText
val TextSecondary = SlateSecondaryText
val TextMuted = SlateMutedText

// Dark Enterprise Canvas
val DarkCanvas = Color(0xFF0B1726)
val DarkSurface = Color(0xFF102A43)
val DarkSurfaceVariant = Color(0xFF16324F)
val DarkCardBorder = Color(0xFF244466)
val DarkBorderGlow = Color(0x332563EB)

// Enterprise Gradient Brushes
val CoolHeroGradient = Brush.horizontalGradient(
    listOf(Color(0xFF102A43), Color(0xFF1E3A5F))
)
val CoolCyanGradient = Brush.linearGradient(
    listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))
)
val CoolDarkCardGradient = Brush.verticalGradient(
    listOf(Color(0xFF16324F), Color(0xFF102A43))
)
val CoolGlassmorphicBorder = Brush.linearGradient(
    listOf(Color(0x60C9A96E), Color(0x302563EB), Color(0x50102A43))
)
val FlameGlowGradient = Brush.horizontalGradient(
    listOf(Color(0xFFC9A96E), Color(0xFFD97706))
)
val CoolPillGradient = Brush.horizontalGradient(
    listOf(Color(0xFF102A43).copy(alpha = 0.08f), Color(0xFF2563EB).copy(alpha = 0.08f))
)
val GoldAccentGradient = Brush.horizontalGradient(
    listOf(Color(0xFFC9A96E), Color(0xFFDFBA73))
)


