package com.pointlessapps.songbook.importsong

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessapps.songbook.Agent
import com.pointlessapps.songbook.Route
import com.pointlessapps.songbook.core.app.AppRepository
import com.pointlessapps.songbook.core.setlist.SetlistRepository
import com.pointlessapps.songbook.core.setlist.model.Setlist
import com.pointlessapps.songbook.core.song.ChordLibrary
import com.pointlessapps.songbook.core.song.LyricsParser
import com.pointlessapps.songbook.core.song.SongRepository
import com.pointlessapps.songbook.core.song.model.NewSong
import com.pointlessapps.songbook.core.song.model.Section
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.common_show
import com.pointlessapps.songbook.shared.error_image_capture_failed
import com.pointlessapps.songbook.shared.error_image_extraction_failed
import com.pointlessapps.songbook.shared.import_changes_saved
import com.pointlessapps.songbook.shared.import_song_imported
import com.pointlessapps.songbook.ui.theme.IconWarning
import com.pointlessapps.songbook.utils.SongbookSnackbarCallbackAction
import com.pointlessapps.songbook.utils.SongbookSnackbarState
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

internal sealed interface ImportSongEvent {
    data object DiscardChanges : ImportSongEvent
    data object NavigateBack : ImportSongEvent
    data class NavigateToLyrics(val songId: String) : ImportSongEvent
    data class NavigateToPreview(
        val title: String,
        val artist: String,
        val sections: List<Section>,
    ) : ImportSongEvent
}

internal data class ImportSongState(
    val songId: String? = null,
    val allSetlists: List<Setlist> = emptyList(),
    val selectedSetlists: List<Setlist> = emptyList(),
    val chordSuggestions: List<String> = emptyList(),
    val canImport: Boolean = false,
    val isExtractingInProgress: Boolean = false,
    val isLoading: Boolean = false,
) {
    val setlistsSelection = allSetlists.associateWith { it in selectedSetlists }
}

internal class ImportSongViewModel(
    id: String?,
    title: String?,
    artist: String?,
    lyrics: String?,
    private val agent: Agent,
    private val setlistRepository: SetlistRepository,
    private val songRepository: SongRepository,
    private val appRepository: AppRepository,
    private val snackbarState: SongbookSnackbarState,
) : ViewModel() {

    val showScanDialog: Boolean =
        id == null && title.isNullOrEmpty() && artist.isNullOrEmpty() && lyrics.isNullOrEmpty()

    private data class ImportSongTransientState(
        val selectedSetlists: List<Setlist> = emptyList(),
        val chordSuggestions: List<String> = emptyList(),
        val isExtractingInProgress: Boolean = false,
        val isLoading: Boolean = false,
    )

    val titleTextFieldState = TextFieldState(title.orEmpty())
    val artistTextFieldState = TextFieldState(artist.orEmpty())
    val lyricsTextFieldState = TextFieldState(lyrics.orEmpty())

    private val _transientState = MutableStateFlow(ImportSongTransientState())

    val state: StateFlow<ImportSongState> = combine(
        setlistRepository.getAllSetlistsFlow(),
        snapshotFlow { titleTextFieldState.text }.distinctUntilChanged(),
        snapshotFlow { lyricsTextFieldState.text }.distinctUntilChanged(),
        _transientState,
    ) { allSetlists, titleText, lyricsText, transient ->
        ImportSongState(
            songId = id,
            allSetlists = allSetlists,
            selectedSetlists = transient.selectedSetlists,
            chordSuggestions = transient.chordSuggestions,
            canImport = titleText.isNotBlank() && lyricsText.isNotBlank(),
            isExtractingInProgress = transient.isExtractingInProgress,
            isLoading = transient.isLoading,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ImportSongState(songId = id, isLoading = true),
    )

    private val eventChannel = Channel<ImportSongEvent>(BUFFERED)
    val events = eventChannel.receiveAsFlow()

    private var extractionJob: Job? = null

    init {
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
                    val suggestions = ChordLibrary.allChords.filter {
                        it.startsWith(typedChord, ignoreCase = true)
                    }
                    _transientState.update { it.copy(chordSuggestions = suggestions) }
                } else {
                    _transientState.update { it.copy(chordSuggestions = emptyList()) }
                }
            }
        }
    }

    fun onImportSongClicked() {
        viewModelScope.launch {
            _transientState.update { it.copy(isLoading = true) }
            val id = songRepository.saveSong(
                NewSong(
                    id = state.value.songId,
                    title = titleTextFieldState.text.toString(),
                    artist = artistTextFieldState.text.toString(),
                    sections = computeSections(),
                ),
            )
            _transientState.update { it.copy(isLoading = false) }
            eventChannel.send(ImportSongEvent.NavigateBack)

            when (state.value.songId) {
                null -> snackbarState.showSnackbar(
                    message = getString(Res.string.import_song_imported),
                    actionLabel = getString(Res.string.common_show),
                    callbackAction = SongbookSnackbarCallbackAction.NavigateTo(
                        Route.Lyrics(id),
                    ),
                    duration = SnackbarDuration.Long,
                )

                else -> snackbarState.showSnackbar(getString(Res.string.import_changes_saved))
            }
        }
    }

    fun onCancelClicked() {
        if (
            titleTextFieldState.text.isNotBlank() ||
            artistTextFieldState.text.isNotBlank() ||
            lyricsTextFieldState.text.isNotBlank() ||
            state.value.selectedSetlists.isNotEmpty()
        ) {
            eventChannel.trySend(ImportSongEvent.DiscardChanges)
        } else {
            eventChannel.trySend(ImportSongEvent.NavigateBack)
        }
    }

    fun onDiscardChangesClicked() {
        eventChannel.trySend(ImportSongEvent.NavigateBack)
    }

    fun onSetlistsSelected(setlists: List<Setlist>) {
        _transientState.update {
            it.copy(selectedSetlists = setlists)
        }
    }

    fun onImageCaptured(bytes: ByteArray?) {
        if (bytes == null) {
            viewModelScope.launch {
                snackbarState.showSnackbar(
                    message = getString(Res.string.error_image_capture_failed),
                    icon = IconWarning,
                )
            }

            return
        }

        extractionJob?.cancel()
        extractionJob = viewModelScope.launch {
            _transientState.update { it.copy(isExtractingInProgress = true) }
            val result = agent.extractSongData(bytes)
            if (result == null) {
                snackbarState.showSnackbar(
                    message = getString(Res.string.error_image_extraction_failed),
                    icon = IconWarning,
                )
                _transientState.update { it.copy(isExtractingInProgress = false) }

                return@launch
            }

            val data = result.first()
            titleTextFieldState.setTextAndPlaceCursorAtEnd(data.title.orEmpty())
            artistTextFieldState.setTextAndPlaceCursorAtEnd(data.author.orEmpty())
            lyricsTextFieldState.setTextAndPlaceCursorAtEnd(data.toLyrics())
            _transientState.update { it.copy(isExtractingInProgress = false) }
        }
    }

    fun onCancelExtractionClicked() {
        extractionJob?.cancel()
        _transientState.update { it.copy(isExtractingInProgress = false) }
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
        _transientState.update { it.copy(chordSuggestions = emptyList()) }
    }

    fun onDismissChordPopup() {
        _transientState.update { it.copy(chordSuggestions = emptyList()) }
    }

    private fun computeSections() = LyricsParser.parseLyrics(lyricsTextFieldState.text.toString())
}
