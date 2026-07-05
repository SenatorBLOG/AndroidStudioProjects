package com.breatheonline.breathe.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.breatheonline.breathe.data.local.dao.MeditationDao
import com.breatheonline.breathe.data.models.MeditationSession
import com.breatheonline.breathe.data.local.dao.SleepInsightFeedbackDao
import com.breatheonline.breathe.data.models.SleepInsightFeedback

@Database(
    entities = [MeditationSession::class, SleepInsightFeedback::class],
    version = 4,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun meditationDao(): MeditationDao
    abstract fun sleepInsightFeedbackDao(): SleepInsightFeedbackDao
}

/**
 * Adds the feedback columns introduced in v2.
 * All columns are nullable or have safe defaults so existing rows are unaffected.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE meditation_sessions ADD COLUMN cycles           INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE meditation_sessions ADD COLUMN moodBefore       INTEGER")
        db.execSQL("ALTER TABLE meditation_sessions ADD COLUMN moodAfter        INTEGER")
        db.execSQL("ALTER TABLE meditation_sessions ADD COLUMN focusLevel       INTEGER")
        db.execSQL("ALTER TABLE meditation_sessions ADD COLUMN stressLevel      INTEGER")
        db.execSQL("ALTER TABLE meditation_sessions ADD COLUMN breathingDepth   INTEGER")
        db.execSQL("ALTER TABLE meditation_sessions ADD COLUMN calmnessScore    INTEGER")
        db.execSQL("ALTER TABLE meditation_sessions ADD COLUMN distractionCount INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE meditation_sessions ADD COLUMN noiseLevel       TEXT    NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE meditation_sessions ADD COLUMN notes            TEXT    NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE meditation_sessions ADD COLUMN timeOfDay        TEXT    NOT NULL DEFAULT ''")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS sleep_insight_feedback (
                insightKey TEXT NOT NULL PRIMARY KEY,
                insightText TEXT NOT NULL,
                sentiment INTEGER NOT NULL,
                createdAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE INDEX IF NOT EXISTS index_meditation_sessions_date ON meditation_sessions (date)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_meditation_sessions_isSynced ON meditation_sessions (isSynced)")
    }
}
