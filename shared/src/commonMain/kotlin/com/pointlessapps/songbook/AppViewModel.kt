package com.pointlessapps.songbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessapps.songbook.core.setlist.SetlistRepository
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
    chordLibrary: ChordLibrary,
    syncRepository: SyncRepository,
    private val setlistRepository: SetlistRepository,
    private val snackbarState: SongbookSnackbarState,
) : ViewModel() {

    val state: StateFlow<AppState> = combine(
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
        syncRepository.observeRemoteAsFlow(),
    ) { AppState(isLoading = false) }
        .catch {
            it.printStackTrace()
            snackbarState.showSnackbar(
                message = getString(Res.string.error_initilizing_error),
                icon = IconWarning,
            )
            emit(AppState(isLoading = false))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = AppState(),
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
