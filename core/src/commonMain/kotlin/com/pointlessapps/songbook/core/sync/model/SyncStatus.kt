package com.pointlessapps.songbook.core.sync.model

enum class SyncStatus {
    LOCAL, SYNCING, SYNCED, SYNC_FAILED, OFFLINE;

    val failed: Boolean
        get() = when(this) {
            SYNC_FAILED, OFFLINE -> true
            else -> false
        }
}
