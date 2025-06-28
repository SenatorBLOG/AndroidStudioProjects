package com.example.breathe.data.models

import java.time.LocalDateTime

data class SleepData(
    val id: Int,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime
)