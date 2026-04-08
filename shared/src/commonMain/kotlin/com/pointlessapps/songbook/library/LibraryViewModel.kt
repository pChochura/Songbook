package com.pointlessapps.songbook.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessapps.songbook.core.model.DataState
import com.pointlessapps.songbook.core.model.SyncStatus
import com.pointlessapps.songbook.core.prefs.PrefsRepository
import com.pointlessapps.songbook.core.setlist.SetlistRepository
import com.pointlessapps.songbook.core.setlist.model.Setlist
import com.pointlessapps.songbook.core.song.SongRepository
import com.pointlessapps.songbook.core.song.model.Song
import com.pointlessapps.songbook.utils.Keep
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal sealed interface LibraryEvent {
    data object NavigateToImportSong : LibraryEvent
    data class NavigateToLyrics(val id: Long) : LibraryEvent
    data class NavigateToSetlist(val id: Long) : LibraryEvent
}

@Keep
internal enum class DisplayMode { List, Grid }

internal data class LibraryState(
    val setlists: List<Setlist> = emptyList(),
    val songs: List<Song> = emptyList(),
    val displayMode: DisplayMode = DisplayMode.Grid,
    val syncStatus: SyncStatus = SyncStatus.LOCAL,
)

internal class LibraryViewModel(
    songRepository: SongRepository,
    private val setlistRepository: SetlistRepository,
    private val prefsRepository: PrefsRepository,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<LibraryState> = combine(
        setlistRepository.getAllSetlists(limit = SETLISTS_LIMIT),
        songRepository.getAllSongs(),
        prefsRepository.getLibraryDisplayModeFlow(),
    ) { setlistsState, songsState, displayMode ->
        LibraryState(
            setlists = setlistsState.data,
            songs = songsState.data,
            displayMode = displayMode?.let(DisplayMode::valueOf) ?: DisplayMode.Grid,
            syncStatus = DataState.statusOf(
                setlistsState.status,
                songsState.status,
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LibraryState(),
    )

    private val eventChannel = Channel<LibraryEvent>(BUFFERED)
    val events = eventChannel.receiveAsFlow()

    fun onImportSongClicked() {
        eventChannel.trySend(LibraryEvent.NavigateToImportSong)
    }

    fun onLyricsClicked(id: Long) {
        eventChannel.trySend(LibraryEvent.NavigateToLyrics(id))
    }

    fun onSetlistClicked(id: Long) {
        eventChannel.trySend(LibraryEvent.NavigateToSetlist(id))
    }

    fun onAddSetlistClicked(name: String) {
        viewModelScope.launch {
            val id = setlistRepository.addSetlist(name)
            eventChannel.send(LibraryEvent.NavigateToSetlist(id))
        }
    }

    fun onDisplayModeChanged(mode: DisplayMode) {
        viewModelScope.launch {
            prefsRepository.setLibraryDisplayMode(mode.name)
        }
    }

    private companion object {
        const val SETLISTS_LIMIT = 5L
    }
}
