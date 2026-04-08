package com.pointlessapps.songbook.setlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessapps.songbook.core.setlist.SetlistRepository
import com.pointlessapps.songbook.core.setlist.model.Setlist
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal sealed interface SetlistEvent {
    data object NavigateBack : SetlistEvent
    data class NavigateToLyrics(val songId: Long) : SetlistEvent
}

internal sealed interface SetlistState {
    data object Loading : SetlistState
    data class Loaded(val setlist: Setlist) : SetlistState
}

internal class SetlistViewModel(
    id: Long,
    private val setlistRepository: SetlistRepository,
) : ViewModel() {

    private val eventChannel = Channel<SetlistEvent>()
    val events = eventChannel.receiveAsFlow()

    val state: StateFlow<SetlistState> = setlistRepository.getSetlistByIdFlow(id)
        .map { (setlist) ->
            if (setlist == null) {
                // TODO show snackbar
                eventChannel.send(SetlistEvent.NavigateBack)

                return@map SetlistState.Loading
            }

            SetlistState.Loaded(setlist = setlist)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SetlistState.Loading,
        )

    fun onLyricsClicked(id: Long) {
        eventChannel.trySend(SetlistEvent.NavigateToLyrics(id))
    }

    fun onNameChanged(name: String) {
        viewModelScope.launch {
            val setlist = (state.value as SetlistState.Loaded).setlist
            setlistRepository.updateSetlist(setlist.copy(name = name))
        }
    }

    fun onDeleteSetlistConfirmClicked() {
        viewModelScope.launch {
            val setlist = (state.value as SetlistState.Loaded).setlist
            setlistRepository.deleteSetlist(setlist.id)
            eventChannel.send(SetlistEvent.NavigateBack)
        }
    }
}
