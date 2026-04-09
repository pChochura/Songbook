package com.pointlessapps.songbook.search

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
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
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
internal class SearchViewModel(
    private val prefsRepository: PrefsRepository,
    private val songRepository: SongRepository,
    private val publicLyricsRepository: PublicLyricsRepository,
) : ViewModel() {

    private val eventChannel = Channel<SearchEvent>()
    val events = eventChannel.receiveAsFlow()

    private data class SearchTransientState(
        val showPublicLyrics: Boolean? = null,
        val isLoadingYourLibrary: Boolean = false,
        val isLoadingPublicLyrics: Boolean = false,
    )

    private val _transientState = MutableStateFlow(SearchTransientState())

    val queryTextFieldState: TextFieldState = TextFieldState()

    val searchResults: Flow<PagingData<SongSearchResult>> = snapshotFlow {
        queryTextFieldState.text
    }.distinctUntilChanged()
        .debounce(SEARCH_QUERY_DEBOUNCE)
        .filter { it.isNotEmpty() }
        .onEach { _transientState.update { it.copy(isLoadingYourLibrary = true) } }
        .flatMapLatest { songRepository.searchSongsFlow(it.toString()) }
        .onEach { _transientState.update { it.copy(isLoadingYourLibrary = false) } }
        .cachedIn(viewModelScope)

    private val publicLyricsSearchFlow: Flow<List<PublicLyrics>> = snapshotFlow {
        queryTextFieldState.text
    }.combine(
        _transientState.map { it.showPublicLyrics }.distinctUntilChanged(),
    ) { query, show -> query to show }
        .debounce(SEARCH_QUERY_DEBOUNCE)
        .transformLatest { (query, show) ->
            if (show != true || query.isEmpty()) {
                _transientState.update { it.copy(isLoadingPublicLyrics = false) }
                emit(emptyList())
                return@transformLatest
            }

            _transientState.update { it.copy(isLoadingPublicLyrics = true) }
            try {
                publicLyricsRepository.searchPublicLyrics(query.toString()).collect {
                    _transientState.update { it.copy(isLoadingPublicLyrics = false) }
                    emit(it)
                }
            } catch (_: Exception) {
                _transientState.update { it.copy(isLoadingPublicLyrics = false) }
            } finally {
                _transientState.update { it.copy(isLoadingPublicLyrics = false) }
            }
        }

    val state: StateFlow<SearchState> = combine(
        prefsRepository.getLastSearchesFlow(),
        publicLyricsSearchFlow,
        _transientState,
    ) { lastSearches, publicLyrics, transient ->
        SearchState(
            lastSearches = lastSearches.toList(),
            publicLyrics = publicLyrics,
            showPublicLyrics = transient.showPublicLyrics,
            isLoadingYourLibrary = transient.isLoadingYourLibrary,
            isLoadingPublicLyrics = transient.isLoadingPublicLyrics,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SearchState(),
    )

    init {
        viewModelScope.launch {
            _transientState.update {
                it.copy(showPublicLyrics = prefsRepository.getShowPublicLyrics())
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
            _transientState.update { it.copy(showPublicLyrics = true) }
        }
    }

    fun onDenyPublicLyricsClicked() {
        viewModelScope.launch {
            prefsRepository.setShowPublicLyrics(false)
            _transientState.update { it.copy(showPublicLyrics = false) }
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
