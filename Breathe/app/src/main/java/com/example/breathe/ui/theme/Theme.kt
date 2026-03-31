package com.example.breathe.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Main app theme. Dark-only (meditation apps live in the dark).
 *
 * Usage:
 *   BreatheTheme(colors = OceanThemeColors) { ... }
 *
 * Inside any composable, access the current custom palette via:
 *   val colors = LocalAppColors.current
 */
@Composable
fun BreatheTheme(
    colors: AppColors = DefaultDarkColors,
    content: @Composable () -> Unit,
) {
    // Map our custom AppColors onto every Material3 color role so that M3
    // system components (AlertDialog, Switch, TopAppBar, etc.) stay on-brand.
    val colorScheme = darkColorScheme(
        // ── Primary action color ──────────────────────────────────────────────
        primary             = colors.primary,
        onPrimary           = colors.onPrimary,
        primaryContainer    = colors.primary.copy(alpha = 0.18f),
        onPrimaryContainer  = colors.title,

        // ── Secondary / accent ────────────────────────────────────────────────
        secondary           = colors.subtitle,
        onSecondary         = colors.background,
        secondaryContainer  = colors.surface,
        onSecondaryContainer = colors.text,

        // ── Tertiary (value highlight) ────────────────────────────────────────
        tertiary            = colors.value,
        onTertiary          = colors.onPrimary,
        tertiaryContainer   = colors.surface,
        onTertiaryContainer = colors.text,

        // ── Backgrounds & surfaces ────────────────────────────────────────────
        background          = colors.background,
        onBackground        = colors.text,
        surface             = colors.surface,
        onSurface           = colors.title,
        surfaceVariant      = colors.surface,       // cards, chips bg
        onSurfaceVariant    = colors.subtitle,      // secondary text on cards

        // ── Outlines ──────────────────────────────────────────────────────────
        outline             = colors.subtitle.copy(alpha = 0.40f),
        outlineVariant      = colors.subtitle.copy(alpha = 0.18f),

        // ── Error ─────────────────────────────────────────────────────────────
        error               = ErrorRed,
        onError             = ErrorDark,
        errorContainer      = ErrorRed.copy(alpha = 0.18f),
        onErrorContainer    = ErrorRed,

        // ── Inverse (snackbars, tooltips) ─────────────────────────────────────
        inverseSurface      = colors.title,
        inverseOnSurface    = colors.background,
        inversePrimary      = colors.primary.copy(alpha = 0.80f),

        // ── Scrim (modal overlays) ────────────────────────────────────────────
        scrim               = Navy950.copy(alpha = 0.65f),
    )

    CompositionLocalProvider(LocalAppColors provides colors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = AppTypography,
            content     = content,
        )
    }
}
