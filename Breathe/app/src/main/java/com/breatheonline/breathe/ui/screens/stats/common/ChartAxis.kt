package com.breatheonline.breathe.ui.screens.stats.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

internal fun DrawScope.drawHorizontalGrid(
    steps: Int,
    color: Color,
    strokeWidth: Float = 1f,
) {
    val step = size.height / steps
    val dash = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
    for (i in 0..steps) {
        val y = i * step
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(0f, y),
            end = androidx.compose.ui.geometry.Offset(size.width, y),
            strokeWidth = strokeWidth,
            pathEffect = dash,
        )
    }
}

internal fun DrawScope.drawBaseline(
    y: Float,
    color: Color,
) {
    val dash = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
    drawLine(
        color = color,
        start = androidx.compose.ui.geometry.Offset(0f, y),
        end = androidx.compose.ui.geometry.Offset(size.width, y),
        strokeWidth = 2f,
        pathEffect = dash,
    )
}
