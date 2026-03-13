package com.pointlessapps.songbook.library

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessapps.songbook.Route
import com.pointlessapps.songbook.core.domain.models.ParsedLine
import com.pointlessapps.songbook.data.SongDao
import com.pointlessapps.songbook.data.SongEntity
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal sealed interface LibraryEvent {
    data class NavigateTo(val route: Route) : LibraryEvent
}

internal data class LibraryState(
    val songs: List<SongEntity> = emptyList(),
    val isLoading: Boolean = false,
    val totalSongs: Int = 0,
    val totalArtists: Int = 0,
    val showImportDialog: Boolean = false,
    val isOcrActive: Boolean = false,
    val ocrScannedText: String? = null,
)

internal class LibraryViewModel(
    private val songDao: SongDao,
) : ViewModel() {

    var state by mutableStateOf(LibraryState())
        private set

    private val eventChannel = Channel<LibraryEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            songDao.getAllSongs().collectLatest { songs ->
                state = state.copy(
                    songs = songs,
                    totalSongs = songs.size,
                    totalArtists = songs.distinctBy { it.artist }.size,
                    isLoading = false,
                )
            }
        }
    }

    fun showImportDialog() {
        state = state.copy(showImportDialog = true)
    }

    fun hideImportDialog() {
        state = state.copy(showImportDialog = false, ocrScannedText = null)
    }

    fun setOcrActive(active: Boolean) {
        state = state.copy(isOcrActive = active)
    }

    fun onOcrScanned(rawText: String) {
        state = state.copy(isOcrActive = false, showImportDialog = true, ocrScannedText = rawText)
    }

    fun onManualInputConfirmed(title: String, artist: String, lyricsText: String) {
        viewModelScope.launch {
            state = state.copy(showImportDialog = false, ocrScannedText = null)
            val sections = listOf(lyricsText.split("\n").map { ParsedLine(it) })
            val entity = SongEntity(
                title = title,
                artist = artist,
                lyrics = lyricsText,
                sections = sections,
            )
            val newId = songDao.insertSong(entity)
            eventChannel.send(LibraryEvent.NavigateTo(Route.Lyrics(newId)))
        }
    }
}
