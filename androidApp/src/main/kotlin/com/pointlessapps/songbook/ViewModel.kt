package com.pointlessapps.songbook

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal sealed interface TempEvent {
    data class NavigateTo(val route: Route) : TempEvent
    data class ShowSnackbar(@StringRes val message: Int) : TempEvent
}

internal data class TempState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
)

internal class ViewModelTemp : ViewModel() {

    var state by mutableStateOf(TempState())
        private set

    private val eventChannel = Channel<TempEvent>()
    val events = eventChannel.receiveAsFlow()

    fun temp() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            // some action here

            state = state.copy(isLoading = false)
        }
    }
}
