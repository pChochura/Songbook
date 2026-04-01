package com.pointlessapps.songbook.lyrics

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessapps.songbook.core.song.SongRepository
import com.pointlessapps.songbook.core.song.model.Chord
import com.pointlessapps.songbook.core.song.model.Section
import com.pointlessapps.songbook.core.model.SyncStatus
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal sealed interface LyricsEvent {
    object NavigateBack : LyricsEvent
}

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
    val textScale: Int = 100,
    val keyOffset: Int = 0,
    val isOcrActive: Boolean = false,
    val mode: LyricsMode = LyricsMode.Inline,
    val isLoading: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.LOCAL,
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
            songRepository.getSongById(songId).collect { stateResult ->
                stateResult.data?.let { song ->
                    state = state.copy(
                        title = song.title,
                        artist = song.artist,
                        syncStatus = stateResult.status,
                        sections = listOf(
                            Section(
                                name = "Verse 1",
                                lyrics = """
                                    A wczora z wieczora
                                    A wczora z wieczora
                                    Z niebieskiego dwora
                                    Z niebieskiego dwora
                                """.trimIndent(),
                                chords = listOf(
                                    Chord("C", 2),
                                    Chord("G", 9),
                                    Chord("C", 15),

                                    Chord("A", 29),

                                    Chord("d", 44),
                                    Chord("G", 52),

                                    Chord("C", 65),
                                    Chord("G", 71),
                                    Chord("C", 76),
                                ),
                            ),
                            Section(
                                name = "Verse 2",
                                lyrics = """
                                    Przyszła nam nowina
                                    Przyszła nam nowina
                                    Panna rodzi Syna
                                    Panna rodzi Syna
                                """.trimIndent(),
                                chords = listOf(),
                            ),
                            Section(
                                name = "Verse 3",
                                lyrics = """
                                    Boga prawdziwego
                                    Boga prawdziwego
                                    Nieogarnionego
                                    Nieogarnionego
                                """.trimIndent(),
                                chords = listOf(),
                            ),
                            Section(
                                name = "Verse 4",
                                lyrics = """
                                    Za wyrokiem Boskim
                                    Za wyrokiem Boskim
                                    W Betlejem żydowskim
                                    W Betlejem żydowskim
                                """.trimIndent(),
                                chords = listOf(),
                            ),
                            Section(
                                name = "Verse 5",
                                lyrics = """
                                    Pastuszkowie mali
                                    Pastuszkowie mali
                                    W polu wtenczas spali
                                    W polu wtenczas spali
                                """.trimIndent(),
                                chords = listOf(),
                            ),
                            Section(
                                name = "Verse 6",
                                lyrics = """
                                    Gdy anioł z północy
                                    Gdy anioł z północy
                                    Światłość z nieba toczy
                                    Światłość z nieba toczy
                                """.trimIndent(),
                                chords = listOf(),
                            ),
                            Section(
                                name = "Verse 7",
                                lyrics = """
                                    Chwałę oznajmując
                                    Chwałę oznajmując
                                    Szopę pokazując
                                    Szopę pokazując
                                """.trimIndent(),
                                chords = listOf(),
                            ),
                            Section(
                                name = "Verse 8",
                                lyrics = """
                                    Gdzie Panna z Dzieciątkiem
                                    Gdzie Panna z Dzieciątkiem
                                    Z wolem i osłątkiem
                                    Z wolem i osłątkiem
                                """.trimIndent(),
                                chords = listOf(),
                            ),
                        ),
                    )
                }
            }
        }
    }

    fun onTextScaleChanged(textScale: Int) {
        state = state.copy(textScale = textScale.coerceIn(MIN_ZOOM, MAX_ZOOM))
    }

    fun onKeyOffsetChanged(keyOffset: Int) {
        state = state.copy(keyOffset = keyOffset)
    }

    fun onModeChanged(mode: LyricsMode) {
        state = state.copy(mode = mode)
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
