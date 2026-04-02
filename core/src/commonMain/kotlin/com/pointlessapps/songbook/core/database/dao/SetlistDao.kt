package com.pointlessapps.songbook.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.pointlessapps.songbook.core.setlist.database.entity.SetlistEntity
import com.pointlessapps.songbook.core.setlist.database.entity.SetlistSongEntity
import com.pointlessapps.songbook.core.setlist.database.entity.SetlistWithSongs
import kotlinx.coroutines.flow.Flow

@Dao
internal interface SetlistDao {
    @Transaction
    @Query("SELECT * FROM setlists")
    fun getAllSetlists(): Flow<List<SetlistWithSongs>>

    @Transaction
    @Query("SELECT * FROM setlists WHERE id = :id")
    fun getSetlistById(id: Long): Flow<SetlistWithSongs?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetlists(setlists: List<SetlistEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetlistSongs(setlistSongs: List<SetlistSongEntity>)

    @Query("DELETE FROM setlists WHERE id = :id")
    suspend fun deleteSetlist(id: Long)
}
