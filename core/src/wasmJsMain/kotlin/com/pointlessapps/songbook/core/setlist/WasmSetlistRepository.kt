package com.pointlessapps.songbook.core.setlist

import com.pointlessapps.songbook.core.setlist.model.Setlist
import com.pointlessapps.songbook.core.song.model.Song
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

internal class WasmSetlistRepository : SetlistRepository {
    override fun getAllSetlistsFlow(limit: Long): Flow<ImmutableList<Setlist>> = emptyFlow()
    override fun getSetlistByIdFlow(id: String): Flow<Setlist?> = emptyFlow()
    override fun getSetlistsSongsById(id: String): Flow<ImmutableList<Song>> = emptyFlow()
    override suspend fun addSetlist(name: String): String = ""
    override suspend fun deleteSetlist(id: String) {}
    override suspend fun updateSetlistName(id: String, name: String) {}
    override suspend fun updateSetlistSongs(id: String, songsIds: List<String>) {}
    override suspend fun addSongToSetlist(id: String, songId: String, order: Int) {}
    override suspend fun removeSongFromSetlist(setlistId: String, songId: String) {}
}
