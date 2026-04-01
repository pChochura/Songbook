package com.pointlessapps.songbook.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pointlessapps.songbook.core.database.entity.SetlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface SetlistDao {
    @Query("SELECT * FROM setlists")
    fun getAllSetlists(): Flow<List<SetlistEntity>>

    @Query("SELECT * FROM setlists WHERE id = :id")
    fun getSetlistById(id: Long): Flow<SetlistEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetlists(setlists: List<SetlistEntity>)

    @Query("DELETE FROM setlists WHERE id = :id")
    suspend fun deleteSetlist(id: Long)
}
