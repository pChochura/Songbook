package com.pointlessapps.songbook.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.pointlessapps.songbook.core.song.database.entity.SongEntity
import com.pointlessapps.songbook.core.song.database.entity.SongSearchEntity
import com.pointlessapps.songbook.core.song.database.entity.SongSearchResult
import com.pointlessapps.songbook.core.song.model.Section.Companion.toLyrics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

@Dao
internal interface SongDao {
    @Query("SELECT * FROM songs")
    fun getAllSongs(): PagingSource<Int, SongEntity>

    @Query("SELECT * FROM songs WHERE id = :id")
    fun getSongByIdFlow(id: String): Flow<SongEntity?>

    @Upsert
    suspend fun insertSongs(songs: List<SongEntity>)

    @Query("DELETE FROM songs WHERE id = :id")
    suspend fun deleteSong(id: String)

    @Transaction
    suspend fun insertSongsWithSearch(songs: List<SongEntity>) {
        insertSongs(songs)
        val searchEntities = songs.map { song ->
            SongSearchEntity(
                songId = song.id,
                title = song.title,
                artist = song.artist,
                content = song.sections.toLyrics(withChords = false),
            )
        }
        insertSearchIndex(searchEntities)
    }

    @Upsert
    suspend fun insertSearchIndex(entities: List<SongSearchEntity>)

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
            songId as id,
            title, 
            artist, 
            snippet(songs_search, '<b>', '</b>', '...', -1, 10) as snippet
        FROM songs_search
        WHERE songs_search MATCH :query
    """,
    )
    fun searchSongs(query: String): PagingSource<Int, SongSearchResult>
}
