package com.pointlessapps.songbook.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.pointlessapps.songbook.core.setlist.database.entity.SetlistEntity
import com.pointlessapps.songbook.core.setlist.database.entity.SetlistSongEntity
import com.pointlessapps.songbook.core.setlist.database.entity.SetlistWithCount
import com.pointlessapps.songbook.core.song.database.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface SetlistDao {
    @Transaction
    @Query(
        """
        SELECT *, (SELECT COUNT(*) FROM setlist_songs WHERE setlist_id = setlists.id) as songCount 
        FROM setlists 
        LIMIT :limit
    """,
    )
    fun getAllSetlistsFlow(limit: Long): Flow<List<SetlistWithCount>>

    @Transaction
    @Query(
        """
        SELECT *, (SELECT COUNT(*) FROM setlist_songs WHERE setlist_id = :id) as songCount 
        FROM setlists 
        WHERE id = :id
    """,
    )
    fun getSetlistByIdFlow(id: String): Flow<SetlistWithCount?>

    @Transaction
    @Query(
        """
        SELECT s.*
        FROM songs s
        INNER JOIN setlist_songs sj ON s.id = sj.song_id
        WHERE sj.setlist_id = :id
        ORDER BY sj.`order` ASC
    """,
    )
    fun getSetlistSongsById(id: String): Flow<List<SongEntity>>

    @Upsert
    suspend fun insertSetlists(setlists: List<SetlistEntity>)

    @Query("DELETE FROM setlists WHERE id NOT IN (:ids)")
    suspend fun deleteSetlistsExcept(ids: List<String>)

    @Transaction
    suspend fun replaceSetlists(setlists: List<SetlistEntity>) {
        deleteSetlistsExcept(setlists.map(SetlistEntity::id))
        insertSetlists(setlists)
    }

    @Query("UPDATE setlists SET name = :name WHERE id = :id")
    suspend fun updateSetlistName(id: String, name: String)

    @Upsert
    suspend fun insertSetlistSongs(setlistSongs: List<SetlistSongEntity>)

    @Query("DELETE FROM setlist_songs WHERE setlist_id NOT IN (:setlistIds)")
    suspend fun deleteSetlistSongsExcept(setlistIds: List<String>)

    @Transaction
    suspend fun replaceSetlistSongs(setlistSongs: List<SetlistSongEntity>) {
        deleteSetlistSongsExcept(setlistSongs.map(SetlistSongEntity::setlistId))
        insertSetlistSongs(setlistSongs)
    }

    @Transaction
    suspend fun updateSetlistSongs(setlistId: String, setlistSongs: List<SetlistSongEntity>) {
        deleteSetlistSongs(setlistId)
        insertSetlistSongs(setlistSongs)
    }

    @Query("DELETE FROM setlist_songs WHERE setlist_id = :setlistId AND song_id = :songId")
    suspend fun deleteSetlistSong(setlistId: String, songId: String)

    @Query("DELETE FROM setlist_songs WHERE setlist_id = :setlistId")
    suspend fun deleteSetlistSongs(setlistId: String)

    @Query("DELETE FROM setlists WHERE id = :id")
    suspend fun deleteSetlist(id: String)

    @Query("DELETE FROM setlists")
    suspend fun clearSetlists()
}
