package com.pointlessapps.songbook

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.pointlessapps.songbook.core.sync.SyncWorker
import com.pointlessapps.songbook.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.fileProperties
import java.util.concurrent.TimeUnit

class SongbookApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            fileProperties()
            androidContext(this@SongbookApp)
        }

        scheduleSync()
    }

    private fun scheduleSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "SyncWorker",
            ExistingPeriodicWorkPolicy.UPDATE,
            syncRequest,
        )
    }
}
