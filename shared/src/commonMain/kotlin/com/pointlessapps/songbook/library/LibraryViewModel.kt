package com.pointlessapps.songbook.library

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessapps.songbook.Route
import com.pointlessapps.songbook.data.SongDao
import com.pointlessapps.songbook.data.SongEntity
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal sealed interface LibraryEvent {
    data class NavigateTo(val route: Route) : LibraryEvent
    data object FocusSearch : LibraryEvent
}

internal data class LibraryState(
    val songs: List<SongEntity> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val filterLetter: Char? = null,
)

internal class LibraryViewModel(
    private val initialFilterLetter: String? = null,
    private val openSearch: Boolean = false,
    private val songDao: SongDao,
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
            songDao.getAllSongs().collectLatest { songs ->
                state = state.copy(
                    songs = songs,
                    isLoading = false,
                )
            }
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
