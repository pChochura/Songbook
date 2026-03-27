package com.pointlessapps.songbook.library

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessapps.songbook.Route
import com.pointlessapps.songbook.core.auth.AuthRepository
import com.pointlessapps.songbook.core.song.SongRepository
import com.pointlessapps.songbook.core.song.model.Song
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal sealed interface LibraryEvent {
    data class NavigateTo(val route: Route) : LibraryEvent
    data object FocusSearch : LibraryEvent
}

internal data class LibraryState(
    val songs: List<Song> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val filterLetter: Char? = null,
)

internal class LibraryViewModel(
    private val initialFilterLetter: String? = null,
    private val openSearch: Boolean = false,
    private val songRepository: SongRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    var state by mutableStateOf(LibraryState())
        private set

    private val eventChannel = Channel<LibraryEvent>(BUFFERED)
    val events = eventChannel.receiveAsFlow()

    init {
        if (initialFilterLetter != null) {
            state = state.copy(
                filterLetter = initialFilterLetter.firstOrNull()?.uppercaseChar(),
            )
        }
        if (openSearch) eventChannel.trySend(LibraryEvent.FocusSearch)

        viewModelScope.launch {
            state = state.copy(isLoading = true)
            authRepository.initialize()
            if (!authRepository.isSignedIn()) {
                authRepository.signInAnonymously()
            }
            state = state.copy(
                songs = songRepository.getAllSongs(),
                isLoading = false,
            )
        }
    }

    fun setSearchQuery(query: String) {
        state = state.copy(searchQuery = query)
    }

    fun setFilterLetter(letter: Char?) {
        state = state.copy(filterLetter = letter, searchQuery = "")
    }

    fun onImportSongRequested() {
        eventChannel.trySend(LibraryEvent.NavigateTo(Route.ImportSong))
    }

    fun onLyricsRequested(id: Long) {
        eventChannel.trySend(LibraryEvent.NavigateTo(Route.Lyrics(id)))
    }
}
