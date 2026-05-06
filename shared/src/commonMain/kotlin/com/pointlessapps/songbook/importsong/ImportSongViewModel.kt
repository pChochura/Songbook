package com.pointlessapps.songbook.importsong

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import com.pointlessapps.songbook.Agent
import com.pointlessapps.songbook.core.app.AppRepository
import com.pointlessapps.songbook.core.network.NetworkRepository
import com.pointlessapps.songbook.core.network.model.NetworkStatus
import com.pointlessapps.songbook.core.prefs.PrefsRepository
import com.pointlessapps.songbook.core.setlist.SetlistRepository
import com.pointlessapps.songbook.core.setlist.model.Setlist
import com.pointlessapps.songbook.core.song.ChordLibrary
import com.pointlessapps.songbook.core.song.LyricsParser
import com.pointlessapps.songbook.core.song.SongRepository
import com.pointlessapps.songbook.core.song.model.Chord
import com.pointlessapps.songbook.core.song.model.NewSong
import com.pointlessapps.songbook.core.song.model.Section
import com.pointlessapps.songbook.core.song.model.Section.Companion.toLyrics
import com.pointlessapps.songbook.core.utils.emptyImmutableList
import com.pointlessapps.songbook.shared.ui.Res
import com.pointlessapps.songbook.shared.ui.common_show
import com.pointlessapps.songbook.shared.ui.error_image_capture_failed
import com.pointlessapps.songbook.shared.ui.error_image_extraction_failed
import com.pointlessapps.songbook.shared.ui.import_changes_saved
import com.pointlessapps.songbook.shared.ui.import_song_imported
import com.pointlessapps.songbook.ui.theme.IconWarning
import com.pointlessapps.songbook.utils.BaseViewModel
import com.pointlessapps.songbook.utils.Keep
import com.pointlessapps.songbook.utils.SongbookSnackbarCallbackAction
import com.pointlessapps.songbook.utils.SongbookSnackbarState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

internal sealed interface ImportSongEvent {
    data object DiscardChanges : ImportSongEvent
    data object NavigateBack : ImportSongEvent
}

@Keep
internal enum class DisplayMode {
    Text, Visual
}

@Stable
internal data class ImportSongState(
    val songId: String? = null,
    val allSetlists: ImmutableList<Setlist> = emptyImmutableList(),
    val selectedSetlists: ImmutableList<Setlist> = emptyImmutableList(),
    val chordSuggestions: ImmutableList<String> = emptyImmutableList(),
    val displayMode: DisplayMode = DisplayMode.Text,
    val textScale: Int = 100,
    val sections: ImmutableList<Section> = emptyImmutableList(),
    val canImport: Boolean = false,
    val isExtractingInProgress: Boolean = false,
    val hasInternetConnection: Boolean = true,
    val isLoading: Boolean = false,
) {
    val setlistsSelection: ImmutableMap<Setlist, Boolean> =
        allSetlists.associateWith { it in selectedSetlists }.toImmutableMap()
}

internal class ImportSongViewModel(
    id: String?,
    title: String?,
    artist: String?,
    lyrics: String?,
    setlistRepository: SetlistRepository,
    networkRepository: NetworkRepository,
    private val agent: Agent,
    private val songRepository: SongRepository,
    private val appRepository: AppRepository,
    private val prefsRepository: PrefsRepository,
    private val snackbarState: SongbookSnackbarState,
) : BaseViewModel(snackbarState) {

    val showScanDialog: Boolean =
        id == null && title.isNullOrEmpty() && artist.isNullOrEmpty() && lyrics.isNullOrEmpty()

    @Stable
    private data class ImportSongTransientState(
        val selectedSetlists: ImmutableList<Setlist> = emptyImmutableList(),
        val displayMode: DisplayMode = DisplayMode.Text,
        val textScale: Int = 100,
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
        snapshotFlow {
            lyricsTextFieldState.text to lyricsTextFieldState.selection.end
        }.distinctUntilChanged(),
        snapshotFlow { lyricsTextFieldState.text }.distinctUntilChanged().map {
            LyricsParser.parseLyrics(it.toString())
        }.distinctUntilChanged(),
        networkRepository.networkStatus,
        _transientState,
    ) { allSetlists, titleText, (lyricsText, lyricsCursor), sections, networkStatus, transient ->
        ImportSongState(
            songId = id,
            allSetlists = allSetlists,
            selectedSetlists = transient.selectedSetlists,
            chordSuggestions = calculateChordSuggestions(lyricsText.toString(), lyricsCursor),
            displayMode = transient.displayMode,
            textScale = transient.textScale,
            sections = sections,
            canImport = titleText.isNotBlank() && lyricsText.isNotBlank(),
            isExtractingInProgress = transient.isExtractingInProgress,
            hasInternetConnection = networkStatus == NetworkStatus.ONLINE,
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
            _transientState.update {
                it.copy(textScale = prefsRepository.getLyricsTextScaleFlow().first())
            }
        }
    }

    fun onImportSongClicked() {
        viewModelScope.launch {
            _transientState.update { it.copy(isLoading = true) }
            val id = songRepository.saveSong(
                newSong = NewSong(
                    id = state.value.songId,
                    title = titleTextFieldState.text.toString(),
                    artist = artistTextFieldState.text.toString(),
                    lyrics = lyricsTextFieldState.text.toString(),
                ),
                setlistsIds = state.value.selectedSetlists.map { it.id },
            )
            _transientState.update { it.copy(isLoading = false) }
            eventChannel.send(ImportSongEvent.NavigateBack)

            when (state.value.songId) {
                null -> snackbarState.showSnackbar(
                    message = getString(Res.string.import_song_imported),
                    actionLabel = getString(Res.string.common_show),
                    callbackAction = SongbookSnackbarCallbackAction.LoadToQueueAndOpen(id),
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

    fun onSetlistsSelected(setlists: ImmutableList<Setlist>) {
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
            val result = runCatching { agent.extractSongData(bytes) }.onFailure {
                it.printStackTrace()
            }.getOrNull()
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
            lyricsTextFieldState.setTextAndPlaceCursorAtEnd(data.content)
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
        _transientState.update {
            it.copy(
                displayMode = if (it.displayMode == DisplayMode.Visual) {
                    DisplayMode.Text
                } else {
                    DisplayMode.Visual
                },
            )
        }
    }

    fun onChordSelected(chord: String) {
        val text = lyricsTextFieldState.text.toString()
        val selection = lyricsTextFieldState.selection
        val cursorPosition = selection.end
        val textBeforeCursor = text.substring(0, cursorPosition)
        val lastOpenBracket = textBeforeCursor.lastIndexOf('[')

        val endIndex = Regex("\\S*]").matchAt(text, cursorPosition)
            ?.range?.last?.inc() ?: cursorPosition

        if (lastOpenBracket != -1) {
            lyricsTextFieldState.edit {
                replace(lastOpenBracket, endIndex, "[$chord]")
            }
        }
    }

    fun onChordMoved(
        sectionId: Int,
        chord: Chord,
        newPosition: Int,
    ) {
        val sections = state.value.sections.toMutableList()
        val sectionIndex = sections.indexOfFirst { it.id == sectionId }
        if (sectionIndex == -1) return
        val section = sections[sectionIndex]
        sections[sectionIndex] = section.copy(
            chords = section.chords.map {
                if (it == chord) {
                    it.copy(
                        position = it.position - it.linePosition + newPosition,
                        linePosition = newPosition,
                    )
                } else {
                    it
                }
            }.toImmutableList(),
        )
        lyricsTextFieldState.edit {
            replace(0, length, sections.toLyrics())
            placeCursorBeforeCharAt(0)
        }
    }

    fun onChordInserted(
        sectionId: Int,
        position: Int,
        chord: String,
    ) {
        val sections = state.value.sections.toMutableList()
        val sectionIndex = sections.indexOfFirst { it.id == sectionId }
        if (sectionIndex == -1) return
        val section = sections[sectionIndex]

        val linesBefore = section.lyrics.substring(0, position).lines()
        val linePosition = linesBefore.last().length

        val newChord = Chord(
            value = chord,
            position = position,
            linePosition = linePosition,
        )

        sections[sectionIndex] = section.copy(
            chords = (section.chords.filter { it.position != newChord.position } + newChord)
                .sortedBy { it.position }.toImmutableList(),
        )
        lyricsTextFieldState.edit {
            replace(0, length, sections.toLyrics())
            placeCursorBeforeCharAt(0)
        }
    }

    private fun calculateChordSuggestions(
        text: String,
        cursorPosition: Int,
    ): ImmutableList<String> {
        val textBeforeCursor = text.substring(0, cursorPosition)
        val lastOpenBracket = textBeforeCursor.lastIndexOf('[')
        val lastCloseBracket = textBeforeCursor.lastIndexOf(']')

        if (lastOpenBracket != -1 && lastOpenBracket > lastCloseBracket) {
            val typedChord = textBeforeCursor.substring(lastOpenBracket + 1)
            val suggestions = ChordLibrary.allChords.filter {
                it.startsWith(typedChord, ignoreCase = true)
            }
            return suggestions.toImmutableList()
        }

        return emptyImmutableList()
    }
}
