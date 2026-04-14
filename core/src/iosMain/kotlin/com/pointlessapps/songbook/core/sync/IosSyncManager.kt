package com.pointlessapps.songbook.core.sync

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import platform.BackgroundTasks.BGProcessingTask
import platform.BackgroundTasks.BGProcessingTaskRequest
import platform.BackgroundTasks.BGTaskScheduler
import platform.Foundation.NSDate
import platform.Foundation.dateWithTimeIntervalSinceNow

@Suppress("UNUSED")
@OptIn(ExperimentalForeignApi::class)
class IosSyncManager : KoinComponent {
    private val syncRepository: SyncRepository by inject()
    private val scope = CoroutineScope(Dispatchers.Default)

    fun registerTasks() {
        BGTaskScheduler.sharedScheduler.registerForTaskWithIdentifier(
            identifier = SYNC_TASK_ID,
            usingQueue = null,
        ) { task ->
            handleSyncTask(task as BGProcessingTask)
        }
    }

    private fun handleSyncTask(task: BGProcessingTask) {
        task.expirationHandler = {
            // TODO: Handle expiration
        }

        scope.launch {
            try {
                syncRepository.sync()
                task.setTaskCompletedWithSuccess(true)
            } catch (e: Exception) {
                task.setTaskCompletedWithSuccess(false)
            }
        }
    }

    fun scheduleSync() {
        val request = BGProcessingTaskRequest(SYNC_TASK_ID).apply {
            earliestBeginDate = NSDate.dateWithTimeIntervalSinceNow(15.0 * 60.0) // 15 minutes
            requiresNetworkConnectivity = true
            requiresExternalPower = false
        }

        try {
            BGTaskScheduler.sharedScheduler.submitTaskRequest(request, null)
        } catch (e: Exception) {
            // Log error
        }
    }

    companion object {
        const val SYNC_TASK_ID = "com.pointlessapps.songbook.sync"
    }
}
