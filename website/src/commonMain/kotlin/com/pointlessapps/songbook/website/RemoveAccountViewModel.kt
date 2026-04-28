package com.pointlessapps.songbook.website

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.pointlessapps.songbook.core.auth.AuthRepository
import com.pointlessapps.songbook.core.auth.model.Tokens
import com.pointlessapps.songbook.shared.ui.Res
import com.pointlessapps.songbook.shared.ui.remove_account_success
import com.pointlessapps.songbook.utils.BaseViewModel
import com.pointlessapps.songbook.utils.SongbookSnackbarState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal data class RemoveAccountState(
    val isLoading: Boolean = true,
    val isLoggedIn: Boolean = false,
)

internal class RemoveAccountViewModel(
    private val url: String,
    private val authRepository: AuthRepository,
    private val snackbarState: SongbookSnackbarState,
) : BaseViewModel(snackbarState, Dispatchers.Main) {

    @Stable
    private data class RemoveAccountTransientState(
        val isLoading: Boolean = false,
    )

    private val _transientState = MutableStateFlow(RemoveAccountTransientState())

    val state = combine(
        authRepository.currentLoginStatusFlow,
        _transientState,
    ) { loginStatus, transient ->
        RemoveAccountState(
            isLoading = transient.isLoading,
            isLoggedIn = loginStatus.isLoggedIn,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RemoveAccountState(),
    )

    init {
        val tokens = Regex("access_token=([^&]+)&refresh_token=([^&]+)")
            .find(url)?.groups
        viewModelScope.launch {
            authRepository.initialize(
                tokens?.let {
                    Tokens(
                        accessToken = it[1]!!.value,
                        refreshToken = it[2]!!.value,
                    )
                },
            )
            _transientState.update { it.copy(isLoading = false) }
        }
    }

    fun onRemoveAccountClicked() {
        viewModelScope.launch {
            _transientState.update { it.copy(isLoading = true) }
            authRepository.removeAccount()
            _transientState.update { it.copy(isLoading = false) }
            snackbarState.showSnackbar(Res.string.remove_account_success)
        }
    }
}
