package com.pointlessapps.songbook.settings

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.pointlessapps.songbook.core.app.AppRepository
import com.pointlessapps.songbook.core.auth.AuthRepository
import com.pointlessapps.songbook.core.auth.exceptions.AccountAlreadyLinkedException
import com.pointlessapps.songbook.core.auth.model.LoginStatus
import com.pointlessapps.songbook.core.prefs.PrefsRepository
import com.pointlessapps.songbook.core.sync.SyncRepository
import com.pointlessapps.songbook.core.sync.model.SyncStatus
import com.pointlessapps.songbook.shared.ui.Res
import com.pointlessapps.songbook.shared.ui.error_account_already_linked_error
import com.pointlessapps.songbook.ui.theme.IconWarning
import com.pointlessapps.songbook.utils.BaseViewModel
import com.pointlessapps.songbook.utils.SongbookSnackbarState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal sealed interface SettingsEvent {
    data object NavigateToIntroduction : SettingsEvent
}

@Stable
internal data class SettingsState(
    val syncStatus: SyncStatus = SyncStatus.LOCAL,
    val loginStatus: LoginStatus = LoginStatus.ANONYMOUS,
)

internal class SettingsViewModel(
    private val appRepository: AppRepository,
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository,
    private val prefsRepository: PrefsRepository,
    private val snackbarState: SongbookSnackbarState,
) : BaseViewModel(snackbarState) {

    private val eventChannel = Channel<SettingsEvent>()
    val events = eventChannel.receiveAsFlow()

    val state = combine(
        syncRepository.currentSyncStatusFlow,
        authRepository.currentLoginStatusFlow,
    ) { syncStatus, loginStatus ->
        SettingsState(
            syncStatus = syncStatus,
            loginStatus = loginStatus,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsState(),
    )

    fun onLoginClicked() {
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

    fun onLogoutClicked() {
        viewModelScope.launch {
            authRepository.logout()
            syncRepository.clearDatabase()
            eventChannel.send(SettingsEvent.NavigateToIntroduction)
        }
    }

    fun onRemoveAccountClicked() {
        viewModelScope.launch {
            authRepository.getTokens()?.let { (accessToken, refreshToken) ->
                appRepository.openRemoveAccountWebsite(accessToken, refreshToken)
            }
            authRepository.clearSession()
            syncRepository.clearDatabase()
            prefsRepository.clearData()
            eventChannel.send(SettingsEvent.NavigateToIntroduction)
        }
    }
}
