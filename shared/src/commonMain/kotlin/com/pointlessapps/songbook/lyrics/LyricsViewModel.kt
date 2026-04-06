package com.pointlessapps.songbook.lyrics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessapps.songbook.core.model.SyncStatus
import com.pointlessapps.songbook.core.prefs.PrefsRepository
import com.pointlessapps.songbook.core.song.SongRepository
import com.pointlessapps.songbook.core.song.model.Section
import com.pointlessapps.songbook.core.song.model.Section.Companion.toLyrics
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal sealed interface LyricsEvent {
    data object NavigateBack : LyricsEvent
    data class NavigateToImportSong(
        val songId: Long,
        val title: String,
        val artist: String,
        val lyrics: String,
    ) : LyricsEvent
}

internal enum class LyricsMode {
    Inline,
    SideBySide,
    Both,
    TextOnly;

    val shouldShowInline get() = this == Inline || this == Both
    val shouldShowSideBySide get() = this == SideBySide || this == Both
}

internal data class LyricsState(
    val songId: Long? = null,
    val title: String = "",
    val artist: String = "",
    val sections: List<Section> = emptyList(),
    val textScale: Int = 100,
    val keyOffset: Int = 0,
    val isOcrActive: Boolean = false,
    val mode: LyricsMode = LyricsMode.Inline,
    val isLoading: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.LOCAL,
)

internal class LyricsViewModel(
    private val songId: Long,
    private val prefsRepository: PrefsRepository,
    private val songRepository: SongRepository,
) : ViewModel() {

    private data class LyricsTransientState(
        val textScale: Int = 100,
        val mode: LyricsMode = LyricsMode.Inline,
        val keyOffset: Int = 0,
        val isLoading: Boolean = false,
    )

    private val _transientState = MutableStateFlow(LyricsTransientState())

    val state: StateFlow<LyricsState> = combine(
        songRepository.getSongById(songId),
        _transientState,
    ) { songResult, transient ->
        val song = songResult.data
        LyricsState(
            songId = songId,
            title = song?.title ?: "",
            artist = song?.artist ?: "",
            sections = song?.sections ?: emptyList(),
            syncStatus = songResult.status,
            textScale = transient.textScale,
            mode = transient.mode,
            keyOffset = transient.keyOffset,
            isLoading = transient.isLoading,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LyricsState(songId = songId, isLoading = true),
    )

    private val eventChannel = Channel<LyricsEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            val textScale = prefsRepository.getTextScale()
            val mode = prefsRepository.getMode()?.let(LyricsMode::valueOf) ?: LyricsMode.Inline
            _transientState.update {
                it.copy(
                    textScale = textScale,
                    mode = mode,
                )
            }
        }
    }

    fun onEditSongClicked() {
        eventChannel.trySend(
            LyricsEvent.NavigateToImportSong(
                songId = songId,
                title = state.value.title,
                artist = state.value.artist,
                lyrics = state.value.sections.toLyrics(),
            ),
        )
    }

    fun onTextScaleChanged(textScale: Int) {
        val newScale = textScale.coerceIn(MIN_ZOOM, MAX_ZOOM)
        _transientState.update { it.copy(textScale = newScale) }
        viewModelScope.launch {
            prefsRepository.setTextScale(newScale)
        }
    }

    fun onKeyOffsetChanged(keyOffset: Int) {
        _transientState.update { it.copy(keyOffset = keyOffset) }
    }

    fun onModeChanged(mode: LyricsMode) {
        _transientState.update { it.copy(mode = mode) }
        viewModelScope.launch {
            prefsRepository.setMode(mode.name)
        }
    }

    fun deleteSong() {
        viewModelScope.launch {
            _transientState.update { it.copy(isLoading = true) }
            songRepository.deleteSong(songId)
            eventChannel.send(LyricsEvent.NavigateBack)
        }
    }

    fun broadcastSongToTeam() {
        // TODO
    }

    companion object {
        const val MIN_ZOOM = 100
        const val MAX_ZOOM = 300
    }
}
