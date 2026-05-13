package com.pointlessapps.songbook.search

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.pointlessapps.songbook.core.prefs.PrefsRepository
import com.pointlessapps.songbook.core.queue.QueueManager
import com.pointlessapps.songbook.core.song.LyricsParser
import com.pointlessapps.songbook.core.song.PublicLyricsRepository
import com.pointlessapps.songbook.core.song.SongRepository
import com.pointlessapps.songbook.core.song.model.PublicLyrics
import com.pointlessapps.songbook.core.song.model.Section
import com.pointlessapps.songbook.core.song.model.SongSearchResult
import com.pointlessapps.songbook.core.sync.SyncRepository
import com.pointlessapps.songbook.core.sync.model.SyncStatus
import com.pointlessapps.songbook.core.utils.emptyImmutableList
import com.pointlessapps.songbook.utils.BaseViewModel
import com.pointlessapps.songbook.utils.SongbookSnackbarState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
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
import kotlin.time.Duration.Companion.milliseconds

internal sealed interface SearchEvent {
    data object NavigateBack : SearchEvent
    data object NavigateToLyrics : SearchEvent
    data class NavigateToImportSong(
        val title: String,
        val artist: String,
        val lyrics: String,
    ) : SearchEvent

    data class NavigateToPreview(
        val title: String,
        val artist: String,
        val sections: ImmutableList<Section>,
    ) : SearchEvent
}

@Stable
internal data class SearchState(
    val lastSearches: ImmutableList<String> = emptyImmutableList(),
    val publicLyrics: ImmutableList<PublicLyrics> = emptyImmutableList(),
    val showPublicLyrics: Boolean? = null,
    val isLoadingYourLibrary: Boolean = false,
    val isLoadingPublicLyrics: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.LOCAL,
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
internal class SearchViewModel(
    syncRepository: SyncRepository,
    private val queueManager: QueueManager,
    private val prefsRepository: PrefsRepository,
    private val songRepository: SongRepository,
    private val publicLyricsRepository: PublicLyricsRepository,
    snackbarState: SongbookSnackbarState,
) : BaseViewModel(snackbarState) {

    private val eventChannel = Channel<SearchEvent>()
    val events = eventChannel.receiveAsFlow()

    @Stable
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
        .flatMapLatest { songRepository.searchSongs(it.toString()) }
        .onEach { _transientState.update { it.copy(isLoadingYourLibrary = false) } }
        .cachedIn(viewModelScope)

    private val publicLyricsSearchFlow: Flow<ImmutableList<PublicLyrics>> = snapshotFlow {
        queryTextFieldState.text
    }.combine(
        _transientState.map { it.showPublicLyrics }.distinctUntilChanged(),
    ) { query, show -> query to show }
        .debounce(SEARCH_QUERY_DEBOUNCE)
        .transformLatest { (query, show) ->
            if (show != true || query.isEmpty()) {
                _transientState.update { it.copy(isLoadingPublicLyrics = false) }
                emit(emptyImmutableList())
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
        syncRepository.currentSyncStatusFlow,
        prefsRepository.getLastSearchesFlow(),
        publicLyricsSearchFlow,
        _transientState,
    ) { syncStatus, lastSearches, publicLyrics, transient ->
        SearchState(
            lastSearches = lastSearches.toImmutableList(),
            publicLyrics = publicLyrics,
            showPublicLyrics = transient.showPublicLyrics,
            isLoadingYourLibrary = transient.isLoadingYourLibrary,
            isLoadingPublicLyrics = transient.isLoadingPublicLyrics,
            syncStatus = syncStatus,
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

    fun onLyricsClicked(songId: String) {
        viewModelScope.launch {
            queueManager.clearQueueAndSetSong(songId)
            eventChannel.send(SearchEvent.NavigateToLyrics)
        }
    }

    fun onBackClicked() {
        eventChannel.trySend(SearchEvent.NavigateBack)
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
        val SEARCH_QUERY_DEBOUNCE = 3.milliseconds
    }
}
