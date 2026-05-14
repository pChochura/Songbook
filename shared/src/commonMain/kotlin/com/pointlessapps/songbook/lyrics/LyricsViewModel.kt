package com.pointlessapps.songbook.lyrics

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.pointlessapps.songbook.core.prefs.PrefsRepository
import com.pointlessapps.songbook.core.queue.QueueManager
import com.pointlessapps.songbook.core.setlist.model.Setlist
import com.pointlessapps.songbook.core.song.LyricsParser
import com.pointlessapps.songbook.core.song.SongRepository
import com.pointlessapps.songbook.core.song.model.Section
import com.pointlessapps.songbook.core.song.model.Song
import com.pointlessapps.songbook.core.sync.SyncRepository
import com.pointlessapps.songbook.core.sync.model.SyncStatus
import com.pointlessapps.songbook.core.utils.Keep
import com.pointlessapps.songbook.core.utils.emptyImmutableList
import com.pointlessapps.songbook.utils.BaseViewModel
import com.pointlessapps.songbook.utils.SongOptionsBottomSheetDelegate
import com.pointlessapps.songbook.utils.SongbookSnackbarState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
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

internal sealed interface LyricsEvent {
    data object NavigateBack : LyricsEvent
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
        val song: Song,
        val sections: ImmutableList<Section> = emptyImmutableList(),
        val textScale: Int = 100,
        val keyOffset: Int = 0,
        val isOcrActive: Boolean = false,
        val displayMode: DisplayMode = DisplayMode.Inline,
        val wrapMode: WrapMode = WrapMode.NoWrap,
        val previousSongTitle: String? = null,
        val nextSongTitle: String? = null,
        val showKeyOffsetFab: Boolean = true,
        val selectedSetlists: ImmutableMap<Setlist, Boolean> = persistentMapOf(),
        val syncStatus: SyncStatus = SyncStatus.LOCAL,
    ) : LyricsState

    data object Loading : LyricsState

    val loaded: Loaded get() = this as Loaded
}

internal class LyricsViewModel(
    syncRepository: SyncRepository,
    private val queueManager: QueueManager,
    private val prefsRepository: PrefsRepository,
    private val songRepository: SongRepository,
    snackbarState: SongbookSnackbarState,
    private val songOptionsBottomSheetDelegate: SongOptionsBottomSheetDelegate,
) : BaseViewModel(snackbarState), SongOptionsBottomSheetDelegate by songOptionsBottomSheetDelegate {

    @Stable
    private data class LyricsTransientState(
        val keyOffset: Int = 0,
    )

    private val _transientState = MutableStateFlow(LyricsTransientState())

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<LyricsState> = combine(
        queueManager.currentSongFlow,
        queueManager.previousSongFlow,
        queueManager.nextSongFlow,
    ) { currentSong, previousSong, nextSong ->
        Triple(currentSong, previousSong, nextSong)
    }.flatMapLatest { (currentSong, previousSong, nextSong) ->
        if (currentSong == null) {
            eventChannel.send(LyricsEvent.NavigateBack)

            return@flatMapLatest flowOf(LyricsState.Loading)
        }

        combine(
            syncRepository.currentSyncStatusFlow,
            songRepository.getSongSetlistsById(currentSong.id),
            prefsRepository.getLyricsTextScaleFlow(),
            prefsRepository.getLyricsDisplayModeFlow(),
            prefsRepository.getLyricsWrapModeFlow(),
            prefsRepository.getShowKeyOffsetFabFlow(),
            _transientState,
        ) { syncStatus, selectedSetlists, textScale, displayMode, wrapMode, showKeyOffsetFab, transient ->
            LyricsState.Loaded(
                song = currentSong,
                sections = LyricsParser.parseLyrics(currentSong.lyrics),
                textScale = textScale,
                displayMode = displayMode?.let(DisplayMode::valueOf) ?: DisplayMode.Inline,
                wrapMode = wrapMode?.let(WrapMode::valueOf) ?: WrapMode.NoWrap,
                keyOffset = transient.keyOffset,
                previousSongTitle = previousSong?.title,
                nextSongTitle = nextSong?.title,
                showKeyOffsetFab = showKeyOffsetFab,
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

    init {
        songOptionsBottomSheetDelegate.init(viewModelScope)
    }

    fun onPreviousSongRequested(): Boolean {
        return queueManager.goToPreviousSong()
    }

    fun onNextSongRequested(): Boolean {
        return queueManager.goToNextSong()
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

    fun onBroadcastToTeamConfirmClicked() {
        // TODO
    }

    override fun onSongDeleteClicked() {
        viewModelScope.launch {
            queueManager.removeFromQueue(state.value.loaded.song.id)
            songRepository.deleteSong(state.value.loaded.song.id)
        }
    }

    companion object {
        const val MIN_ZOOM = 100
        const val MAX_ZOOM = 300
    }
}
