package com.example.breathe.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.breathe.data.local.AppDatabase
import com.example.breathe.data.local.dao.MeditationDao
import com.example.breathe.data.repository.MeditationRepository
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
        ).addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                android.util.Log.d("DatabaseModule", "Database created at ${context.getDatabasePath("meditation_database")}")
            }
        }).build()
        return db
    }

    @Provides
    fun provideMeditationDao(database: AppDatabase): MeditationDao {
        return database.meditationDao()
    }
    @Provides
    fun provideMeditationRepository(dao: MeditationDao): MeditationRepository {
        return MeditationRepository(dao)
    }
}