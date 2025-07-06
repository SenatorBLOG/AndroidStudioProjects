package com.example.breathe.ui.theme

import androidx.compose.ui.graphics.Color

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
    val glowOuter: Color, // Внешнее свечение
    val glowInner: Color, // Внутреннее свечение
    val glowBackground: Color, // Фон для внутреннего свечения: Color // Пульсирующее свечение
)

val OceanThemeColors = AppColors(
    background = Color(0xFF0A0F1F),
    surface = Color(0xFF1A2A4F),
    primary = Color(0xFF3A82F7),
    onPrimary = Color(0xFF10182F),
    title = Color(0xFF70B8FF),
    subtitle = Color(0xFF88AACC),
    label = Color(0xFF88AACC),
    value = Color(0xFF70B8FF),
    text = Color.White,
    glowOuter = Color(0xFF70B8FF).copy(alpha = 0.35f),
    glowInner = Color(0xFF65A8FF).copy(alpha = 0.7f),
    glowBackground = Color(0xFF65A8FF).copy(alpha = 0.1f)
)

val ForestThemeColors = AppColors(
    background = Color(0xFF1A2F1F),
    surface = Color(0xFF2A442C),
    primary = Color(0xFF4CAF50),
    onPrimary = Color(0xFF1A2F1F),
    title = Color(0xFFE8F5E9),
    subtitle = Color(0xFFA5D6A7),
    label = Color(0xFFA5D6A7),
    value = Color(0xFFE8F5E9),
    text = Color(0xFFE8F5E9),
    glowOuter = Color(0xFF4CAF50).copy(alpha = 0.35f),
    glowInner = Color(0xFF4CAF50).copy(alpha = 0.7f),
    glowBackground = Color(0xFF4CAF50).copy(alpha = 0.1f)
)

val SunsetThemeColors = AppColors(
    background = Color(0xFF3F2A1D),
    surface = Color(0xFF5A3F2A),
    primary = Color(0xFFFF5722),
    onPrimary = Color(0xFF3F2A1D),
    title = Color(0xFFFFF8E1),
    subtitle = Color(0xFFFFB74D),
    label = Color(0xFFFFB74D),
    value = Color(0xFFFFF8E1),
    text = Color(0xFFFFF8E1),
    glowOuter = Color(0xFFFF5722).copy(alpha = 0.35f),
    glowInner = Color(0xFFFF5722).copy(alpha = 0.7f),
    glowBackground = Color(0xFFFF5722).copy(alpha = 0.1f)
)