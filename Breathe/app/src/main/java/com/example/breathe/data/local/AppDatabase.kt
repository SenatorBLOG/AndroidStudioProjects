package com.example.breathe.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.breathe.data.local.dao.MeditationDao
import com.example.breathe.data.models.MeditationSession

@Database(entities = [MeditationSession::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun meditationDao(): MeditationDao
}