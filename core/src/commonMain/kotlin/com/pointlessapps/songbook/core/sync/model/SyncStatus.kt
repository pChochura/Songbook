package com.pointlessapps.songbook.core.sync.model

import com.pointlessapps.songbook.core.utils.Keep

@Keep
enum class SyncStatus {
    LOCAL, SYNCING, SYNCED, SYNC_FAILED, OFFLINE;

    val idle: Boolean
        get() = when (this) {
            LOCAL, OFFLINE, SYNC_FAILED, SYNCED -> true
            else -> false
        }

    val failed: Boolean
        get() = when(this) {
            SYNC_FAILED, OFFLINE -> true
            else -> false
        }
}
