package com.pointlessapps.songbook.setlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessapps.songbook.core.setlist.SetlistRepository
import com.pointlessapps.songbook.core.setlist.model.Setlist
import com.pointlessapps.songbook.core.song.model.Song
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn

internal sealed interface SetlistEvent {
    data object NavigateBack : SetlistEvent
    data class NavigateToLyrics(val songId: Long) : SetlistEvent
}

internal data class SetlistState(
    val setlist: Setlist? = null,
    val songs: List<Song> = emptyList(),
)

internal class SetlistViewModel(
    id: Long,
    private val setlistRepository: SetlistRepository,
) : ViewModel() {

    private val eventChannel = Channel<SetlistEvent>()
    val events = eventChannel.receiveAsFlow()

    val state: StateFlow<SetlistState> = setlistRepository.getSetlistById(id)
        .map { dataState ->
            SetlistState(
                setlist = dataState.data,
                songs = dataState.data?.songs.orEmpty(),
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SetlistState(),
        )

    fun onLyricsClicked(id: Long) {
        eventChannel.trySend(SetlistEvent.NavigateToLyrics(id))
    }
}
