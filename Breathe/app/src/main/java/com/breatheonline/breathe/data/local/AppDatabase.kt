package com.breatheonline.breathe.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.breatheonline.breathe.data.local.dao.MeditationDao
import com.breatheonline.breathe.data.models.MeditationSession

@Database(entities = [MeditationSession::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun meditationDao(): MeditationDao
}
