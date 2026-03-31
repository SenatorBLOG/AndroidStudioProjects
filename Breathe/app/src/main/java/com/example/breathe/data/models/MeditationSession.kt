package com.example.breathe.data.models
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meditation_sessions")
data class MeditationSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val duration: Int,               // seconds
    val date: Long,                  // epoch milliseconds
    val isSynced: Boolean = false,   // true once successfully POSTed to API
    val remoteId: String?  = null,   // MongoDB _id returned by POST /sessions
    val type: String       = "deep", // API type key: "4-7-8", "box", "deep", "energy"
)