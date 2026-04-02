package com.pointlessapps.songbook.core.model

enum class SyncStatus(val priority: Int) {
    REMOTE_FAILED(0), LOCAL(1), SYNCED(2)
}

data class DataState<T>(
    val data: T,
    val status: SyncStatus,
) {
    companion object {
        fun statusOf(vararg statuses: SyncStatus) = statuses.minBy { it.priority }
    }
}
