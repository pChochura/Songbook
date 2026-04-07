package com.pointlessapps.songbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessapps.songbook.core.auth.AuthRepository
import com.pointlessapps.songbook.core.song.ChordLibrary
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.error_initilizing_error
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.getString

data class AppState(
    val error: String? = null,
    val isLoading: Boolean = true,
)

class AppViewModel(
    private val chordLibrary: ChordLibrary,
    private val authRepository: AuthRepository,
) : ViewModel() {

    val state: StateFlow<AppState> = combine(
        flow {
            authRepository.initialize()
            if (!authRepository.isSignedIn()) {
                authRepository.signInAnonymously()
            }
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
            emit(
                AppState(
                    error = getString(Res.string.error_initilizing_error),
                    isLoading = false,
                ),
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppState(),
        )

    private companion object {
        const val CHORD_JSON = "files/chords.json"
    }
}
