package com.example.breathe.data.models

import java.time.LocalDate

data class MeditationSession(
    val id: Int,
    val date: LocalDate,    // Session Date
    val duration: Long      // Duration in milliseconds
)