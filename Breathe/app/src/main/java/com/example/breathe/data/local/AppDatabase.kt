package com.example.breathe.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.breathe.data.local.dao.MeditationDao
import com.example.breathe.data.models.MeditationSession

@Database(entities = [MeditationSession::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun meditationDao(): MeditationDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE meditation_sessions ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE meditation_sessions ADD COLUMN remoteId TEXT")
        db.execSQL("ALTER TABLE meditation_sessions ADD COLUMN type TEXT NOT NULL DEFAULT 'deep'")
    }
}