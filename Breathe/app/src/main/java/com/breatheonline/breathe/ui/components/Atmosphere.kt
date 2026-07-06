package com.breatheonline.breathe.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.breatheonline.breathe.ui.theme.AppColors

/**
 * Shared visual language: ambient mesh background + gradient glow button.
 * Used across Home / Breathe / auth screens so every surface feels lit
 * by the same light source instead of a flat fill.
 */

// ── Ambient mesh background ───────────────────────────────────────────────────

/**
 * Three soft radial blobs of the theme's primary hue. Draw this as the first
 * child of a full-screen Box, behind the content. [offsetY] lets callers
 * parallax-scroll it.
 */
@Composable
fun AtmosphericBackground(
    colors: AppColors,
    modifier: Modifier = Modifier,
    offsetY: Float = 0f,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { translationY = -offsetY }
            .drawBehind {
                val w = size.width
                val h = size.height
                // Top-left glow
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(colors.primary.copy(alpha = 0.14f), Color.Transparent),
                        center = Offset(w * 0.12f, h * 0.08f),
                        radius = w * 0.62f,
                    ),
                    radius = w * 0.62f,
                    center = Offset(w * 0.12f, h * 0.08f),
                )
                // Bottom-right counter-glow in the orb's second hue
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(colors.orbSecondary.copy(alpha = 0.08f), Color.Transparent),
                        center = Offset(w * 0.92f, h * 0.82f),
                        radius = w * 0.58f,
                    ),
                    radius = w * 0.58f,
                    center = Offset(w * 0.92f, h * 0.82f),
                )
                // Center ambient wash
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(colors.primary.copy(alpha = 0.05f), Color.Transparent),
                        center = Offset(w * 0.52f, h * 0.44f),
                        radius = w * 0.72f,
                    ),
                    radius = w * 0.72f,
                    center = Offset(w * 0.52f, h * 0.44f),
                )
            },
    )
}

// ── Primary glow button ───────────────────────────────────────────────────────

/**
 * The app's main CTA: primary→orbSecondary gradient, soft outer glow,
 * springy press-scale and a haptic tick. Replaces flat filled buttons.
 */
@Composable
fun GlowButton(
    text: String,
    colors: AppColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    height: Dp = 54.dp,
    cornerRadius: Dp = 18.dp,
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue   = if (pressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label         = "glowButtonScale",
    )
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            // Soft glow halo painted outside the button bounds
            .drawBehind {
                drawRoundRect(
                    brush = Brush.radialGradient(
                        0f to colors.primary.copy(alpha = 0.35f),
                        1f to Color.Transparent,
                        center = Offset(size.width / 2, size.height),
                        radius = size.width * 0.75f,
                    ),
                    topLeft = Offset(-12.dp.toPx(), 0f),
                    size = size.copy(width = size.width + 24.dp.toPx(), height = size.height + 14.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx() + 8.dp.toPx()),
                )
            }
            .clip(shape)
            .drawBehind {
                // Diagonal two-hue gradient body
                drawRect(
                    Brush.linearGradient(
                        0f to colors.primary,
                        1f to colors.orbSecondary,
                        start = Offset(0f, size.height),
                        end   = Offset(size.width, 0f),
                    )
                )
                // Top sheen
                drawRect(
                    Brush.verticalGradient(
                        0f to Color.White.copy(alpha = 0.22f),
                        0.5f to Color.Transparent,
                    )
                )
            }
            .clickable(interactionSource = interactionSource, indication = null) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(vertical = ((height - 22.dp) / 2)),
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (icon != null) {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    tint               = Color.White,
                    modifier           = Modifier.size(18.dp),
                )
            }
            Text(
                text       = text,
                style      = MaterialTheme.typography.labelLarge,
                color      = Color.White,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
