package com.pointlessapps.songbook.lyrics

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessapps.songbook.core.model.SyncStatus
import com.pointlessapps.songbook.core.prefs.PrefsRepository
import com.pointlessapps.songbook.core.song.SongRepository
import com.pointlessapps.songbook.core.song.model.Section
import com.pointlessapps.songbook.core.song.model.Section.Companion.toLyrics
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
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

    var state by mutableStateOf(LyricsState(songId = songId))
        private set

    private val eventChannel = Channel<LyricsEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            state = state.copy(
                textScale = prefsRepository.getTextScale(),
                mode = prefsRepository.getMode()?.let(LyricsMode::valueOf) ?: LyricsMode.Inline,
            )

            songRepository.getSongById(songId).collect { stateResult ->
                stateResult.data?.let { song ->
                    state = state.copy(
                        title = song.title,
                        artist = song.artist,
                        syncStatus = stateResult.status,
                        sections = song.sections,
                    )
                }
            }
        }
    }

    fun onEditSongClicked() {
        eventChannel.trySend(
            LyricsEvent.NavigateToImportSong(
                songId = songId,
                title = state.title,
                artist = state.artist,
                lyrics = state.sections.toLyrics(),
            ),
        )
    }

    fun onTextScaleChanged(textScale: Int) {
        state = state.copy(textScale = textScale.coerceIn(MIN_ZOOM, MAX_ZOOM))
        viewModelScope.launch {
            prefsRepository.setTextScale(state.textScale)
        }
    }

    fun onKeyOffsetChanged(keyOffset: Int) {
        state = state.copy(keyOffset = keyOffset)
    }

    fun onModeChanged(mode: LyricsMode) {
        state = state.copy(mode = mode)
        viewModelScope.launch {
            prefsRepository.setMode(mode.name)
        }
    }

    fun deleteSong() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
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
