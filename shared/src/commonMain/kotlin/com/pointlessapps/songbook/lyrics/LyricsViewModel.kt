package com.pointlessapps.songbook.lyrics

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessapps.songbook.Route
import com.pointlessapps.songbook.core.domain.models.ChordMarker
import com.pointlessapps.songbook.core.domain.models.ParsedLine
import com.pointlessapps.songbook.data.SongDao
import com.pointlessapps.songbook.data.SongEntity
import com.pointlessapps.songbook.ui.components.NavigationDestination
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal sealed interface LyricsEvent {
    data class NavigateTo(val route: Route) : LyricsEvent
}

internal data class LyricsState(
    val songId: Long? = null,
    val title: String = "Untitled Song",
    val artist: String = "Unknown Artist",
    val selectedDestination: NavigationDestination = NavigationDestination.NowPlaying,
    val transposition: Int = 0,
    val isOcrActive: Boolean = false,
    val parsedSections: List<List<ParsedLine>>? = null,
    val popupState: PopupState? = null,
    val isLoading: Boolean = false,
)

internal data class PopupState(
    val sectionIndex: Int,
    val lineIndex: Int,
    val charIndex: Int,
    val offset: Offset,
    val editingMarker: ChordMarker? = null,
)

internal class LyricsViewModel(
    private val songId: Long? = null,
    private val songDao: SongDao,
) : ViewModel() {

    var state by mutableStateOf(LyricsState(songId = songId))
        private set

    private val eventChannel = Channel<LyricsEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        songId?.let { id ->
            viewModelScope.launch {
                songDao.getSongById(id)?.let { song ->
                    state = state.copy(
                        title = song.title,
                        artist = song.artist,
                        parsedSections = song.sections,
                    )
                }
            }
        }
    }

    fun onDestinationSelected(destination: NavigationDestination) {
        state = state.copy(selectedDestination = destination)
    }

    fun transposeUp() {
        state = state.copy(transposition = state.transposition + 1)
    }

    fun transposeDown() {
        state = state.copy(transposition = state.transposition - 1)
    }

    fun resetTransposition() {
        state = state.copy(transposition = 0)
    }

    fun setOcrActive(active: Boolean) {
        state = state.copy(isOcrActive = active)
    }

    fun onOcrCompleted(sections: List<List<ParsedLine>>?) {
        state = state.copy(
            parsedSections = sections,
            isOcrActive = false,
        )
    }

    fun onCursorFinalized(sectionIndex: Int, lineIndex: Int, charIndex: Int, offset: Offset) {
        state = state.copy(
            popupState = PopupState(
                sectionIndex = sectionIndex,
                lineIndex = lineIndex,
                charIndex = charIndex,
                offset = offset,
            ),
        )
    }

    fun onChordClicked(sectionIndex: Int, lineIndex: Int, marker: ChordMarker, offset: Offset) {
        state = state.copy(
            popupState = PopupState(
                sectionIndex = sectionIndex,
                lineIndex = lineIndex,
                charIndex = marker.offset,
                offset = offset,
                editingMarker = marker,
            ),
        )
    }

    fun dismissPopup() {
        state = state.copy(popupState = null)
    }

    fun onChordSelected(chord: com.pointlessapps.songbook.core.domain.models.Chord) {
        val popupState = state.popupState ?: return
        val sectionIndex = popupState.sectionIndex
        val lineIndex = popupState.lineIndex

        val newSections = state.parsedSections?.mapIndexed { sIndex, sLines ->
            if (sIndex == sectionIndex) {
                sLines.mapIndexed { lIndex, lData ->
                    if (lIndex == lineIndex) {
                        if (popupState.editingMarker != null) {
                            lData.copy(
                                chords = lData.chords.map {
                                    if (it == popupState.editingMarker) it.copy(chord = chord) else it
                                },
                            )
                        } else {
                            lData.copy(
                                chords = (lData.chords + ChordMarker(
                                    chord,
                                    popupState.charIndex,
                                )).sortedBy { it.offset },
                            )
                        }
                    } else lData
                }
            } else sLines
        }

        state = state.copy(
            parsedSections = newSections,
            popupState = null,
        )

        saveSong()
    }

    private fun saveSong() {
        viewModelScope.launch {
            val sections = state.parsedSections ?: return@launch
            val entity = SongEntity(
                id = state.songId ?: 0,
                title = state.title,
                artist = state.artist,
                lyrics = sections.flatten().joinToString("\n") { it.text },
                key = "C Major", // Default for now
                duration = "4:00",
                bpm = 120,
                sections = sections,
            )
            val newId = if (state.songId == null || state.songId == 0L) {
                songDao.insertSong(entity)
            } else {
                songDao.updateSong(entity)
                state.songId
            }
            state = state.copy(songId = newId)
        }
    }
}
