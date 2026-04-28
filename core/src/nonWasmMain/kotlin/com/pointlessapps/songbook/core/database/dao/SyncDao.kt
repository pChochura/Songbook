package com.pointlessapps.songbook.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.pointlessapps.songbook.core.setlist.database.entity.SetlistEntity
import com.pointlessapps.songbook.core.setlist.database.entity.SetlistSongEntity
import com.pointlessapps.songbook.core.song.database.entity.SongEntity
import com.pointlessapps.songbook.core.song.database.entity.SongSearchEntity
import com.pointlessapps.songbook.core.song.database.mapper.toSearchEntity

@Dao
internal interface SyncDao {

    @Upsert
    suspend fun insertSongs(songs: List<SongEntity>)

    @Upsert
    suspend fun insertSearchIndex(entities: List<SongSearchEntity>)

    @Upsert
    suspend fun insertSetlistSongs(setlistSongs: List<SetlistSongEntity>)

    @Upsert
    suspend fun insertSetlists(setlists: List<SetlistEntity>)

    @Query("DELETE FROM songs WHERE id NOT IN (:ids)")
    suspend fun deleteSongsExcept(ids: List<String>)

    @Query("DELETE FROM songs_search")
    suspend fun deleteSearchIndexes()

    @Query("DELETE FROM setlists WHERE id NOT IN (:ids)")
    suspend fun deleteSetlistsExcept(ids: List<String>)

    @Query("DELETE FROM setlist_songs WHERE setlist_id NOT IN (:setlistIds)")
    suspend fun deleteSetlistSongsExcept(setlistIds: List<String>)

    @Transaction
    suspend fun replaceAll(
        songs: List<SongEntity>,
        setlists: List<SetlistEntity>,
        setlistSongs: List<SetlistSongEntity>,
    ) {
        deleteSetlistSongsExcept(setlistSongs.map(SetlistSongEntity::setlistId))
        deleteSetlistsExcept(setlists.map(SetlistEntity::id))
        deleteSongsExcept(songs.map(SongEntity::id))
        deleteSearchIndexes()

        insertSongs(songs)
        insertSearchIndex(songs.map(SongEntity::toSearchEntity))
        insertSetlists(setlists)
        insertSetlistSongs(setlistSongs)
    }

    @Query("DELETE FROM songs")
    suspend fun clearSongs()

    @Query("DELETE FROM songs_search")
    suspend fun clearSearchIndex()

    @Query("DELETE FROM setlists")
    suspend fun clearSetlists()

    @Query("DELETE FROM setlist_songs")
    suspend fun clearSetlistSongs()

    @Query("DELETE FROM sync_actions")
    suspend fun clearActions()

    @Transaction
    suspend fun clear() {
        clearSongs()
        clearSearchIndex()

        clearSetlists()
        clearSetlistSongs()

        clearActions()
    }
}
