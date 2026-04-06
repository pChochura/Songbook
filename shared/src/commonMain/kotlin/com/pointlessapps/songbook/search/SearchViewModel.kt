package com.pointlessapps.songbook.search

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.pointlessapps.songbook.core.prefs.PrefsRepository
import com.pointlessapps.songbook.core.song.LyricsParser
import com.pointlessapps.songbook.core.song.PublicLyricsRepository
import com.pointlessapps.songbook.core.song.SongRepository
import com.pointlessapps.songbook.core.song.database.entity.SongSearchResult
import com.pointlessapps.songbook.core.song.model.PublicLyrics
import com.pointlessapps.songbook.core.song.model.Section
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal sealed interface SearchEvent {
    data object NavigateBack : SearchEvent
    data class NavigateToImportSong(
        val title: String,
        val artist: String,
        val lyrics: String,
    ) : SearchEvent

    data class NavigateToPreview(
        val title: String,
        val artist: String,
        val sections: List<Section>,
    ) : SearchEvent
}

internal data class SearchState(
    val lastSearches: List<String> = emptyList(),
    val publicLyrics: List<PublicLyrics> = emptyList(),
    val showPublicLyrics: Boolean? = null,
    val isLoadingYourLibrary: Boolean = false,
    val isLoadingPublicLyrics: Boolean = false,
    val isLoading: Boolean = false,
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
internal class SearchViewModel(
    private val prefsRepository: PrefsRepository,
    private val songRepository: SongRepository,
    private val publicLyricsRepository: PublicLyricsRepository,
) : ViewModel() {

    val queryTextFieldState: TextFieldState = TextFieldState()

    var state by mutableStateOf(SearchState())
        private set

    private val eventChannel = Channel<SearchEvent>()
    val events = eventChannel.receiveAsFlow()

    val searchResults: Flow<PagingData<SongSearchResult>> = snapshotFlow {
        queryTextFieldState.text
    }.distinctUntilChanged()
        .debounce(SEARCH_QUERY_DEBOUNCE)
        .filter { it.isNotEmpty() }
        .onEach { state = state.copy(isLoadingYourLibrary = true) }
        .flatMapLatest { songRepository.searchSongs(it.toString()) }
        .onEach { state = state.copy(isLoadingYourLibrary = false) }
        .cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            prefsRepository.getLastSearchesFlow().collect { lastSearches ->
                state = state.copy(
                    lastSearches = lastSearches.toList(),
                    isLoading = false,
                )
            }
        }

        viewModelScope.launch {
            state = state.copy(showPublicLyrics = prefsRepository.getShowPublicLyrics())

            snapshotFlow { queryTextFieldState.text }
                .filter { state.showPublicLyrics == true }
                .distinctUntilChanged()
                .debounce(SEARCH_QUERY_DEBOUNCE)
                .filter { it.isNotEmpty() }
                .onEach {
                    state = state.copy(
                        isLoadingPublicLyrics = true,
                        publicLyrics = emptyList(),
                    )
                }
                .flatMapLatest { publicLyricsRepository.searchPublicLyrics(it.toString()) }
                .collectLatest {
                    state = state.copy(
                        isLoadingPublicLyrics = false,
                        publicLyrics = it,
                    )
                }
        }
    }

    fun onBackClicked() {
        viewModelScope.launch {
            eventChannel.send(SearchEvent.NavigateBack)
        }
    }

    fun onClearClicked() {
        addLastSearch(queryTextFieldState.text.toString())
        queryTextFieldState.clearText()
    }

    fun onLastSearchClicked(search: String) {
        queryTextFieldState.setTextAndPlaceCursorAtEnd(search)
    }

    fun onLastSearchRemoveClicked(search: String) = viewModelScope.launch {
        prefsRepository.removeLastSearch(search)
    }

    fun onPublicLyricsPreviewClicked(lyrics: PublicLyrics) {
        addLastSearch(queryTextFieldState.text.toString())
        eventChannel.trySend(
            SearchEvent.NavigateToPreview(
                title = lyrics.trackName,
                artist = lyrics.artistName,
                sections = LyricsParser.parseLyrics(lyrics.plainLyrics),
            ),
        )
    }

    fun onPublicLyricsImportClicked(lyrics: PublicLyrics) {
        addLastSearch(queryTextFieldState.text.toString())
        eventChannel.trySend(
            SearchEvent.NavigateToImportSong(
                title = lyrics.trackName,
                artist = lyrics.artistName,
                lyrics = lyrics.plainLyrics,
            ),
        )
    }

    fun onAllowPublicLyricsClicked() {
        viewModelScope.launch {
            prefsRepository.setShowPublicLyrics(true)
            queryTextFieldState.setTextAndPlaceCursorAtEnd(queryTextFieldState.text.toString())
            state = state.copy(showPublicLyrics = true)
        }
    }

    fun onDenyPublicLyricsClicked() {
        viewModelScope.launch {
            prefsRepository.setShowPublicLyrics(false)
            state = state.copy(showPublicLyrics = false)
        }
    }

    fun onImeAction() {
        addLastSearch(queryTextFieldState.text.toString())
    }

    private fun addLastSearch(search: String) = viewModelScope.launch {
        prefsRepository.addLastSearch(search)
    }

    private companion object {
        const val SEARCH_QUERY_DEBOUNCE = 300L
    }
}
