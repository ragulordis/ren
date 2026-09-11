package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Premium Cool Cyber-Luxe Theme - Electric Cyan & Radiant Indigo
val BrandPrimary = Color(0xFF0284C7) // Electric Oceanic Sky
val BrandPrimaryDark = Color(0xFF38BDF8) // High-contrast Ice Cyan
val BrandPrimaryContainer = Color(0xFFE0F2FE)
val BrandOnPrimaryContainer = Color(0xFF0369A1)
val BrandSecondary = Color(0xFF6366F1) // Vibrant Radiant Indigo
val BrandSecondaryContainer = Color(0xFFEEF2FF)
val BrandOnSecondaryContainer = Color(0xFF3730A3)
val BrandTertiary = Color(0xFF06B6D4) // Cool Aquamarine

// Speed & Urgency Colors (Neon Coral Flame & Deep Obsidian Cyber Card)
val UrgencyFlame = Color(0xFFF97316)
val UrgencyFlameContainer = Color(0xFFFFEDD5)
val UrgencyDarkCard = Color(0xFF0F172A)
val UrgencyDarkBorder = Color(0xFF334155)
val FastSaleAmber = Color(0xFFF59E0B)
val FastSaleAmberContainer = Color(0xFFFEF3C7)
val NormalGreen = Color(0xFF10B981)
val NormalGreenContainer = Color(0xFFD1FAE5)
val PrivateSaleDark = Color(0xFF0F172A)

// Verification & Trust
val VerifiedGreen = Color(0xFF10B981)
val VerifiedGreenContainer = Color(0xFFD1FAE5)
val TrustGold = Color(0xFFF59E0B)

// Premium Cool Light Neutral Canvas
val BackgroundLight = Color(0xFFF8FAFC)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFF1F5F9)
val CardBorder = Color(0xFFE2E8F0)
val CardBorderSubtle = Color(0xFFEEF2F6)
val TextPrimary = Color(0xFF0F172A)
val TextSecondary = Color(0xFF475569)
val TextMuted = Color(0xFF94A3B8)

// Dark Cool Obsidian Canvas
val DarkCanvas = Color(0xFF0A0F1D)
val DarkSurface = Color(0xFF111827)
val DarkSurfaceVariant = Color(0xFF1E293B)
val DarkCardBorder = Color(0xFF334155)
val DarkBorderGlow = Color(0x3338BDF8)

// Cool Gradient Brushes for Visual Polish
val CoolHeroGradient = Brush.horizontalGradient(
    listOf(Color(0xFF0284C7), Color(0xFF6366F1))
)
val CoolCyanGradient = Brush.linearGradient(
    listOf(Color(0xFF06B6D4), Color(0xFF0284C7))
)
val CoolDarkCardGradient = Brush.verticalGradient(
    listOf(Color(0xFF1E293B), Color(0xFF0F172A))
)
val CoolGlassmorphicBorder = Brush.linearGradient(
    listOf(Color(0x8038BDF8), Color(0x306366F1), Color(0x6038BDF8))
)
val FlameGlowGradient = Brush.horizontalGradient(
    listOf(Color(0xFFF97316), Color(0xFFEF4444))
)
val CoolPillGradient = Brush.horizontalGradient(
    listOf(Color(0xFF0284C7).copy(alpha = 0.12f), Color(0xFF6366F1).copy(alpha = 0.12f))
)

