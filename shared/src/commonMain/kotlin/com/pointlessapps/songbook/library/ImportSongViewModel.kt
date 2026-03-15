package com.pointlessapps.songbook.library

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessapps.songbook.Agent
import com.pointlessapps.songbook.Route
import com.pointlessapps.songbook.core.domain.models.ParsedLine
import com.pointlessapps.songbook.data.SongDao
import com.pointlessapps.songbook.data.SongEntity
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal sealed interface ImportSongEvent {
    data object Back : ImportSongEvent
    data class NavigateToLyrics(val songId: Long) : ImportSongEvent
}

internal data class ImportSongState(
    val title: String = "",
    val artist: String = "",
    val lyrics: String = "",
    val isLoading: Boolean = false,
)

internal class ImportSongViewModel(
    private val songDao: SongDao,
) : ViewModel() {

    var state by mutableStateOf(ImportSongState())
        private set

    private val eventChannel = Channel<ImportSongEvent>()
    val events = eventChannel.receiveAsFlow()

    fun updateTitle(title: String) {
        state = state.copy(title = title)
    }

    fun updateArtist(artist: String) {
        state = state.copy(artist = artist)
    }

    fun updateLyrics(lyrics: String) {
        state = state.copy(lyrics = lyrics)
    }

    fun onImageCaptured(bytes: ByteArray?) {
        viewModelScope.launch {
            bytes?.let {
                state = state.copy(isLoading = true)
                val songData = Agent.extractSongData(it)
                if (songData != null) {
                    state = state.copy(
                        title = songData.title ?: "",
                        artist = songData.author ?: "",
                        lyrics = songData.sections.joinToString("\n\n") { section ->
                            section.lines.joinToString("\n") { it.text }
                        },
                        isLoading = false,
                    )
                } else {
                    state = state.copy(isLoading = false)
                }
            }
        }
    }

    fun onManualInputConfirmed() {
        viewModelScope.launch {
            val sections = listOf(state.lyrics.split("\n").map { ParsedLine(it) })
            val entity = SongEntity(
                title = state.title,
                artist = state.artist,
                lyrics = state.lyrics,
                sections = sections,
            )
            val id = songDao.insertSong(entity)
            eventChannel.send(ImportSongEvent.NavigateToLyrics(id))
        }
    }

    fun onBack() {
        viewModelScope.launch {
            eventChannel.send(ImportSongEvent.Back)
        }
    }
}
