package com.pointlessapps.songbook.setlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessapps.songbook.core.setlist.SetlistRepository
import com.pointlessapps.songbook.core.setlist.model.Setlist
import com.pointlessapps.songbook.core.song.model.Song
import com.pointlessapps.songbook.core.sync.SyncRepository
import com.pointlessapps.songbook.core.sync.model.SyncStatus
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.error_setlist_not_found
import com.pointlessapps.songbook.ui.theme.IconWarning
import com.pointlessapps.songbook.utils.SongbookSnackbarState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

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

    val loaded: Loaded get() = this as Loaded
}

internal class SetlistViewModel(
    id: Long,
    syncRepository: SyncRepository,
    private val setlistRepository: SetlistRepository,
    private val snackbarState: SongbookSnackbarState,
) : ViewModel() {

    private data class SetlistTransientState(
        val localSongs: List<Song>? = null,
        val isLoading: Boolean = false,
    )

    private val _transientState = MutableStateFlow(SetlistTransientState())

    val state: StateFlow<SetlistState> = combine(
        syncRepository.currentSyncStatus,
        setlistRepository.getSetlistByIdFlow(id),
        setlistRepository.getSetlistsSongsById(id),
        _transientState,
    ) { syncStatus, setlist, songs, transient ->
        if (transient.isLoading) {
            return@combine SetlistState.Loading
        }

        if (setlist == null) {
            snackbarState.showSnackbar(
                message = getString(Res.string.error_setlist_not_found),
                icon = IconWarning,
            )
            eventChannel.trySend(SetlistEvent.NavigateBack)

            return@combine SetlistState.Loading
        }

        SetlistState.Loaded(
            setlist = setlist,
            songs = transient.localSongs ?: songs,
            syncStatus = syncStatus,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SetlistState.Loading,
    )

    private val eventChannel = Channel<SetlistEvent>()
    val events = eventChannel.receiveAsFlow()

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
        val state = state.value.loaded
        val songs = state.songs.toMutableList()
        if (fromIndex !in songs.indices || toIndex !in songs.indices) return

        val song = songs.removeAt(fromIndex)
        songs.add(toIndex, song)

        _transientState.update { it.copy(localSongs = songs) }
    }

    fun onReorderDone() {
        viewModelScope.launch {
            val state = state.value.loaded
            setlistRepository.updateSetlistSongsOrder(
                id = state.setlist.id,
                songs = state.songs,
            )
            _transientState.update { it.copy(localSongs = null) }
        }
    }

    fun onDeleteSetlistConfirmClicked() {
        viewModelScope.launch {
            val state = state.value.loaded
            _transientState.update { it.copy(isLoading = true) }
            setlistRepository.deleteSetlist(state.setlist.id)
            eventChannel.trySend(SetlistEvent.NavigateBack)
        }
    }
}
