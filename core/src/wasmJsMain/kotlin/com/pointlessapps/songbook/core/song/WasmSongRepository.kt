package com.pointlessapps.songbook.core.song

import androidx.paging.PagingData
import com.pointlessapps.songbook.core.setlist.model.Setlist
import com.pointlessapps.songbook.core.song.model.NewSong
import com.pointlessapps.songbook.core.song.model.Song
import com.pointlessapps.songbook.core.song.model.SongSearchResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

internal class WasmSongRepository : SongRepository {
    override fun getAllSongs(
        initialFilterLetter: String?,
        sortBy: Song.SortBy,
        sortInAscendingOrder: Boolean,
    ): Flow<PagingData<Song>> = emptyFlow()

    override fun getSongByIdFlow(id: String): Flow<Song> = emptyFlow()
    override fun getSongsByIdFlow(ids: List<String>): Flow<ImmutableList<Song>> = emptyFlow()
    override fun searchSongs(query: String): Flow<PagingData<SongSearchResult>> = emptyFlow()
    override fun getSongSetlistsById(id: String): Flow<ImmutableMap<Setlist, Boolean>> = emptyFlow()
    override suspend fun updateSongSetlists(id: String, setlistsIds: List<String>) {}
    override suspend fun saveSong(newSong: NewSong, setlistsIds: List<String>): String = ""
    override suspend fun deleteSong(id: String) {}
}
