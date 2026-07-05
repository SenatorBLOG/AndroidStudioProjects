package com.breatheonline.breathe.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Main app theme.
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
    val colorScheme = if (colors.isLight) {
        lightColorScheme(
            primary              = colors.primary,
            onPrimary            = colors.onPrimary,
            primaryContainer     = colors.primary.copy(alpha = 0.14f),
            onPrimaryContainer   = colors.title,
            secondary            = colors.subtitle,
            onSecondary          = colors.surface,
            secondaryContainer   = colors.surface,
            onSecondaryContainer = colors.text,
            tertiary             = colors.value,
            onTertiary           = colors.onPrimary,
            tertiaryContainer    = colors.surface,
            onTertiaryContainer  = colors.text,
            background           = colors.background,
            onBackground         = colors.text,
            surface              = colors.surface,
            onSurface            = colors.title,
            surfaceVariant       = colors.background,
            onSurfaceVariant     = colors.subtitle,
            outline              = colors.subtitle.copy(alpha = 0.35f),
            outlineVariant       = colors.subtitle.copy(alpha = 0.14f),
            error                = ErrorRed,
            onError              = colors.surface,
            errorContainer       = ErrorRed.copy(alpha = 0.12f),
            onErrorContainer     = ErrorRed,
            inverseSurface       = colors.title,
            inverseOnSurface     = colors.surface,
            inversePrimary       = colors.primary.copy(alpha = 0.80f),
            scrim                = colors.title.copy(alpha = 0.40f),
        )
    } else {
        darkColorScheme(
            primary              = colors.primary,
            onPrimary            = colors.onPrimary,
            primaryContainer     = colors.primary.copy(alpha = 0.18f),
            onPrimaryContainer   = colors.title,
            secondary            = colors.subtitle,
            onSecondary          = colors.background,
            secondaryContainer   = colors.surface,
            onSecondaryContainer = colors.text,
            tertiary             = colors.value,
            onTertiary           = colors.onPrimary,
            tertiaryContainer    = colors.surface,
            onTertiaryContainer  = colors.text,
            background           = colors.background,
            onBackground         = colors.text,
            surface              = colors.surface,
            onSurface            = colors.title,
            surfaceVariant       = colors.surface,
            onSurfaceVariant     = colors.subtitle,
            outline              = colors.subtitle.copy(alpha = 0.40f),
            outlineVariant       = colors.subtitle.copy(alpha = 0.18f),
            error                = ErrorRed,
            onError              = ErrorDark,
            errorContainer       = ErrorRed.copy(alpha = 0.18f),
            onErrorContainer     = ErrorRed,
            inverseSurface       = colors.title,
            inverseOnSurface     = colors.background,
            inversePrimary       = colors.primary.copy(alpha = 0.80f),
            scrim                = Navy950.copy(alpha = 0.65f),
        )
    }

    CompositionLocalProvider(LocalAppColors provides colors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = AppTypography,
            content     = content,
        )
    }
}
