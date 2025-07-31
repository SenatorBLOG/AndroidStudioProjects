package com.example.breathe.data.models
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meditation_sessions")
data class MeditationSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val duration: Int, // duration in seconds
    val date: Long     // date in timestamp format
)