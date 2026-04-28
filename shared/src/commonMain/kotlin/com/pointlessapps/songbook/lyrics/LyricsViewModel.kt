package com.pointlessapps.songbook.lyrics

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.pointlessapps.songbook.core.prefs.PrefsRepository
import com.pointlessapps.songbook.core.queue.QueueManager
import com.pointlessapps.songbook.core.setlist.SetlistRepository
import com.pointlessapps.songbook.core.setlist.model.Setlist
import com.pointlessapps.songbook.core.song.LyricsParser
import com.pointlessapps.songbook.core.song.SongRepository
import com.pointlessapps.songbook.core.song.model.Section
import com.pointlessapps.songbook.core.song.model.Section.Companion.toLyrics
import com.pointlessapps.songbook.core.sync.SyncRepository
import com.pointlessapps.songbook.core.sync.model.SyncStatus
import com.pointlessapps.songbook.core.utils.Keep
import com.pointlessapps.songbook.core.utils.emptyImmutableList
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.error_song_not_found
import com.pointlessapps.songbook.ui.theme.IconWarning
import com.pointlessapps.songbook.utils.BaseViewModel
import com.pointlessapps.songbook.utils.SongbookSnackbarState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

internal sealed interface LyricsEvent {
    data object NavigateBack : LyricsEvent
    data class NavigateToImportSong(
        val songId: String,
        val title: String,
        val artist: String,
        val lyrics: String,
    ) : LyricsEvent
}

@Keep
internal enum class DisplayMode {
    Inline,
    SideBySide,
    Both,
    TextOnly;

    val shouldShowInline get() = this == Inline || this == Both
    val shouldShowSideBySide get() = this == SideBySide || this == Both
}

@Keep
internal enum class WrapMode { Wrap, NoWrap }

internal sealed interface LyricsState {
    @Stable
    data class Loaded(
        val songId: String,
        val title: String = "",
        val artist: String = "",
        val sections: ImmutableList<Section> = emptyImmutableList(),
        val textScale: Int = 100,
        val keyOffset: Int = 0,
        val isOcrActive: Boolean = false,
        val displayMode: DisplayMode = DisplayMode.Inline,
        val wrapMode: WrapMode = WrapMode.NoWrap,
        val previousSongTitle: String? = null,
        val nextSongTitle: String? = null,
        val showKeyOffsetFab: Boolean = true,
        val allSetlists: ImmutableList<Setlist> = emptyImmutableList(),
        val selectedSetlists: ImmutableList<Setlist> = emptyImmutableList(),
        val syncStatus: SyncStatus = SyncStatus.LOCAL,
    ) : LyricsState {
        val setlistsSelection: ImmutableMap<Setlist, Boolean> =
            allSetlists.associateWith { it.id in selectedSetlists.map(Setlist::id) }
                .toImmutableMap()
    }

    data object Loading : LyricsState

    val loaded: Loaded get() = this as Loaded
}

internal class LyricsViewModel(
    syncRepository: SyncRepository,
    private val queueManager: QueueManager,
    private val prefsRepository: PrefsRepository,
    private val songRepository: SongRepository,
    private val setlistRepository: SetlistRepository,
    private val snackbarState: SongbookSnackbarState,
) : BaseViewModel(snackbarState) {

    @Stable
    private data class LyricsTransientState(
        val keyOffset: Int = 0,
        val isLoading: Boolean = false,
    )

    private val _transientState = MutableStateFlow(LyricsTransientState())

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<LyricsState> = combine(
        queueManager.queueFlow,
        queueManager.currentSongFlow,
    ) { queue, songId -> queue to songId }.flatMapLatest { (queue, song) ->
        if (song == null) {
            snackbarState.showSnackbar(
                message = getString(Res.string.error_song_not_found),
                icon = IconWarning,
            )
            eventChannel.trySend(LyricsEvent.NavigateBack)

            return@flatMapLatest flowOf(LyricsState.Loading)
        }

        combine(
            syncRepository.currentSyncStatusFlow,
            setlistRepository.getAllSetlistsFlow(),
            songRepository.getSongSetlistsById(song.id),
            prefsRepository.getLyricsTextScaleFlow(),
            prefsRepository.getLyricsDisplayModeFlow(),
            prefsRepository.getLyricsWrapModeFlow(),
            prefsRepository.getShowKeyOffsetFabFlow(),
            _transientState,
        ) { syncStatus, allSetlists, selectedSetlists, textScale, displayMode, wrapMode, showKeyOffsetFab, transient ->
            if (transient.isLoading) {
                return@combine LyricsState.Loading
            }

            LyricsState.Loaded(
                songId = song.id,
                title = song.title,
                artist = song.artist,
                sections = LyricsParser.parseLyrics(song.lyrics),
                textScale = textScale,
                displayMode = displayMode?.let(DisplayMode::valueOf) ?: DisplayMode.Inline,
                wrapMode = wrapMode?.let(WrapMode::valueOf) ?: WrapMode.NoWrap,
                keyOffset = transient.keyOffset,
                previousSongTitle = queueManager.peekPreviousSong()?.title,
                nextSongTitle = queueManager.peekNextSong()?.title,
                showKeyOffsetFab = showKeyOffsetFab,
                allSetlists = allSetlists,
                selectedSetlists = selectedSetlists,
                syncStatus = syncStatus,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LyricsState.Loading,
    )

    private val eventChannel = Channel<LyricsEvent>()
    val events = eventChannel.receiveAsFlow()

    fun onPreviousSongRequested() {
        queueManager.goToPreviousSong()
    }

    fun onNextSongRequested() {
        queueManager.goToNextSong()
    }

    fun onEditSongClicked() {
        val state = state.value.loaded

        eventChannel.trySend(
            LyricsEvent.NavigateToImportSong(
                songId = state.songId,
                title = state.title,
                artist = state.artist,
                lyrics = state.sections.toLyrics(),
            ),
        )
    }

    fun onTextScaleChanged(textScale: Int) {
        viewModelScope.launch {
            prefsRepository.setLyricsTextScale(textScale.coerceIn(MIN_ZOOM, MAX_ZOOM))
        }
    }

    fun onKeyOffsetChanged(keyOffset: Int) {
        _transientState.update { it.copy(keyOffset = keyOffset) }
    }

    fun onShowKeyOffsetFabChanged(showKeyOffsetFab: Boolean) {
        viewModelScope.launch {
            prefsRepository.setShowKeyOffsetFab(showKeyOffsetFab)
        }
    }

    fun onDisplayModeChanged(mode: DisplayMode) {
        viewModelScope.launch {
            prefsRepository.setLyricsDisplayMode(mode.name)
        }
    }

    fun onWrapModeChanged(mode: WrapMode) {
        viewModelScope.launch {
            prefsRepository.setLyricsWrapMode(mode.name)
        }
    }

    fun onSetlistsSelected(setlists: List<Setlist>) {
        viewModelScope.launch {
            songRepository.updateSongSetlists(
                id = state.value.loaded.songId,
                setlistsIds = setlists.map { it.id },
            )
        }
    }

    fun onBroadcastToTeamConfirmClicked() {
        // TODO
    }

    fun onDeleteSongConfirmClicked() {
        val state = state.value.loaded

        viewModelScope.launch {
            _transientState.update { it.copy(isLoading = true) }
            songRepository.deleteSong(state.songId)
            eventChannel.send(LyricsEvent.NavigateBack)
        }
    }

    companion object {
        const val MIN_ZOOM = 100
        const val MAX_ZOOM = 300
    }
}
