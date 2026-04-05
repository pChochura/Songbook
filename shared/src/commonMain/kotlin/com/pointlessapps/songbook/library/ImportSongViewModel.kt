package com.pointlessapps.songbook.library

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessapps.songbook.Agent
import com.pointlessapps.songbook.core.app.AppRepository
import com.pointlessapps.songbook.core.setlist.SetlistRepository
import com.pointlessapps.songbook.core.setlist.model.Setlist
import com.pointlessapps.songbook.core.song.ChordLibrary
import com.pointlessapps.songbook.core.song.LyricsParser
import com.pointlessapps.songbook.core.song.SongRepository
import com.pointlessapps.songbook.core.song.model.NewSong
import com.pointlessapps.songbook.core.song.model.Section
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal sealed interface ImportSongEvent {
    data object DiscardChanges : ImportSongEvent
    data object NavigateBack : ImportSongEvent
    data class NavigateToLyrics(val songId: Long) : ImportSongEvent
    data class NavigateToPreview(
        val title: String,
        val artist: String,
        val sections: List<Section>,
    ) : ImportSongEvent
}

internal data class ImportSongState(
    val songId: Long? = null,
    val allSetlists: List<Setlist> = emptyList(),
    val selectedSetlists: List<Setlist> = emptyList(),
    val chordSuggestions: List<String> = emptyList(),
    val canImport: Boolean = false,
    val isExtractingInProgress: Boolean = false,
    val isLoading: Boolean = false,
) {
    val setlistsSelection = allSetlists.associateWith { it in selectedSetlists }
    val isChordPopupVisible = chordSuggestions.isNotEmpty()
}

internal class ImportSongViewModel(
    private val agent: Agent,
    private val setlistRepository: SetlistRepository,
    private val songRepository: SongRepository,
    private val appRepository: AppRepository,
) : ViewModel() {

    val titleTextFieldState = TextFieldState("")
    val artistTextFieldState = TextFieldState("")
    val lyricsTextFieldState = TextFieldState("")

    var state by mutableStateOf(ImportSongState())
        private set

    private val eventChannel = Channel<ImportSongEvent>()
    val events = eventChannel.receiveAsFlow()

    private var extractionJob: Job? = null

    init {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            state = state.copy(
                allSetlists = setlistRepository.getAllSetlists()
                    .firstOrNull()?.data.orEmpty(),
                isLoading = false,
            )

            combine(
                snapshotFlow { lyricsTextFieldState.text }.distinctUntilChanged(),
                snapshotFlow { titleTextFieldState.text }.distinctUntilChanged(),
            ) { lyrics, title -> lyrics.isNotBlank() && title.isNotBlank() }
                .distinctUntilChanged()
                .collect { state = state.copy(canImport = it) }
        }

        viewModelScope.launch {
            snapshotFlow {
                lyricsTextFieldState.text to lyricsTextFieldState.selection
            }.collect { (text, selection) ->
                val cursorPosition = selection.end
                val textBeforeCursor = text.substring(0, cursorPosition)
                val lastOpenBracket = textBeforeCursor.lastIndexOf('[')
                val lastCloseBracket = textBeforeCursor.lastIndexOf(']')

                if (lastOpenBracket != -1 && lastOpenBracket > lastCloseBracket) {
                    val typedChord = textBeforeCursor.substring(lastOpenBracket + 1)
                    state = state.copy(
                        chordSuggestions = ChordLibrary.allChords.filter {
                            it.startsWith(typedChord, ignoreCase = true)
                        }.take(MAX_CHORDS_SUGGESTIONS),
                    )
                } else {
                    state = state.copy(chordSuggestions = emptyList())
                }
            }
        }
    }

    fun setData(id: Long?, title: String?, artist: String?, lyrics: String?) {
        state = state.copy(songId = id)
        titleTextFieldState.setTextAndPlaceCursorAtEnd(title.orEmpty())
        artistTextFieldState.setTextAndPlaceCursorAtEnd(artist.orEmpty())
        lyricsTextFieldState.setTextAndPlaceCursorAtEnd(lyrics.orEmpty())
    }

    fun onImportSongClicked() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            songRepository.saveSong(
                NewSong(
                    id = state.songId,
                    title = titleTextFieldState.text.toString(),
                    artist = artistTextFieldState.text.toString(),
                    sections = computeSections(),
                ),
            )
            eventChannel.send(ImportSongEvent.NavigateBack)
            state = state.copy(isLoading = false)
        }
    }

    fun onCancelClicked() {
        if (
            titleTextFieldState.text.isNotBlank() ||
            artistTextFieldState.text.isNotBlank() ||
            lyricsTextFieldState.text.isNotBlank() ||
            state.selectedSetlists.isNotEmpty()
        ) {
            titleTextFieldState.clearText()
            artistTextFieldState.clearText()
            lyricsTextFieldState.clearText()
            eventChannel.trySend(ImportSongEvent.DiscardChanges)
        } else {
            eventChannel.trySend(ImportSongEvent.NavigateBack)
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

        extractionJob?.cancel()
        extractionJob = viewModelScope.launch {
            state = state.copy(isExtractingInProgress = true)
            val result = agent.extractSongData(bytes)
            if (result == null) {
                // TODO show a snackbar
                state = state.copy(isExtractingInProgress = false)
                return@launch
            }

            val data = result.first()
            titleTextFieldState.setTextAndPlaceCursorAtEnd(data.title.orEmpty())
            artistTextFieldState.setTextAndPlaceCursorAtEnd(data.author.orEmpty())
            lyricsTextFieldState.setTextAndPlaceCursorAtEnd(data.toLyrics())
            state = state.copy(isExtractingInProgress = false)
        }
    }

    fun onCancelExtractionClicked() {
        extractionJob?.cancel()
        state = state.copy(isExtractingInProgress = false)
    }

    fun onOpenSettingsClicked() {
        appRepository.openAppSettings()
    }

    fun onPreviewClicked() {
        eventChannel.trySend(
            ImportSongEvent.NavigateToPreview(
                title = titleTextFieldState.text.toString(),
                artist = artistTextFieldState.text.toString(),
                sections = computeSections(),
            ),
        )
    }

    fun onChordSelected(chord: String) {
        val text = lyricsTextFieldState.text.toString()
        val selection = lyricsTextFieldState.selection
        val cursorPosition = selection.end
        val textBeforeCursor = text.substring(0, cursorPosition)
        val lastOpenBracket = textBeforeCursor.lastIndexOf('[')

        if (lastOpenBracket != -1) {
            lyricsTextFieldState.edit {
                replace(lastOpenBracket, cursorPosition, "[$chord]")
            }
        }
        state = state.copy(chordSuggestions = emptyList())
    }

    fun onDismissChordPopup() {
        state = state.copy(chordSuggestions = emptyList())
    }

    private fun computeSections() = LyricsParser.parseLyrics(lyricsTextFieldState.text.toString())

    private companion object {
        const val MAX_CHORDS_SUGGESTIONS = 6
    }
}
