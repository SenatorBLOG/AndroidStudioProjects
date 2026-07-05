package com.breatheonline.breathe.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.breatheonline.breathe.BuildConfig
import com.breatheonline.breathe.data.local.AppDatabase
import com.breatheonline.breathe.data.local.MIGRATION_1_2
import com.breatheonline.breathe.data.local.MIGRATION_2_3
import com.breatheonline.breathe.data.local.MIGRATION_3_4
import com.breatheonline.breathe.data.local.dao.MeditationDao
import com.breatheonline.breathe.data.local.dao.SleepInsightFeedbackDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        val db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "meditation_database"
        ).addMigrations(MIGRATION_1_2)
         .addMigrations(MIGRATION_2_3)
         .addMigrations(MIGRATION_3_4)
         .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                if (BuildConfig.DEBUG) {
                    Log.d("DatabaseModule", "DB created: ${context.getDatabasePath("meditation_database")}")
                }
            }
        }).build()
        return db
    }

    @Provides
    fun provideMeditationDao(database: AppDatabase): MeditationDao {
        return database.meditationDao()
    }

    @Provides
    fun provideSleepInsightFeedbackDao(database: AppDatabase): SleepInsightFeedbackDao {
        return database.sleepInsightFeedbackDao()
    }
}
