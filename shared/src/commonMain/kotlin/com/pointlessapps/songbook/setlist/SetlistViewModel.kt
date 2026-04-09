package com.pointlessapps.songbook.setlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessapps.songbook.core.model.DataState
import com.pointlessapps.songbook.core.model.SyncStatus
import com.pointlessapps.songbook.core.setlist.SetlistRepository
import com.pointlessapps.songbook.core.setlist.model.Setlist
import com.pointlessapps.songbook.core.song.model.Song
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal sealed interface SetlistEvent {
    data object NavigateBack : SetlistEvent
    data class NavigateToLyrics(val songId: Long) : SetlistEvent
}

internal sealed interface SetlistState {
    data object Loading : SetlistState
    data class Loaded(
        val setlist: Setlist,
        val songs: List<Song>,
        val syncStatus: SyncStatus = SyncStatus.LOCAL,
    ) : SetlistState
}

internal class SetlistViewModel(
    id: Long,
    private val setlistRepository: SetlistRepository,
) : ViewModel() {

    private val eventChannel = Channel<SetlistEvent>()
    val events = eventChannel.receiveAsFlow()

    private val localSongs = MutableStateFlow<List<Song>?>(null)

    val state: StateFlow<SetlistState> = combine(
        localSongs,
        setlistRepository.getSetlistByIdFlow(id),
        setlistRepository.getSetlistsSongsByIdFlow(id),
    ) { localSongs, setlistData, songsData ->
        val setlist = setlistData.data
        if (setlist == null) {
            // TODO show snackbar
            eventChannel.trySend(SetlistEvent.NavigateBack)

            return@combine SetlistState.Loading
        }

        SetlistState.Loaded(
            setlist = setlist,
            songs = localSongs ?: songsData.data,
            syncStatus = DataState.statusOf(setlistData.status, songsData.status),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SetlistState.Loading,
    )

    fun onLyricsClicked(id: Long) {
        eventChannel.trySend(SetlistEvent.NavigateToLyrics(id))
    }

    fun onNameChanged(name: String) {
        viewModelScope.launch {
            val setlist = (state.value as SetlistState.Loaded).setlist
            setlistRepository.updateSetlistName(setlist.id, name)
        }
    }

    fun onMove(fromIndex: Int, toIndex: Int) {
        val currentState = state.value as? SetlistState.Loaded ?: return
        val songs = currentState.songs.toMutableList()
        if (fromIndex !in songs.indices || toIndex !in songs.indices) return

        val song = songs.removeAt(fromIndex)
        songs.add(toIndex, song)

        localSongs.value = songs
    }

    fun onReorderDone() {
        viewModelScope.launch {
            val setlist = (state.value as SetlistState.Loaded).setlist
            setlistRepository.updateSetlistSongsOrder(setlist.id, localSongs.value ?: emptyList())
            localSongs.value = null
        }
    }

    fun onDeleteSetlistConfirmClicked() {
        viewModelScope.launch {
            val setlist = (state.value as SetlistState.Loaded).setlist
            setlistRepository.deleteSetlist(setlist.id)
            eventChannel.trySend(SetlistEvent.NavigateBack)
        }
    }
}
