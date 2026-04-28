package com.pointlessapps.songbook.introduction

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.pointlessapps.songbook.core.auth.AuthRepository
import com.pointlessapps.songbook.shared.ui.Res
import com.pointlessapps.songbook.shared.ui.error_login_failed
import com.pointlessapps.songbook.ui.theme.IconWarning
import com.pointlessapps.songbook.utils.BaseViewModel
import com.pointlessapps.songbook.utils.SongbookSnackbarState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal sealed interface IntroductionEvent {
    data object NavigateToLibrary : IntroductionEvent
}

@Stable
internal data class IntroductionState(
    val isLoading: Boolean = false,
)

internal class IntroductionViewModel(
    private val authRepository: AuthRepository,
    private val snackbarState: SongbookSnackbarState,
) : BaseViewModel(snackbarState) {

    private val _state = MutableStateFlow(IntroductionState())
    val state = _state.asStateFlow()

    private val eventChannel = Channel<IntroductionEvent>()
    val events = eventChannel.receiveAsFlow()

    fun onContinueAsGuestClicked() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            if (authRepository.signInAnonymously()) {
                eventChannel.send(IntroductionEvent.NavigateToLibrary)
            } else {
                snackbarState.showSnackbar(
                    message = Res.string.error_login_failed,
                    icon = IconWarning,
                )
            }
            _state.update { it.copy(isLoading = false) }
        }.invokeOnCompletion {
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun onSignInWithGoogleClicked() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            if (authRepository.signInWithGoogle()) {
                eventChannel.send(IntroductionEvent.NavigateToLibrary)
            } else {
                snackbarState.showSnackbar(
                    message = Res.string.error_login_failed,
                    icon = IconWarning,
                )
            }
            _state.update { it.copy(isLoading = false) }
        }.invokeOnCompletion {
            _state.update { it.copy(isLoading = false) }
        }
    }
}
