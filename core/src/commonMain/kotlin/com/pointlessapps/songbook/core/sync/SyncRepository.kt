package com.pointlessapps.songbook.core.sync

import com.pointlessapps.songbook.core.sync.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SyncRepository {
    val currentSyncStatusFlow: StateFlow<SyncStatus>

    fun performSyncAsFlow(): Flow<Unit>

    suspend fun clearDatabase()
}
