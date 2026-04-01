package com.pointlessapps.songbook.library

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessapps.songbook.Agent
import com.pointlessapps.songbook.core.model.SyncStatus
import com.pointlessapps.songbook.core.setlist.SetlistRepository
import com.pointlessapps.songbook.core.setlist.model.Setlist
import com.pointlessapps.songbook.core.song.LyricsParser
import com.pointlessapps.songbook.core.song.SongRepository
import com.pointlessapps.songbook.core.song.model.Chord
import com.pointlessapps.songbook.core.song.model.Section
import com.pointlessapps.songbook.model.SongData
import kotlinx.coroutines.channels.Channel
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
    val isLoading: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.LOCAL,
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
            setlistRepository.getAllSetlists()
                .collect {
                    state = state.copy(
                        allSetlists = it.data,
                        syncStatus = it.status,
                        isLoading = false,
                    )
                }
        }
    }

    fun onSetlistsSelected(setlists: List<Setlist>) {
        state = state.copy(
            selectedSetlists = setlists,
        )
    }

    fun onImageCaptured(bytes: ByteArray?) {
        bytes?.let {
            viewModelScope.launch {
                val result = agent.extractSongData(it)
                val data = result?.firstOrNull() ?: return@launch
                val sectionTypeCount = mutableMapOf<SongData.Section.Type, Int>()
                titleTextFieldState.setTextAndPlaceCursorAtEnd(data.title.orEmpty())
                artistTextFieldState.setTextAndPlaceCursorAtEnd(data.author.orEmpty())
                lyricsTextFieldState.setTextAndPlaceCursorAtEnd(
                    data.sections.joinToString("\n") {
                        "[${it.type.name}]\n${it.lines.joinToString("\n") { it.text }}"
                    },
                )
                state = state.copy(
                    sections = data.sections.map {
                        Section(
                            name = "${it.type.name} ${sectionTypeCount.getOrPut(it.type) { 0 } + 1}",
                            lyrics = it.lines.joinToString("\n") { it.text },
                            chords = it.chordsBeside.map { Chord(value = it, position = 0) },
                        )
                    },
                )
            }
        }
    }

    fun computeSections(): List<Section> {
        return LyricsParser.parseLyrics(lyricsTextFieldState.text.toString())
    }
}
