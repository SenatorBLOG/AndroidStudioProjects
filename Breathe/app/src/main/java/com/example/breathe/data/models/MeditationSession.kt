package com.example.breathe.data.models
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meditation_sessions")
data class MeditationSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val duration: Long, // длительность в секундах
    val date: Long     // дата в формате timestamp
)