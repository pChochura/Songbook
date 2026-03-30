package com.pointlessapps.songbook.library

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessapps.songbook.Agent
import com.pointlessapps.songbook.core.song.SongRepository
import com.pointlessapps.songbook.core.song.model.NewSong
import com.pointlessapps.songbook.core.song.model.Section
import com.pointlessapps.songbook.model.SongData
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal sealed interface ImportSongEvent {
    data object Back : ImportSongEvent
    data class NavigateToLyrics(val songId: Long) : ImportSongEvent
}

internal data class ImportSongState(
    val sections: List<Section> = emptyList(),
    val isLoading: Boolean = false,
    val showCamera: Boolean = false,
)

internal class ImportSongViewModel(
    private val agent: Agent,
    private val songRepository: SongRepository,
) : ViewModel() {

    val titleTextFieldState = TextFieldState("")
    val artistTextFieldState = TextFieldState("")
    val lyricsTextFieldState = TextFieldState("")

    var state by mutableStateOf(ImportSongState())
        private set

    private val eventChannel = Channel<ImportSongEvent>()
    val events = eventChannel.receiveAsFlow()

    fun onCameraRequested() {
        state = state.copy(showCamera = true)
    }

    fun onCameraCaptureDone(bytes: ByteArray?) {
        state = state.copy(showCamera = false)
        onImageCaptured(bytes)
    }

    fun onImageCaptured(bytes: ByteArray?) {
        viewModelScope.launch {
            bytes?.let {
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
                            chords = it.chordsBeside,
                        )
                    },
                )
            }
        }
    }

    fun onManualInputConfirmed() {
        viewModelScope.launch {
            songRepository.saveSong(
                NewSong(
                    title = titleTextFieldState.text.toString(),
                    artist = artistTextFieldState.text.toString(),
                    sections = state.sections,
                ),
            )
        }
    }

    fun onBack() {
        viewModelScope.launch {
            eventChannel.send(ImportSongEvent.Back)
        }
    }
}
