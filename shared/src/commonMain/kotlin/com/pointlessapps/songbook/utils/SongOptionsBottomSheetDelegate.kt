package com.pointlessapps.songbook.utils

import androidx.compose.runtime.Stable
import com.pointlessapps.songbook.core.queue.QueueManager
import com.pointlessapps.songbook.core.setlist.model.Setlist
import com.pointlessapps.songbook.core.song.SongRepository
import com.pointlessapps.songbook.core.song.model.Song
import com.pointlessapps.songbook.utils.SongOptionsBottomSheetState.Loaded
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Stable
internal interface SongOptionsBottomSheetDelegate {
    val songState: StateFlow<SongOptionsBottomSheetState>
    val songEvents: Flow<SongOptionsBottomSheetEvent>

    fun init(coroutineScope: CoroutineScope)

    fun onSongLongClicked(song: Song)
    fun onSongEditClicked()
    fun onSongSetlistsSelected(setlists: List<Setlist>)
    fun onSongAddToQueueClicked()
    fun onSongAddToFavouritesClicked()
    fun onSongDeleteClicked()
}

internal class SongOptionsBottomSheetDelegateImpl(
    private val queueManager: QueueManager,
    private val songRepository: SongRepository,
) : SongOptionsBottomSheetDelegate {

    private lateinit var coroutineScope: CoroutineScope

    private val _songEvents = Channel<SongOptionsBottomSheetEvent>()
    override val songEvents = _songEvents.receiveAsFlow()

    private val _songState = MutableStateFlow<SongOptionsBottomSheetState>(
        value = SongOptionsBottomSheetState.Empty,
    )
    override val songState = _songState.asStateFlow()

    override fun init(coroutineScope: CoroutineScope) {
        this.coroutineScope = coroutineScope
    }

    override fun onSongLongClicked(song: Song) {
        _songState.update { Loaded(song, persistentMapOf()) }
        coroutineScope.launch {
            songRepository.getSongSetlistsById(song.id).firstOrNull()?.let { setlists ->
                _songState.update { Loaded(song, setlists) }
            }
        }
    }

    override fun onSongEditClicked() {
        val state = songState.value as? Loaded ?: return

        _songEvents.trySend(
            SongOptionsBottomSheetEvent.NavigateToImportSong(
                songId = state.song.id,
                title = state.song.title,
                artist = state.song.artist,
                lyrics = state.song.lyrics,
            ),
        )
    }

    override fun onSongSetlistsSelected(setlists: List<Setlist>) {
        val state = songState.value as? Loaded ?: return

        coroutineScope.launch {
            songRepository.updateSongSetlists(
                id = state.song.id,
                setlistsIds = setlists.map(Setlist::id),
            )
        }
    }

    override fun onSongAddToQueueClicked() {
        val state = songState.value as? Loaded ?: return

        coroutineScope.launch {
            queueManager.addToQueue(state.song.id)
        }
    }

    override fun onSongAddToFavouritesClicked() {
        // TODO
    }

    override fun onSongDeleteClicked() {
        val state = songState.value as? Loaded ?: return

        coroutineScope.launch {
            queueManager.removeFromQueue(state.song.id)
            songRepository.deleteSong(state.song.id)
        }
    }
}

@Stable
internal sealed interface SongOptionsBottomSheetState {
    data object Empty : SongOptionsBottomSheetState
    data class Loaded(
        val song: Song,
        val setlists: ImmutableMap<Setlist, Boolean>,
    ) : SongOptionsBottomSheetState
}

internal sealed interface SongOptionsBottomSheetEvent {
    data class NavigateToImportSong(
        val songId: String,
        val title: String,
        val artist: String,
        val lyrics: String,
    ) : SongOptionsBottomSheetEvent
}
