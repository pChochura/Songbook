package com.pointlessapps.songbook.setlist

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.pointlessapps.songbook.core.queue.QueueManager
import com.pointlessapps.songbook.core.setlist.SetlistRepository
import com.pointlessapps.songbook.core.setlist.model.Setlist
import com.pointlessapps.songbook.core.song.SongRepository
import com.pointlessapps.songbook.core.song.model.Song
import com.pointlessapps.songbook.core.song.model.SongSearchResult
import com.pointlessapps.songbook.core.sync.SyncRepository
import com.pointlessapps.songbook.core.sync.model.SyncStatus
import com.pointlessapps.songbook.core.utils.emptyImmutableList
import com.pointlessapps.songbook.shared.ui.Res
import com.pointlessapps.songbook.shared.ui.common_undo
import com.pointlessapps.songbook.shared.ui.error_setlist_not_found
import com.pointlessapps.songbook.shared.ui.setlist_song_removed_from_setlist
import com.pointlessapps.songbook.ui.theme.IconInfo
import com.pointlessapps.songbook.ui.theme.IconWarning
import com.pointlessapps.songbook.utils.BaseViewModel
import com.pointlessapps.songbook.utils.SongOptionsBottomSheetDelegate
import com.pointlessapps.songbook.utils.SongbookSnackbarCallbackAction
import com.pointlessapps.songbook.utils.SongbookSnackbarState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import kotlin.time.Duration.Companion.milliseconds

internal sealed interface SetlistEvent {
    data object NavigateBack : SetlistEvent
    data object NavigateToLyrics : SetlistEvent
}

@Stable
internal sealed interface SetlistState {
    @Stable
    data object Loading : SetlistState

    @Stable
    data class Loaded(
        val setlist: Setlist,
        val songs: ImmutableList<Song>,
        val syncStatus: SyncStatus = SyncStatus.LOCAL,
    ) : SetlistState

    val loaded: Loaded get() = this as Loaded
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
internal class SetlistViewModel(
    id: String,
    syncRepository: SyncRepository,
    private val queueManager: QueueManager,
    private val setlistRepository: SetlistRepository,
    private val songRepository: SongRepository,
    private val snackbarState: SongbookSnackbarState,
    songOptionsBottomSheetDelegate: SongOptionsBottomSheetDelegate,
) : BaseViewModel(snackbarState), SongOptionsBottomSheetDelegate by songOptionsBottomSheetDelegate {

    @Stable
    private data class SetlistTransientState(
        val localSongs: ImmutableList<Song> = emptyImmutableList(),
        val isLoading: Boolean = false,
    )

    private val _transientState = MutableStateFlow(SetlistTransientState())

    val state: StateFlow<SetlistState> = combine(
        setlistRepository.getSetlistsSongsById(id)
            .onEach { updateLocalSongs(it) }
            .map {}.distinctUntilChanged(),
        syncRepository.currentSyncStatusFlow,
        setlistRepository.getSetlistByIdFlow(id),
        _transientState,
    ) { _, syncStatus, setlist, transient ->
        if (transient.isLoading) {
            return@combine SetlistState.Loading
        }

        if (setlist == null) {
            snackbarState.showSnackbar(
                message = getString(Res.string.error_setlist_not_found),
                icon = IconWarning,
            )
            eventChannel.trySend(SetlistEvent.NavigateBack)

            return@combine SetlistState.Loading
        }

        SetlistState.Loaded(
            setlist = setlist,
            songs = transient.localSongs,
            syncStatus = syncStatus,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SetlistState.Loading,
    )

    val songSearchQueryTextFieldState: TextFieldState = TextFieldState()
    val songSearchResults: Flow<PagingData<SongSearchResult>> = snapshotFlow {
        songSearchQueryTextFieldState.text
    }.distinctUntilChanged()
        .debounce(SEARCH_QUERY_DEBOUNCE.milliseconds)
        .flatMapLatest { songRepository.searchSongs(it.toString()) }
        .cachedIn(viewModelScope)

    private val eventChannel = Channel<SetlistEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        songOptionsBottomSheetDelegate.init(viewModelScope)
    }

    fun onLyricsClicked(songId: String) {
        viewModelScope.launch {
            queueManager.setQueue(
                songsIds = state.value.loaded.songs.map { it.id },
                currentSongId = songId,
            )
            eventChannel.send(SetlistEvent.NavigateToLyrics)
        }
    }

    fun onNameChanged(name: String) {
        viewModelScope.launch {
            val setlist = (state.value as SetlistState.Loaded).setlist
            setlistRepository.updateSetlistName(setlist.id, name)
        }
    }

    fun onMove(fromIndex: Int, toIndex: Int) {
        val state = state.value.loaded
        val songs = state.songs.toMutableList()
        if (fromIndex !in songs.indices || toIndex !in songs.indices) return

        val song = songs.removeAt(fromIndex)
        songs.add(toIndex, song)

        _transientState.update { it.copy(localSongs = songs.toImmutableList()) }
    }

    fun onReorderDone() {
        viewModelScope.launch {
            val state = state.value.loaded
            setlistRepository.updateSetlistSongs(
                id = state.setlist.id,
                songsIds = state.songs.map(Song::id),
            )
        }
    }

    fun onDeleteSetlistConfirmClicked() {
        viewModelScope.launch {
            val state = state.value.loaded
            _transientState.update { it.copy(isLoading = true) }
            setlistRepository.deleteSetlist(state.setlist.id)
            eventChannel.trySend(SetlistEvent.NavigateBack)
        }
    }

    fun onAddSongToSetlistClicked(id: String) {
        viewModelScope.launch {
            val state = state.value.loaded
            setlistRepository.addSongToSetlist(state.setlist.id, id, state.songs.size)
        }
    }

    fun onRemoveSongFromSetlistClicked(id: String) {
        viewModelScope.launch {
            val state = state.value.loaded
            val songIndex = state.songs.indexOfFirst { it.id == id }
            setlistRepository.removeSongFromSetlist(state.setlist.id, id)
            _transientState.update {
                it.copy(
                    localSongs = it.localSongs
                        .filter { song -> song.id != id }
                        .toImmutableList(),
                )
            }
            snackbarState.showSnackbar(
                message = getString(Res.string.setlist_song_removed_from_setlist),
                icon = IconInfo,
                actionLabel = getString(Res.string.common_undo),
                callbackAction = SongbookSnackbarCallbackAction.AddSongToSetlist(
                    setlistId = state.setlist.id,
                    songId = id,
                    order = songIndex,
                ),
            )
        }
    }

    private fun updateLocalSongs(songs: ImmutableList<Song>) {
        viewModelScope.launch {
            _transientState.update {
                it.copy(localSongs = songs)
            }
        }
    }

    private companion object {
        const val SEARCH_QUERY_DEBOUNCE = 300L
    }
}
