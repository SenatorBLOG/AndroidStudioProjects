package com.example.breathe

import android.app.Application
import com.example.breathe.data.local.AppDatabase
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BreatheApp : Application() {
    @Inject
    lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        database.openHelper.writableDatabase
    }
}