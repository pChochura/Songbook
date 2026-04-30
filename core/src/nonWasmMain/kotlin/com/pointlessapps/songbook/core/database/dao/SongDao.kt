package com.pointlessapps.songbook.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.pointlessapps.songbook.core.setlist.database.entity.SetlistEntity
import com.pointlessapps.songbook.core.setlist.database.entity.SetlistSongEntity
import com.pointlessapps.songbook.core.song.database.entity.SongEntity
import com.pointlessapps.songbook.core.song.database.entity.SongSearchEntity
import com.pointlessapps.songbook.core.song.database.mapper.toSearchEntity
import com.pointlessapps.songbook.core.song.model.SongSearchResult
import kotlinx.coroutines.flow.Flow

@Dao
internal interface SongDao {
    @Query("SELECT * FROM songs WHERE title LIKE :initialFilterLetter || '%' ORDER BY title ASC")
    fun getAllSongs(initialFilterLetter: String): PagingSource<Int, SongEntity>

    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): PagingSource<Int, SongEntity>

    @Query("SELECT * FROM songs WHERE id = :id")
    fun getSongByIdFlow(id: String): Flow<SongEntity?>

    @Upsert
    suspend fun insertSongs(songs: List<SongEntity>)

    @Upsert
    suspend fun insertSearchIndex(entities: List<SongSearchEntity>)

    @Transaction
    suspend fun insertSongWithSetlists(
        song: SongEntity,
        setlistsSongs: List<SetlistSongEntity>,
    ) {
        insertSongs(listOf(song))
        insertSearchIndex(listOf(song.toSearchEntity()))
        updateSongSetlists(song.id, setlistsSongs)
    }

    @Query("DELETE FROM songs WHERE id = :id")
    suspend fun deleteSong(id: String)

    @Query("DELETE FROM songs_search WHERE songId = :id")
    suspend fun deleteSearchIndex(id: String)

    @Transaction
    suspend fun deleteSongWithSearch(id: String) {
        deleteSong(id)
        deleteSearchIndex(id)
    }

    @Query(
        """
        SELECT 
            rowid as id,
            songId,
            title, 
            artist, 
            snippet(songs_search, '<b>', '</b>', '...', -1, 10) as snippet
        FROM songs_search
        WHERE songs_search MATCH :query
    """,
    )
    fun searchSongs(query: String): PagingSource<Int, SongSearchResult>

    @Transaction
    @Query(
        """
        SELECT s.*
        FROM setlists s
        INNER JOIN setlist_songs sj ON s.id = sj.setlist_id
        WHERE sj.song_id = :id
        ORDER BY sj.`order` ASC
    """,
    )
    fun getSongSetlistsById(id: String): Flow<List<SetlistEntity>>

    @Query("DELETE FROM setlist_songs WHERE song_id = :songId")
    suspend fun deleteSongSetlist(songId: String)

    @Upsert
    suspend fun insertSetlistSongs(setlistSongs: List<SetlistSongEntity>)

    @Transaction
    suspend fun updateSongSetlists(songId: String, setlistSongs: List<SetlistSongEntity>) {
        deleteSongSetlist(songId)
        insertSetlistSongs(setlistSongs)
    }
}
