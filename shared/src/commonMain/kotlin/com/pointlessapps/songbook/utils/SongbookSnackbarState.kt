package com.pointlessapps.songbook.utils

import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pointlessapps.songbook.Route
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import org.jetbrains.compose.resources.DrawableResource
import kotlin.coroutines.resume

internal class SongbookSnackbarState {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private val mutex = Mutex()
    var currentSnackbarData by mutableStateOf<SongbookSnackbarData?>(null)
        private set

    private val callbackActions = Channel<SongbookSnackbarCallbackAction>()
    val callbackActionsFlow: Flow<SongbookSnackbarCallbackAction>
        get() = callbackActions.consumeAsFlow()

    fun showSnackbar(
        message: String,
        icon: DrawableResource? = null,
        duration: SnackbarDuration = SnackbarDuration.Short,
    ) {
        coroutineScope.launch {
            showSnackbar(
                SongbookSnackbarVisuals(
                    message = message,
                    actionLabel = null,
                    withDismissAction = false,
                    duration = duration,
                    icon = icon,
                ),
            )
        }
    }

    fun showSnackbar(
        message: String,
        actionLabel: String,
        callbackAction: SongbookSnackbarCallbackAction,
        icon: DrawableResource? = null,
        duration: SnackbarDuration = SnackbarDuration.Long,
    ) {
        coroutineScope.launch {
            val result = withTimeout(duration.toMillis()) {
                showSnackbar(
                    SongbookSnackbarVisuals(
                        message = message,
                        actionLabel = actionLabel,
                        withDismissAction = false,
                        duration = duration,
                        icon = icon,
                    ),
                )
            }

            if (result == SnackbarResult.ActionPerformed) {
                callbackActions.send(callbackAction)
            }
        }
    }

    private suspend fun showSnackbar(visuals: SongbookSnackbarVisuals): SnackbarResult {
        mutex.withLock {
            try {
                return suspendCancellableCoroutine { continuation ->
                    currentSnackbarData = SongbookSnackbarData(visuals, continuation)
                }
            } finally {
                currentSnackbarData = null
            }
        }
    }

    private fun SnackbarDuration.toMillis() = when (this) {
        SnackbarDuration.Short -> 4000L
        SnackbarDuration.Long -> 10000L
        SnackbarDuration.Indefinite -> Long.MAX_VALUE
    }
}

internal data class SongbookSnackbarData(
    override val visuals: SongbookSnackbarVisuals,
    private val continuation: CancellableContinuation<SnackbarResult>,
) : SnackbarData {
    override fun dismiss() = continuation.resume(SnackbarResult.Dismissed)
    override fun performAction() =
        continuation.resume(SnackbarResult.ActionPerformed)
}

internal data class SongbookSnackbarVisuals(
    override val message: String,
    override val actionLabel: String?,
    override val withDismissAction: Boolean,
    override val duration: SnackbarDuration,
    val icon: DrawableResource?,
) : SnackbarVisuals

internal sealed interface SongbookSnackbarCallbackAction {
    data class NavigateTo(val route: Route) : SongbookSnackbarCallbackAction
}
