package com.pointlessapps.songbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessapps.songbook.core.auth.AuthRepository
import com.pointlessapps.songbook.core.song.ChordLibrary
import com.pointlessapps.songbook.core.sync.SyncRepository
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.error_initilizing_error
import com.pointlessapps.songbook.ui.theme.IconWarning
import com.pointlessapps.songbook.utils.SongbookSnackbarState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.getString

internal data class AppState(
    val isLoading: Boolean = true,
)

internal class AppViewModel(
    private val chordLibrary: ChordLibrary,
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository,
    private val snackbarState: SongbookSnackbarState,
) : ViewModel() {

    val state: StateFlow<AppState> = combine(
        flow {
            authRepository.initialize()
            if (!authRepository.isSignedIn()) {
                authRepository.signInAnonymously()
            }
            syncRepository.startSync()
            emit(Unit)
        },
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
    ) { AppState(isLoading = false) }
        .catch {
            snackbarState.showSnackbar(
                message = getString(Res.string.error_initilizing_error),
                icon = IconWarning,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = AppState(),
        )

    override fun onCleared() {
        viewModelScope.launch { syncRepository.stopSync() }
        super.onCleared()
    }

    private companion object {
        const val CHORD_JSON = "files/chords.json"
    }
}
