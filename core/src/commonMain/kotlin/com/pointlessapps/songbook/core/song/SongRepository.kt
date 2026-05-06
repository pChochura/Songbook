package com.pointlessapps.songbook.core.song

import androidx.paging.PagingData
import com.pointlessapps.songbook.core.setlist.model.Setlist
import com.pointlessapps.songbook.core.song.model.NewSong
import com.pointlessapps.songbook.core.song.model.Song
import com.pointlessapps.songbook.core.song.model.SongSearchResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

interface SongRepository {
    fun getAllSongs(
        initialFilterLetter: String? = null,
        sortBy: Song.SortBy,
        sortInAscendingOrder: Boolean,
    ): Flow<PagingData<Song>>

    fun getSongByIdFlow(id: String): Flow<Song?>
    fun getSongsByIdFlow(ids: List<String>): Flow<ImmutableList<Song>>
    fun searchSongs(query: String): Flow<PagingData<SongSearchResult>>
    fun getSongSetlistsById(id: String): Flow<ImmutableList<Setlist>>

    suspend fun updateSongSetlists(id: String, setlistsIds: List<String>)
    suspend fun saveSong(newSong: NewSong, setlistsIds: List<String>): String
    suspend fun deleteSong(id: String)
}
