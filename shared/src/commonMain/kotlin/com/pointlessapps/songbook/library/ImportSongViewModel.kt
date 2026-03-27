package com.pointlessapps.songbook.library

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessapps.songbook.core.repository.SongRepository
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
    val showCamera: Boolean = false,
)

internal class ImportSongViewModel(
    private val songRepository: SongRepository,
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

    fun onCameraRequested() {
        state = state.copy(showCamera = true)
    }

    fun onCameraCaptureDone(bytes: ByteArray?) {
        state = state.copy(showCamera = false)
        onImageCaptured(bytes)
    }

    fun onImageCaptured(bytes: ByteArray?) {
        viewModelScope.launch {
        }
    }

    fun onManualInputConfirmed() {
    }

    fun onBack() {
        viewModelScope.launch {
            eventChannel.send(ImportSongEvent.Back)
        }
    }
}
