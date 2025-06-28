package com.example.breathe.data.models

import java.time.LocalDateTime

data class HeartRateData(
    val id: Int,
    val timestamp: LocalDateTime,
    val bpm: Int // Удары в минуту
)