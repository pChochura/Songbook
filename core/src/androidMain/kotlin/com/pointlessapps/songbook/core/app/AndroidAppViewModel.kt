package com.pointlessapps.songbook.core.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessapps.songbook.core.auth.AuthRepository
import com.pointlessapps.songbook.core.prefs.PrefsRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

data class AndroidAppState(
    val isInitializing: Boolean = true,
    val previewAlreadyRendered: Boolean = true,
)

class AndroidAppViewModel(
    private val authRepository: AuthRepository,
    private val prefsRepository: PrefsRepository,
) : ViewModel(
    viewModelScope = CoroutineScope(
        SupervisorJob() +
                Dispatchers.Main.immediate +
                CoroutineExceptionHandler { _, throwable ->
                    throwable.printStackTrace()
                },
    ),
) {

    var state by mutableStateOf(AndroidAppState())
        private set

    init {
        viewModelScope.launch {
            runCatching { authRepository.initialize() }
            state = state.copy(
                isInitializing = false,
                previewAlreadyRendered = prefsRepository.getIsWidgetPreviewRendered(),
            )
        }
    }

    fun setPreviewRendered() {
        state = state.copy(previewAlreadyRendered = true)
        viewModelScope.launch {
            prefsRepository.setIsWidgetPreviewRendered()
        }
    }
}
