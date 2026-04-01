package com.pointlessapps.songbook.core.model

enum class SyncStatus {
    LOCAL, SYNCED
}

data class DataState<T>(
    val data: T,
    val status: SyncStatus,
)
