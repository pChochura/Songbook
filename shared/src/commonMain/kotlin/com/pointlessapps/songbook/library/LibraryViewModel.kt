package com.pointlessapps.songbook.library

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessapps.songbook.Route
import com.pointlessapps.songbook.core.auth.AuthRepository
import com.pointlessapps.songbook.core.model.DataState
import com.pointlessapps.songbook.core.model.SyncStatus
import com.pointlessapps.songbook.core.setlist.SetlistRepository
import com.pointlessapps.songbook.core.setlist.model.Setlist
import com.pointlessapps.songbook.core.song.SongRepository
import com.pointlessapps.songbook.core.song.model.Song
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal sealed interface LibraryEvent {
    data class NavigateTo(val route: Route) : LibraryEvent
}

internal data class LibraryState(
    val setlists: List<Setlist> = emptyList(),
    val songs: List<Song> = emptyList(),
    val isLoading: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.LOCAL,
)

internal class LibraryViewModel(
    initialFilterLetter: String? = null,
    openSearch: Boolean = false,
    private val setlistRepository: SetlistRepository,
    private val songRepository: SongRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    var state by mutableStateOf(LibraryState())
        private set

    private val eventChannel = Channel<LibraryEvent>(BUFFERED)
    val events = eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            authRepository.initialize()
            if (!authRepository.isSignedIn()) {
                authRepository.signInAnonymously()
            }

            state = state.copy(isLoading = true)
            combine(
                setlistRepository.getAllSetlists(limit = SETLISTS_LIMIT),
                songRepository.getAllSongs(),
            ) { setlistsState, songsState ->
                state = state.copy(
                    setlists = setlistsState.data,
                    songs = songsState.data,
                    syncStatus = DataState.statusOf(
                        setlistsState.status,
                        songsState.status,
                    ),
                    isLoading = false,
                )
            }.collect()
        }
    }

    fun onImportSongClicked() {
        eventChannel.trySend(LibraryEvent.NavigateTo(Route.ImportSong()))
    }

    fun onLyricsClicked(id: Long) {
        eventChannel.trySend(LibraryEvent.NavigateTo(Route.Lyrics(id)))
    }

    fun onAddSetlistClicked(name: String) {

    }

    private companion object {
        const val SETLISTS_LIMIT = 5L
    }
}
