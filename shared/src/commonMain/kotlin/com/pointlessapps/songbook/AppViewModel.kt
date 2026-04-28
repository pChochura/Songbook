package com.pointlessapps.songbook

import androidx.lifecycle.viewModelScope
import com.pointlessapps.songbook.core.auth.AuthRepository
import com.pointlessapps.songbook.core.auth.model.LoginStatus
import com.pointlessapps.songbook.core.setlist.SetlistRepository
import com.pointlessapps.songbook.core.song.ChordLibrary
import com.pointlessapps.songbook.core.sync.SyncRepository
import com.pointlessapps.songbook.shared.ui.Res
import com.pointlessapps.songbook.shared.ui.error_initilizing_error
import com.pointlessapps.songbook.ui.theme.IconWarning
import com.pointlessapps.songbook.utils.BaseViewModel
import com.pointlessapps.songbook.utils.SongbookSnackbarState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.getString

internal class AppViewModel(
    chordLibrary: ChordLibrary,
    syncRepository: SyncRepository,
    private val authRepository: AuthRepository,
    private val setlistRepository: SetlistRepository,
    private val snackbarState: SongbookSnackbarState,
) : BaseViewModel(snackbarState) {

    val isLoggedIn: Boolean
        get() = authRepository.isLoggedIn()

    @OptIn(ExperimentalCoroutinesApi::class)
    val state = combine(
        flow {
            withContext(Dispatchers.IO) {
                chordLibrary.initialize(
                    Json.decodeFromString(
                        Res.readBytes(CHORD_JSON).decodeToString(),
                    ),
                )
            }
            emit(Unit)
        },
        authRepository.currentLoginStatusFlow,
    ) { _, loginStatus -> loginStatus }
        .flatMapLatest { loginStatus ->
            return@flatMapLatest if (loginStatus.isLoggedIn) {
                syncRepository.performSyncAsFlow()
            } else {
                emptyFlow()
            }
        }.catch {
            it.printStackTrace()
            snackbarState.showSnackbar(
                message = getString(Res.string.error_initilizing_error),
                icon = IconWarning,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = LoginStatus.LOGGED_OUT,
        )

    fun addSongToSetlist(setlistId: String, songId: String, order: Int) {
        viewModelScope.launch {
            setlistRepository.addSongToSetlist(setlistId, songId, order)
        }
    }

    private companion object {
        const val CHORD_JSON = "files/chords.json"
    }
}
