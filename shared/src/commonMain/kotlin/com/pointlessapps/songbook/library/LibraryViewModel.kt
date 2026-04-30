package com.pointlessapps.songbook.library

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.pointlessapps.songbook.core.app.AppRepository
import com.pointlessapps.songbook.core.auth.AuthRepository
import com.pointlessapps.songbook.core.auth.exceptions.AccountAlreadyLinkedException
import com.pointlessapps.songbook.core.auth.model.LoginStatus
import com.pointlessapps.songbook.core.prefs.PrefsRepository
import com.pointlessapps.songbook.core.queue.QueueManager
import com.pointlessapps.songbook.core.setlist.SetlistRepository
import com.pointlessapps.songbook.core.setlist.model.Setlist
import com.pointlessapps.songbook.core.song.SongRepository
import com.pointlessapps.songbook.core.song.model.Song
import com.pointlessapps.songbook.core.sync.SyncRepository
import com.pointlessapps.songbook.core.sync.model.SyncStatus
import com.pointlessapps.songbook.core.utils.emptyImmutableList
import com.pointlessapps.songbook.shared.ui.Res
import com.pointlessapps.songbook.shared.ui.error_account_already_linked_error
import com.pointlessapps.songbook.ui.theme.IconWarning
import com.pointlessapps.songbook.utils.BaseViewModel
import com.pointlessapps.songbook.utils.Keep
import com.pointlessapps.songbook.utils.SongbookSnackbarState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal sealed interface LibraryEvent {
    data object NavigateToIntroduction : LibraryEvent
    data object NavigateToImportSong : LibraryEvent
    data object NavigateToLyrics : LibraryEvent
    data class NavigateToSetlist(val id: String) : LibraryEvent
}

@Keep
internal enum class DisplayMode { List, Grid }

@Stable
internal data class LibraryState(
    val setlists: ImmutableList<Setlist> = emptyImmutableList(),
    val initialFilterLetter: String? = null,
    val displayMode: DisplayMode = DisplayMode.Grid,
    val syncStatus: SyncStatus = SyncStatus.LOCAL,
    val loginStatus: LoginStatus = LoginStatus.ANONYMOUS,
    val isLoading: Boolean = false,
)

internal class LibraryViewModel(
    initialFilterLetter: String?,
    private val appRepository: AppRepository,
    private val queueManager: QueueManager,
    private val syncRepository: SyncRepository,
    private val songRepository: SongRepository,
    private val setlistRepository: SetlistRepository,
    private val prefsRepository: PrefsRepository,
    private val authRepository: AuthRepository,
    private val snackbarState: SongbookSnackbarState,
) : BaseViewModel(snackbarState) {

    @Stable
    private data class LibraryTransientState(
        val initialFilterLetter: String? = null,
        val isLoading: Boolean = false,
    )

    private val _transientState = MutableStateFlow(
        LibraryTransientState(initialFilterLetter = initialFilterLetter),
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<LibraryState> = combine(
        syncRepository.currentSyncStatusFlow,
        authRepository.currentLoginStatusFlow,
        setlistRepository.getAllSetlistsFlow(limit = SETLISTS_LIMIT),
        prefsRepository.getLibraryDisplayModeFlow(),
        _transientState,
    ) { syncStatus, loginStatus, setlists, displayMode, transient ->
        LibraryState(
            setlists = setlists,
            initialFilterLetter = transient.initialFilterLetter,
            displayMode = displayMode?.let(DisplayMode::valueOf) ?: DisplayMode.Grid,
            syncStatus = syncStatus,
            loginStatus = loginStatus,
            isLoading = transient.isLoading,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LibraryState(),
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val songs = _transientState.map { it.initialFilterLetter }
        .distinctUntilChanged()
        .flatMapLatest { songRepository.getAllSongs(it) }
        .cachedIn(viewModelScope)

    private val eventChannel = Channel<LibraryEvent>(BUFFERED)
    val events = eventChannel.receiveAsFlow()

    fun onClearInitialFilterLetterClicked() {
        _transientState.update { it.copy(initialFilterLetter = null) }
    }

    fun onImportSongClicked() {
        eventChannel.trySend(LibraryEvent.NavigateToImportSong)
    }

    fun onLyricsClicked(song: Song) {
        viewModelScope.launch {
            queueManager.setQueue(listOf(song), song)
            eventChannel.send(LibraryEvent.NavigateToLyrics)
        }
    }

    fun onSetlistClicked(id: String) {
        eventChannel.trySend(LibraryEvent.NavigateToSetlist(id))
    }

    fun onAddSetlistClicked(name: String) {
        viewModelScope.launch {
            val id = setlistRepository.addSetlist(name)
            eventChannel.send(LibraryEvent.NavigateToSetlist(id))
        }
    }

    fun loginClicked() {
        viewModelScope.launch {
            try {
                authRepository.linkWithGoogle()
            } catch (_: AccountAlreadyLinkedException) {
                snackbarState.showSnackbar(
                    message = Res.string.error_account_already_linked_error,
                    icon = IconWarning,
                    duration = SnackbarDuration.Long,
                )
            }
        }
    }

    fun logoutClicked() {
        viewModelScope.launch {
            authRepository.logout()
            syncRepository.clearDatabase()
            eventChannel.send(LibraryEvent.NavigateToIntroduction)
        }
    }

    fun removeAccountClicked() {
        viewModelScope.launch {
            authRepository.getTokens()?.let { (accessToken, refreshToken) ->
                appRepository.openRemoveAccountWebsite(accessToken, refreshToken)
            }
            authRepository.clearSession()
            syncRepository.clearDatabase()
            eventChannel.send(LibraryEvent.NavigateToIntroduction)
        }
    }

    fun onDisplayModeChanged(mode: DisplayMode) {
        viewModelScope.launch {
            prefsRepository.setLibraryDisplayMode(mode.name)
        }
    }

    private companion object {
        const val SETLISTS_LIMIT = 5L
    }
}
