package com.breatheonline.breathe.ui.theme

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
    val isLight: Boolean = false,
    // Second hue of the breathing orb gradient — pairs with [primary]
    val orbSecondary: Color = Color(0xFF818CF8),
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
    orbSecondary  = Color(0xFF818CF8),   // indigo
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
    orbSecondary  = Color(0xFF22D3EE),   // cyan
)

// ── Forest theme ──────────────────────────────────────────────────────────────

val ForestThemeColors = AppColors(
    background    = Color(0xFF08120D),
    surface       = Color(0xFF122018),
    primary       = Color(0xFF2D6A4F),
    onPrimary     = Color(0xFFE6F0E9),
    title         = Color(0xFFE7F2EA),
    subtitle      = Color(0xFF9BB7A6),
    label         = Color(0xFF9BB7A6),
    value         = Color(0xFFD7E8DB),
    text          = Color(0xFFD7E8DB),
    glowOuter     = Color(0xFF4C8B6B).copy(alpha = 0.30f),
    glowInner     = Color(0xFF4C8B6B).copy(alpha = 0.62f),
    glowBackground = Color(0xFF4C8B6B).copy(alpha = 0.08f),
    orbSecondary  = Color(0xFF86EFAC),   // spring green
)

// ── Sunset theme ──────────────────────────────────────────────────────────────

val SunsetThemeColors = AppColors(
    background    = Color(0xFF16100C),
    surface       = Color(0xFF241914),
    primary       = Color(0xFFC46A4A),
    onPrimary     = Color(0xFFFFF3EA),
    title         = Color(0xFFFFF3EA),
    subtitle      = Color(0xFFD8A58B),
    label         = Color(0xFFD8A58B),
    value         = Color(0xFFF7DDD0),
    text          = Color(0xFFF7DDD0),
    glowOuter     = Color(0xFFD78C6A).copy(alpha = 0.30f),
    glowInner     = Color(0xFFD78C6A).copy(alpha = 0.60f),
    glowBackground = Color(0xFFD78C6A).copy(alpha = 0.08f),
    orbSecondary  = Color(0xFFF472B6),   // dusk pink
)

// ── Day (light) theme ─────────────────────────────────────────────────────────

val DayThemeColors = AppColors(
    background     = Color(0xFFF5F7FA),
    surface        = Color(0xFFFFFFFF),
    primary        = Color(0xFF0D9488),   // teal-600
    onPrimary      = Color(0xFFFFFFFF),
    title          = Color(0xFF111827),   // gray-900
    subtitle       = Color(0xFF6B7280),   // gray-500
    label          = Color(0xFF6B7280),
    value          = Color(0xFF0D9488),
    text           = Color(0xFF374151),   // gray-700
    glowOuter      = Color(0xFF0D9488).copy(alpha = 0.18f),
    glowInner      = Color(0xFF0D9488).copy(alpha = 0.45f),
    glowBackground = Color(0xFF0D9488).copy(alpha = 0.06f),
    isLight        = true,
    orbSecondary   = Color(0xFF38BDF8),   // sky
)

// ── CompositionLocal — screens read current theme without passing params ──────

val LocalAppColors = staticCompositionLocalOf<AppColors> { DefaultDarkColors }

// ── Sleep stage palette ───────────────────────────────────────────────────────

val SleepDeep   = Color(0xFF3840E1)
val SleepLight  = Color(0xFF0E8BF5)
val SleepRem    = Color(0xFF21C6FF)
val SleepAwake  = Color(0xFFB7E6FF)
val SleepAccent = Color(0xFF7C4DFF)
