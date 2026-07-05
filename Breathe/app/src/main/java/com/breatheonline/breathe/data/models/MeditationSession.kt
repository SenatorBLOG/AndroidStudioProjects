package com.breatheonline.breathe.data.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "meditation_sessions",
    indices = [
        Index(value = ["date"]),
        Index(value = ["isSynced"]),
    ],
)
data class MeditationSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val duration:         Int,                  // seconds
    val date:             Long,                  // epoch milliseconds
    val isSynced:         Boolean = false,        // true once successfully POSTed to API
    val remoteId:         String? = null,         // MongoDB _id returned by POST /sessions
    val type:             String  = "deep",       // API type key: "4-7-8", "box", "deep", etc.
    // Feedback fields — stored locally so SyncWorker sends real values instead of empty defaults
    val cycles:           Int     = 0,
    val moodBefore:       Int?    = null,
    val moodAfter:        Int?    = null,
    val focusLevel:       Int?    = null,
    val stressLevel:      Int?    = null,
    val breathingDepth:   Int?    = null,
    val calmnessScore:    Int?    = null,
    val distractionCount: Int     = 0,
    val noiseLevel:       String  = "",
    val notes:            String  = "",
    val timeOfDay:        String  = "",
)
