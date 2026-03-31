package com.example.breathe.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ── Custom app-specific color roles ──────────────────────────────────────────

data class AppColors(
    val background: Color,
    val surface: Color,
    val primary: Color,
    val onPrimary: Color,
    val title: Color,
    val subtitle: Color,
    val label: Color,
    val value: Color,
    val text: Color,
    val glowOuter: Color,
    val glowInner: Color,
    val glowBackground: Color,
)

// ── Shared palette tokens ─────────────────────────────────────────────────────

// Neutrals — calm charcoal base (replaces heavy navy)
val Charcoal950 = Color(0xFF0B0C10)   // deepest background
val Charcoal900 = Color(0xFF131519)   // default surface
val Charcoal800 = Color(0xFF1C1F26)   // surface variant / card
val Charcoal700 = Color(0xFF252A35)   // elevated surface
val Slate400    = Color(0xFF8B9DB8)   // muted text / labels  (was #94B4C8 — slightly warmer)
val Slate300    = Color(0xFFCBD5E1)   // body text
val White90     = Color(0xFFECF0FA)   // titles / headings

// Keep legacy aliases so other files don't break
val Navy950   = Charcoal950
val Navy900   = Charcoal900
val Navy800   = Charcoal800
val Navy700   = Charcoal700
val Cream100  = White90

// Teal / mint — Default dark theme primary (breathing circle color)
val Teal300   = Color(0xFF5EEAD4)
val Teal200   = Color(0xFF99F6E4)
val Teal900   = Color(0xFF002419)

// Blue — Ocean theme primary
val Blue400   = Color(0xFF3A82F7)
val Blue200   = Color(0xFF70B8FF)
val Blue900   = Color(0xFF10182F)

// Error
val ErrorRed  = Color(0xFFFF6B6B)
val ErrorDark = Color(0xFF0B0C10)

// ── Default dark theme ───────────────────────────────────────────────────────

val DefaultDarkColors = AppColors(
    background    = Charcoal950,
    surface       = Charcoal900,
    primary       = Teal300,
    onPrimary     = Teal900,
    title         = White90,
    subtitle      = Slate400,
    label         = Slate400,
    value         = Teal300,
    text          = Slate300,
    glowOuter     = Teal300.copy(alpha = 0.28f),
    glowInner     = Teal300.copy(alpha = 0.65f),
    glowBackground = Teal300.copy(alpha = 0.08f),
)

// ── Ocean theme ───────────────────────────────────────────────────────────────

val OceanThemeColors = AppColors(
    background    = Color(0xFF080E1C),
    surface       = Color(0xFF111E3A),
    primary       = Blue400,
    onPrimary     = Blue900,
    title         = Blue200,
    subtitle      = Color(0xFF88AACC),
    label         = Color(0xFF88AACC),
    value         = Blue200,
    text          = Color.White,
    glowOuter     = Blue200.copy(alpha = 0.35f),
    glowInner     = Color(0xFF65A8FF).copy(alpha = 0.70f),
    glowBackground = Color(0xFF65A8FF).copy(alpha = 0.10f),
)

// ── Forest theme ──────────────────────────────────────────────────────────────

val ForestThemeColors = AppColors(
    background    = Color(0xFF0B1A11),
    surface       = Color(0xFF152B1C),
    primary       = Color(0xFF4CAF50),
    onPrimary     = Color(0xFF1A2F1F),
    title         = Color(0xFFE8F5E9),
    subtitle      = Color(0xFFA5D6A7),
    label         = Color(0xFFA5D6A7),
    value         = Color(0xFFE8F5E9),
    text          = Color(0xFFE8F5E9),
    glowOuter     = Color(0xFF4CAF50).copy(alpha = 0.35f),
    glowInner     = Color(0xFF4CAF50).copy(alpha = 0.70f),
    glowBackground = Color(0xFF4CAF50).copy(alpha = 0.10f),
)

// ── Sunset theme ──────────────────────────────────────────────────────────────

val SunsetThemeColors = AppColors(
    background    = Color(0xFF150D05),
    surface       = Color(0xFF221508),
    primary       = Color(0xFFFF5722),
    onPrimary     = Color(0xFF3F2A1D),
    title         = Color(0xFFFFF8E1),
    subtitle      = Color(0xFFFFB74D),
    label         = Color(0xFFFFB74D),
    value         = Color(0xFFFFF8E1),
    text          = Color(0xFFFFF8E1),
    glowOuter     = Color(0xFFFF5722).copy(alpha = 0.35f),
    glowInner     = Color(0xFFFF5722).copy(alpha = 0.70f),
    glowBackground = Color(0xFFFF5722).copy(alpha = 0.10f),
)

// ── CompositionLocal — screens read current theme without passing params ──────

val LocalAppColors = staticCompositionLocalOf<AppColors> { DefaultDarkColors }
