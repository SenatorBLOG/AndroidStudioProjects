// com/example/breathe/ui/components/BreathingCircle.kt
package com.example.breathe.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp
import com.example.breathe.Phase
import com.example.breathe.ui.theme.AppColors

@Composable
fun BreathingCircle(
    scale: Float,
    phase: Phase,
    colors: AppColors
) {
    Box(
        modifier = Modifier
            .size(300.dp)
            .scale(scale)
            .drawBehind {
                // Светящееся внешнее свечение
                val outerGradient = Brush.radialGradient(
                    colors = listOf(colors.glowOuter, Color.Transparent),
                    center = Offset(size.width / 2, size.height / 2),
                    radius = size.width / 2 + 32.dp.toPx()
                )
                drawCircle(brush = outerGradient, radius = size.width / 2 + 32.dp.toPx())
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Градиент внутри круга
            val innerGradient = Brush.radialGradient(
                colors = listOf(colors.glowInner, colors.primary.copy(alpha = 0.3f), Color.Transparent),
                center = Offset(size.width / 2, size.height / 2),
                radius = size.width / 2
            )
            drawCircle(brush = innerGradient, radius = size.width / 2)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when (phase) {
                    Phase.INHALE -> "Inhale"
                    Phase.HOLD1 -> "Hold"
                    Phase.EXHALE -> "Exhale"
                    Phase.HOLD2 -> "Hold"
                },
                color = colors.text,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
