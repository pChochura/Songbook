package com.pointlessapps.songbook.core.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val syncRepository: SyncRepository by inject()

    override suspend fun doWork() = syncRepository.sync().fold(
        onSuccess = { Result.success() },
        onFailure = { Result.retry() },
    )
}
