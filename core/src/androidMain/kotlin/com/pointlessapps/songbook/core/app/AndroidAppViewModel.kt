package com.pointlessapps.songbook.core.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessapps.songbook.core.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AndroidAppViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _isInitializing = MutableStateFlow(true)
    val isInitializing: StateFlow<Boolean> = _isInitializing.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching { authRepository.initialize() }
            _isInitializing.value = false
        }
    }
}
