package com.pointlessapps.songbook.core.sync.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.pointlessapps.songbook.core.sync.database.entity.SyncActionEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface SyncActionDao {
    @Query("SELECT * FROM sync_actions ORDER BY createdAt ASC")
    fun getAllActionsFlow(): Flow<List<SyncActionEntity>>

    @Insert
    suspend fun insertAction(action: SyncActionEntity)

    @Query("DELETE FROM sync_actions WHERE id = :id")
    suspend fun deleteAction(id: Long)
}
