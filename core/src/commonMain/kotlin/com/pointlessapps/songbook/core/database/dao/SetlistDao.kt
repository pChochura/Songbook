package com.pointlessapps.songbook.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.pointlessapps.songbook.core.setlist.database.entity.SetlistEntity
import com.pointlessapps.songbook.core.setlist.database.entity.SetlistSongEntity
import com.pointlessapps.songbook.core.setlist.database.entity.SetlistWithSongs
import kotlinx.coroutines.flow.Flow

@Dao
internal interface SetlistDao {
    @Transaction
    @Query("SELECT * FROM setlists LIMIT :limit")
    fun getAllSetlistsFlow(limit: Long = -1L): Flow<List<SetlistWithSongs>>

    @Transaction
    @Query("SELECT * FROM setlists WHERE id = :id")
    fun getSetlistByIdFlow(id: Long): Flow<SetlistWithSongs?>

    @Upsert
    suspend fun insertSetlists(setlists: List<SetlistEntity>)

    @Update
    suspend fun updateSetlist(setlist: SetlistEntity)

    @Upsert
    suspend fun insertSetlistSongs(setlistSongs: List<SetlistSongEntity>)

    @Query("DELETE FROM setlists WHERE id = :id")
    suspend fun deleteSetlist(id: Long)

    @Transaction
    suspend fun syncSetlists(
        setlists: List<SetlistEntity>,
        setlistSongs: List<SetlistSongEntity>,
    ) {
        insertSetlists(setlists)
        insertSetlistSongs(setlistSongs)
    }
}
