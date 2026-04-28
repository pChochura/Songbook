package com.pointlessapps.songbook.core.setlist

import com.pointlessapps.songbook.core.setlist.model.Setlist
import com.pointlessapps.songbook.core.song.model.Song
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

interface SetlistRepository {
    fun getAllSetlistsFlow(limit: Long = Long.MAX_VALUE): Flow<ImmutableList<Setlist>>
    fun getSetlistByIdFlow(id: String): Flow<Setlist?>
    fun getSetlistsSongsById(id: String): Flow<ImmutableList<Song>>

    suspend fun addSetlist(name: String): String
    suspend fun deleteSetlist(id: String)
    suspend fun updateSetlistName(id: String, name: String)
    suspend fun updateSetlistSongs(id: String, songsIds: List<String>)
    suspend fun addSongToSetlist(id: String, songId: String, order: Int)
    suspend fun removeSongFromSetlist(setlistId: String, songId: String)
}
