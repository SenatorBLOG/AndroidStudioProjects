package com.breatheonline.breathe.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.breatheonline.breathe.R

// Montserrat — bundled locally; same clean geometric warmth as Nunito / Inter,
// works offline and ships at the same size as a downloadable-font setup.
val MontserratFontFamily = FontFamily(
    Font(R.font.montserrat_thin,              FontWeight.Thin),
    Font(R.font.montserrat_thinitalic,        FontWeight.Thin,       FontStyle.Italic),
    Font(R.font.montserrat_extralight,        FontWeight.ExtraLight),
    Font(R.font.montserrat_extralightitalic,  FontWeight.ExtraLight,  FontStyle.Italic),
    Font(R.font.montserrat_light,             FontWeight.Light),
    Font(R.font.montserrat_lightitalic,       FontWeight.Light,       FontStyle.Italic),
    Font(R.font.montserrat_regular,           FontWeight.Normal),
    Font(R.font.montserrat_italic,            FontWeight.Normal,      FontStyle.Italic),
    Font(R.font.montserrat_medium,            FontWeight.Medium),
    Font(R.font.montserrat_mediumitalic,      FontWeight.Medium,      FontStyle.Italic),
    Font(R.font.montserrat_semibold,          FontWeight.SemiBold),
    Font(R.font.montserrat_semibolditalic,    FontWeight.SemiBold,    FontStyle.Italic),
    Font(R.font.montserrat_bold,              FontWeight.Bold),
    Font(R.font.montserrat_bolditalic,        FontWeight.Bold,        FontStyle.Italic),
    Font(R.font.montserrat_extrabold,         FontWeight.ExtraBold),
    Font(R.font.montserrat_extrabolditalic,   FontWeight.ExtraBold,   FontStyle.Italic),
    Font(R.font.montserrat_black,             FontWeight.Black),
    Font(R.font.montserrat_blackitalic,       FontWeight.Black,       FontStyle.Italic),
)

val AppTypography = Typography(

    // ── Display ───────────────────────────────────────────────────────────────
    // Large hero moments (splash screens, onboarding)
    displaySmall = TextStyle(
        fontFamily   = MontserratFontFamily,
        fontWeight   = FontWeight.Light,
        fontSize     = 36.sp,
        lineHeight   = 44.sp,
        letterSpacing = (-0.5).sp,
    ),

    // ── Headlines ─────────────────────────────────────────────────────────────
    // Primary screen title  e.g. "Breathe Better"
    headlineLarge = TextStyle(
        fontFamily   = MontserratFontFamily,
        fontWeight   = FontWeight.Medium,
        fontSize     = 28.sp,
        lineHeight   = 34.sp,
        letterSpacing = (-0.25).sp,
    ),
    // Section heading  e.g. "Hello, Mikhail!"
    headlineMedium = TextStyle(
        fontFamily   = MontserratFontFamily,
        fontWeight   = FontWeight.Medium,
        fontSize     = 22.sp,
        lineHeight   = 28.sp,
    ),
    // Sub-section heading  e.g. card titles in Profile
    headlineSmall = TextStyle(
        fontFamily   = MontserratFontFamily,
        fontWeight   = FontWeight.Medium,
        fontSize     = 18.sp,
        lineHeight   = 24.sp,
    ),

    // ── Titles ────────────────────────────────────────────────────────────────
    // Prominent labels / bottom-sheet titles
    titleLarge = TextStyle(
        fontFamily   = MontserratFontFamily,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 18.sp,
        lineHeight   = 24.sp,
    ),
    // Subtitle below main heading  e.g. "Meditation for Sleep & Relaxation"
    titleMedium = TextStyle(
        fontFamily   = MontserratFontFamily,
        fontWeight   = FontWeight.Normal,
        fontSize     = 15.sp,
        lineHeight   = 22.sp,
        letterSpacing = 0.1.sp,
    ),
    // Dialog / sheet title  e.g. "Select Duration"
    titleSmall = TextStyle(
        fontFamily   = MontserratFontFamily,
        fontWeight   = FontWeight.Medium,
        fontSize     = 14.sp,
        lineHeight   = 20.sp,
    ),

    // ── Body ──────────────────────────────────────────────────────────────────
    // Breathing phase label inside the animated circle  e.g. "Inhale"
    bodyLarge = TextStyle(
        fontFamily   = MontserratFontFamily,
        fontWeight   = FontWeight.Medium,
        fontSize     = 32.sp,
        lineHeight   = 36.sp,
    ),
    // Setting values, dropdown text, stat numbers  e.g. "10 min"
    bodyMedium = TextStyle(
        fontFamily   = MontserratFontFamily,
        fontWeight   = FontWeight.W500,
        fontSize     = 15.sp,
        lineHeight   = 22.sp,
    ),
    // Supporting / secondary copy  e.g. stat labels in Profile
    bodySmall = TextStyle(
        fontFamily   = MontserratFontFamily,
        fontWeight   = FontWeight.Normal,
        fontSize     = 13.sp,
        lineHeight   = 18.sp,
        letterSpacing = 0.25.sp,
    ),

    // ── Labels ────────────────────────────────────────────────────────────────
    // CTA / primary button text  e.g. "Start", "Save Goals"
    labelLarge = TextStyle(
        fontFamily   = MontserratFontFamily,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 14.sp,
        lineHeight   = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    // Chips, tags
    labelMedium = TextStyle(
        fontFamily   = MontserratFontFamily,
        fontWeight   = FontWeight.Medium,
        fontSize     = 12.sp,
        lineHeight   = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    // Setting card uppercase labels  e.g. "DURATION"
    labelSmall = TextStyle(
        fontFamily   = MontserratFontFamily,
        fontWeight   = FontWeight.W500,
        fontSize     = 11.sp,
        lineHeight   = 16.sp,
        letterSpacing = 0.5.sp,
        textAlign    = TextAlign.Center,
    ),
)
