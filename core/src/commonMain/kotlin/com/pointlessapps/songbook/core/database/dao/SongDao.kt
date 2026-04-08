package com.pointlessapps.songbook.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.pointlessapps.songbook.core.song.database.entity.SongEntity
import com.pointlessapps.songbook.core.song.database.entity.SongSearchEntity
import com.pointlessapps.songbook.core.song.database.entity.SongSearchResult
import kotlinx.coroutines.flow.Flow

@Dao
internal interface SongDao {
    @Query("SELECT * FROM songs")
    fun getAllSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE id = :id")
    fun getSongById(id: Long): Flow<SongEntity?>

    @Upsert
    suspend fun insertSongs(songs: List<SongEntity>)

    @Query("DELETE FROM songs WHERE id = :id")
    suspend fun deleteSong(id: Long)

    @Transaction
    suspend fun insertSongsWithSearch(songs: List<SongEntity>) {
        insertSongs(songs)
        val searchEntities = songs.map { song ->
            SongSearchEntity(
                id = song.id,
                title = song.title,
                artist = song.artist,
                content = song.sections.joinToString("\n") { it.lyrics }
            )
        }
        insertSearchIndex(searchEntities)
    }

    @Upsert
    suspend fun insertSearchIndex(entities: List<SongSearchEntity>)

    @Query("DELETE FROM songs_search WHERE rowid = :id")
    suspend fun deleteSearchIndex(id: Long)

    @Transaction
    suspend fun deleteSongWithSearch(id: Long) {
        deleteSong(id)
        deleteSearchIndex(id)
    }

    @Query("""
        SELECT 
            rowid as id, 
            title, 
            artist, 
            snippet(songs_search, '<b>', '</b>', '...', -1, 10) as snippet
        FROM songs_search
        WHERE songs_search MATCH :query
    """)
    fun searchSongs(query: String): PagingSource<Int, SongSearchResult>
}
