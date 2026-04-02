package com.pointlessapps.songbook.search

import androidx.compose.foundation.text.input.TextFieldState
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
import com.pointlessapps.songbook.core.song.SongRepository
import com.pointlessapps.songbook.core.song.database.entity.SongSearchResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal sealed interface SearchEvent {
    data object NavigateBack : SearchEvent
}

internal data class SearchState(
    val lastSearches: List<String> = emptyList(),
    val isLoading: Boolean = false,
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
internal class SearchViewModel(
    private val prefsRepository: PrefsRepository,
    private val songRepository: SongRepository,
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
        .flatMapLatest { songRepository.searchSongs(it.toString()) }
        .cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            prefsRepository.getLastSearchesFlow().collect { lastSearches ->
                state = state.copy(
                    lastSearches = lastSearches.toList().asReversed(),
                    isLoading = false,
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
        queryTextFieldState.setTextAndPlaceCursorAtEnd("")
    }

    fun onLastSearchClicked(search: String) {
        queryTextFieldState.setTextAndPlaceCursorAtEnd(search)
    }

    fun onLastSearchRemoveClicked(search: String) = viewModelScope.launch {
        prefsRepository.removeLastSearch(search)
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
