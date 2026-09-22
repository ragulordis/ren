package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// =========================================================================
// REN — IVORY & NAVY LUXURY REAL ESTATE DESIGN SYSTEM
// =========================================================================

// Foundation Neutrals (Light / Ivory)
val RenIvory = Color(0xFFF8F6F1)           // Primary Ivory Canvas #F8F6F1
val RenSecondaryIvory = Color(0xFFF2EFE8)  // Secondary Tonal Ivory #F2EFE8
val RenSurfaceWhite = Color(0xFFFFFFFF)    // Pure White Card & Sheet Surface #FFFFFF
val RenBorder = Color(0xFFE4E0D8)          // Architectural Border #E4E0D8
val RenBorderSubtle = Color(0xFFEFECE5)    // Hairline Subtle Border

// Foundation Navies
val RenPrimaryNavy = Color(0xFF10233F)     // Dominant Brand Navy #10233F
val RenDeepNavy = Color(0xFF08182D)        // Deep Architectural Navy #08182D
val RenSoftNavy = Color(0xFF243B5A)        // Soft Slate Navy Accent #243B5A

// Foundation Typography
val RenTextPrimary = Color(0xFF172033)     // High-Contrast Primary Text #172033
val RenTextSecondary = Color(0xFF687386)   // Subtle Readable Secondary Text #687386
val RenTextMuted = Color(0xFF9BA3AF)       // Placeholder / Caption Text

// Luxury Accent (Restrained, subtle gold accent)
val RenGold = Color(0xFFC9A96E)            // Champagne Luxury Accent #C9A96E
val RenGoldSurface = Color(0xFFFAF7F0)     // Soft Gold Tint Surface
val RenGoldBorder = Color(0xFFDFD1B3)

// Semantic Accents
val RenSuccess = Color(0xFF397A5A)         // Calm Forest Emerald #397A5A
val RenSuccessSurface = Color(0xFFEEF6F2)
val RenError = Color(0xFFB94A48)           // Rich Brick Crimson #B94A48
val RenErrorSurface = Color(0xFFFDF2F2)
val RenWarning = Color(0xFFA87932)         // Warm Cognac Warning #A87932
val RenWarningSurface = Color(0xFFFDF8EE)

// Dark Luxury Foundation
val RenDarkBackground = Color(0xFF0B1220)  // Deep Midnight Canvas #0B1220
val RenDarkSurface = Color(0xFF121C2D)     // Layered Midnight Surface #121C2D
val RenDarkSurfaceElevated = Color(0xFF18253B)
val RenDarkNavy = Color(0xFF182B47)        // Dark Primary Navy #182B47
val RenDarkTextPrimary = Color(0xFFF5F2EA) // Warm Ivory Text #F5F2EA
val RenDarkTextSecondary = Color(0xFFAAB3C2)// Silver Slate Secondary #AAB3C2
val RenDarkBorder = Color(0xFF1E2D44)

// =========================================================================
// BACKWARD COMPATIBILITY ALIASES (Guarantees zero architectural regressions)
// =========================================================================
val NavyPrimary = RenPrimaryNavy
val BlueCorporate = RenPrimaryNavy // Use primary navy for buttons and accents to eliminate noisy royal blue
val AccentGold = RenGold
val IvoryBackground = RenIvory
val SurfaceWhite = RenSurfaceWhite
val SurfaceIvoryTint = RenSecondaryIvory
val CharcoalNavyText = RenTextPrimary
val SlateSecondaryText = RenTextSecondary
val SlatePrimaryText = RenTextPrimary
val SlateMutedText = RenTextMuted
val EmeraldVerify = RenSuccess

val BrandPrimary = RenPrimaryNavy
val BrandPrimaryDark = Color(0xFF93B5E0)
val BrandPrimaryContainer = RenSecondaryIvory
val BrandOnPrimaryContainer = RenPrimaryNavy
val BrandSecondary = RenSoftNavy
val BrandSecondaryContainer = RenSecondaryIvory
val BrandOnSecondaryContainer = RenPrimaryNavy
val BrandTertiary = RenGold

val UrgencyFlame = RenWarning
val UrgencyFlameContainer = RenWarningSurface
val UrgencyDarkCard = RenDeepNavy
val UrgencyDarkBorder = RenSoftNavy
val FastSaleAmber = RenGold
val FastSaleAmberContainer = RenGoldSurface
val NormalGreen = RenSuccess
val NormalGreenContainer = RenSuccessSurface
val PrivateSaleDark = RenDeepNavy

val VerifiedGreen = RenSuccess
val VerifiedGreenContainer = RenSuccessSurface
val TrustGold = RenGold
val TrustGoldContainer = RenGoldSurface

val BackgroundLight = RenIvory
val SurfaceLight = RenSurfaceWhite
val SurfaceVariantLight = RenSecondaryIvory
val CardBorder = RenBorder
val CardBorderSubtle = RenBorderSubtle
val TextPrimary = RenTextPrimary
val TextSecondary = RenTextSecondary
val TextMuted = RenTextMuted

val DarkCanvas = RenDarkBackground
val DarkSurface = RenDarkSurface
val DarkSurfaceVariant = RenDarkSurfaceElevated
val DarkCardBorder = RenDarkBorder
val DarkBorderGlow = Color(0x33C9A96E)

// Gradient Brushes (Architectural & Refined)
val CoolHeroGradient = Brush.verticalGradient(
    listOf(RenDeepNavy, RenPrimaryNavy)
)
val CoolCyanGradient = Brush.horizontalGradient(
    listOf(RenPrimaryNavy, RenSoftNavy)
)
val CoolDarkCardGradient = Brush.verticalGradient(
    listOf(RenDarkSurfaceElevated, RenDarkSurface)
)
val CoolGlassmorphicBorder = Brush.linearGradient(
    listOf(RenGold.copy(alpha = 0.4f), RenBorder)
)
val FlameGlowGradient = Brush.horizontalGradient(
    listOf(RenGold, RenWarning)
)
val CoolPillGradient = Brush.horizontalGradient(
    listOf(RenPrimaryNavy.copy(alpha = 0.08f), RenSoftNavy.copy(alpha = 0.04f))
)
val GoldAccentGradient = Brush.horizontalGradient(
    listOf(RenGold, Color(0xFFDFBA73))
)
val LuxuryNavyPillGradient = Brush.horizontalGradient(
    listOf(RenPrimaryNavy, RenDeepNavy)
)
