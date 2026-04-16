package com.breatheonline.breathe

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.breatheonline.breathe.BuildConfig
import com.breatheonline.breathe.data.local.AppDatabase
import com.breatheonline.breathe.worker.SyncWorker
import com.mapbox.common.MapboxOptions
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BreatheApp : Application(), Configuration.Provider {

    @Inject lateinit var database:      AppDatabase
    @Inject lateinit var workerFactory: HiltWorkerFactory

    // Tell WorkManager to use the Hilt-generated factory so @HiltWorker injection works
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        MapboxOptions.accessToken = BuildConfig.MAPBOX_ACCESS_TOKEN
        database.openHelper.writableDatabase   // warm up Room on first launch

        // Sync any sessions that failed to reach the server in a previous run
        SyncWorker.schedule(this)
    }
}
