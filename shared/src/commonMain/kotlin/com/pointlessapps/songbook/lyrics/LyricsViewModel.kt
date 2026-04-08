package com.pointlessapps.songbook.lyrics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessapps.songbook.core.model.SyncStatus
import com.pointlessapps.songbook.core.prefs.PrefsRepository
import com.pointlessapps.songbook.core.song.SongRepository
import com.pointlessapps.songbook.core.song.model.Section
import com.pointlessapps.songbook.core.song.model.Section.Companion.toLyrics
import com.pointlessapps.songbook.core.utils.Keep
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

@Keep
internal enum class DisplayMode {
    Inline,
    SideBySide,
    Both,
    TextOnly;

    val shouldShowInline get() = this == Inline || this == Both
    val shouldShowSideBySide get() = this == SideBySide || this == Both
}

@Keep
internal enum class WrapMode { Wrap, NoWrap }

internal data class LyricsState(
    val songId: Long? = null,
    val title: String = "",
    val artist: String = "",
    val sections: List<Section> = emptyList(),
    val textScale: Int = 100,
    val keyOffset: Int = 0,
    val isOcrActive: Boolean = false,
    val displayMode: DisplayMode = DisplayMode.Inline,
    val wrapMode: WrapMode = WrapMode.NoWrap,
    val isLoading: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.LOCAL,
)

internal class LyricsViewModel(
    private val songId: Long,
    private val prefsRepository: PrefsRepository,
    private val songRepository: SongRepository,
) : ViewModel() {

    private data class LyricsTransientState(
        val keyOffset: Int = 0,
        val isLoading: Boolean = false,
    )

    private val _transientState = MutableStateFlow(LyricsTransientState())

    val state: StateFlow<LyricsState> = combine(
        prefsRepository.getLyricsTextScaleFlow(),
        prefsRepository.getLyricsDisplayModeFlow(),
        prefsRepository.getLyricsWrapModeFlow(),
        songRepository.getSongById(songId),
        _transientState,
    ) { textScale, displayMode, wrapMode, songResult, transient ->
        val song = songResult.data
        LyricsState(
            songId = songId,
            title = song?.title.orEmpty(),
            artist = song?.artist.orEmpty(),
            sections = song?.sections.orEmpty(),
            syncStatus = songResult.status,
            textScale = textScale,
            displayMode = displayMode?.let(DisplayMode::valueOf) ?: DisplayMode.Inline,
            wrapMode = wrapMode?.let(WrapMode::valueOf) ?: WrapMode.NoWrap,
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
        viewModelScope.launch {
            prefsRepository.setLyricsTextScale(newScale)
        }
    }

    fun onKeyOffsetChanged(keyOffset: Int) {
        _transientState.update { it.copy(keyOffset = keyOffset) }
    }

    fun onDisplayModeChanged(mode: DisplayMode) {
        viewModelScope.launch {
            prefsRepository.setLyricsDisplayMode(mode.name)
        }
    }

    fun onWrapModeChanged(mode: WrapMode) {
        viewModelScope.launch {
            prefsRepository.setLyricsWrapMode(mode.name)
        }
    }

    fun onDeleteSongConfirmClicked() {
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
