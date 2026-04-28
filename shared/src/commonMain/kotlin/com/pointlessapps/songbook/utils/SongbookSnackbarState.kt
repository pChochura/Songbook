package com.pointlessapps.songbook.utils

import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pointlessapps.songbook.Route
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import kotlin.coroutines.resume
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal class SongbookSnackbarState {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private val mutex = Mutex()
    var currentSnackbarData by mutableStateOf<SongbookSnackbarData?>(null)
        private set

    private val callbackActions = Channel<SongbookSnackbarCallbackAction>()
    val callbackActionsFlow: Flow<SongbookSnackbarCallbackAction>
        get() = callbackActions.receiveAsFlow()

    fun showSnackbar(
        message: StringResource,
        icon: DrawableResource? = null,
        duration: SnackbarDuration = SnackbarDuration.Short,
    ) {
        coroutineScope.launch {
            showSnackbar(
                message = getString(message),
                icon = icon,
                duration = duration,
            )
        }
    }

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
        message: StringResource,
        actionLabel: StringResource,
        callbackAction: SongbookSnackbarCallbackAction,
        icon: DrawableResource? = null,
        duration: SnackbarDuration = SnackbarDuration.Long,
    ) {
        coroutineScope.launch {
            showSnackbar(
                message = getString(message),
                actionLabel = getString(actionLabel),
                callbackAction = callbackAction,
                icon = icon,
                duration = duration,
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
            val result = showSnackbar(
                SongbookSnackbarVisuals(
                    message = message,
                    actionLabel = actionLabel,
                    withDismissAction = false,
                    duration = duration,
                    icon = icon,
                ),
            )

            if (result == SnackbarResult.ActionPerformed) {
                callbackActions.send(callbackAction)
            }
        }
    }

    private suspend fun showSnackbar(visuals: SongbookSnackbarVisuals): SnackbarResult {
        mutex.withLock {
            try {
                return withTimeout(visuals.duration.toMillis()) {
                    suspendCancellableCoroutine { continuation ->
                        currentSnackbarData = SongbookSnackbarData(visuals, continuation)
                    }
                }
            } finally {
                currentSnackbarData = null
            }
        }
    }

    private fun SnackbarDuration.toMillis() = when (this) {
        SnackbarDuration.Short -> 4.seconds
        SnackbarDuration.Long -> 10.seconds
        SnackbarDuration.Indefinite -> Duration.INFINITE
    }
}

@Stable
internal data class SongbookSnackbarData(
    override val visuals: SongbookSnackbarVisuals,
    private val continuation: CancellableContinuation<SnackbarResult>,
) : SnackbarData {
    override fun dismiss() = continuation.resume(SnackbarResult.Dismissed)
    override fun performAction() =
        continuation.resume(SnackbarResult.ActionPerformed)
}

@Stable
internal data class SongbookSnackbarVisuals(
    override val message: String,
    override val actionLabel: String?,
    override val withDismissAction: Boolean,
    override val duration: SnackbarDuration,
    val icon: DrawableResource?,
) : SnackbarVisuals

internal sealed interface SongbookSnackbarCallbackAction {
    data class NavigateTo(val route: Route) : SongbookSnackbarCallbackAction
    data class LoadToQueueAndOpen(val songId: String) : SongbookSnackbarCallbackAction
    data class AddSongToSetlist(
        val setlistId: String,
        val songId: String,
        val order: Int,
    ) : SongbookSnackbarCallbackAction
}
