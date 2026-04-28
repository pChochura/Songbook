package com.pointlessapps.songbook.core.sync

import com.pointlessapps.songbook.core.sync.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow

internal class WasmSyncRepository : SyncRepository {
    override val currentSyncStatusFlow = MutableStateFlow(SyncStatus.SYNCED)
    override fun performSyncAsFlow(): Flow<Unit> = emptyFlow()
    override suspend fun clearDatabase() {}
}
