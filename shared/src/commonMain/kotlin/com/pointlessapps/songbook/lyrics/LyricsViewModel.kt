package com.pointlessapps.songbook.lyrics

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessapps.songbook.core.song.SongRepository
import com.pointlessapps.songbook.core.song.model.Section
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal sealed interface LyricsEvent

internal enum class LyricsMode {
    Inline,
    SideBySide,
    TextOnly,
}

internal data class LyricsState(
    val songId: Long? = null,
    val title: String = "Untitled Song",
    val artist: String = "Unknown Artist",
    val sections: List<Section> = emptyList(),
    val isOcrActive: Boolean = false,
    val mode: LyricsMode = LyricsMode.Inline,
    val isLoading: Boolean = false,
)

internal class LyricsViewModel(
    private val songId: Long,
    private val songRepository: SongRepository,
) : ViewModel() {

    var state by mutableStateOf(LyricsState(songId = songId))
        private set

    private val eventChannel = Channel<LyricsEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            songRepository.getSongById(songId).collect {
                it?.let { song ->
                    state = state.copy(
                        title = song.title,
                        artist = song.artist,
                        sections = song.sections,
                    )
                }
            }
        }
    }
}
