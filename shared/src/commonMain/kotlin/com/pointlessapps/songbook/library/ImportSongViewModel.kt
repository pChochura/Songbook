package com.pointlessapps.songbook.library

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessapps.songbook.Agent
import com.pointlessapps.songbook.core.setlist.SetlistRepository
import com.pointlessapps.songbook.core.setlist.model.Setlist
import com.pointlessapps.songbook.core.song.LyricsParser
import com.pointlessapps.songbook.core.song.SongRepository
import com.pointlessapps.songbook.core.song.model.Section
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal sealed interface ImportSongEvent {
    data class NavigateToLyrics(val songId: Long) : ImportSongEvent
}

internal data class ImportSongState(
    val sections: List<Section> = emptyList(),
    val allSetlists: List<Setlist> = emptyList(),
    val selectedSetlists: List<Setlist> = emptyList(),
    val canImport: Boolean = false,
    val isExtractingFromPhoto: Boolean = false,
    val isLoading: Boolean = false,
) {
    val setlistsSelection = allSetlists.associateWith { it in selectedSetlists }
}

internal class ImportSongViewModel(
    private val agent: Agent,
    private val setlistRepository: SetlistRepository,
    private val songRepository: SongRepository,
) : ViewModel() {

    val titleTextFieldState = TextFieldState("")
    val artistTextFieldState = TextFieldState("")
    val lyricsTextFieldState = TextFieldState("")

    var state by mutableStateOf(ImportSongState())
        private set

    private val eventChannel = Channel<ImportSongEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            state = state.copy(
                allSetlists = setlistRepository.getAllSetlists()
                    .firstOrNull()?.data.orEmpty(),
                isLoading = false,
            )
        }
    }

    fun onSetlistsSelected(setlists: List<Setlist>) {
        state = state.copy(
            selectedSetlists = setlists,
        )
    }

    fun onImageCaptured(bytes: ByteArray?) {
        if (bytes == null) {
            // TODO show a snackbar
            return
        }

        viewModelScope.launch {
            state = state.copy(isExtractingFromPhoto = true)
            val result = agent.extractSongData(bytes)
            if (result == null) {
                // TODO show a snackbar
                state = state.copy(isExtractingFromPhoto = false)
                return@launch
            }

            val data = result.first()
            titleTextFieldState.setTextAndPlaceCursorAtEnd(data.title.orEmpty())
            artistTextFieldState.setTextAndPlaceCursorAtEnd(data.author.orEmpty())
            lyricsTextFieldState.setTextAndPlaceCursorAtEnd(data.toLyrics())
            state = state.copy(
                sections = computeSections(),
                isExtractingFromPhoto = false,
            )
        }
    }

    fun computeSections() = LyricsParser.parseLyrics(lyricsTextFieldState.text.toString())
}
