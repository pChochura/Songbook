package com.pointlessapps.songbook.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.pointlessapps.songbook.ui.theme.IconWarning
import com.pointlessapps.songbook.ui.theme.spacing
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

@Composable
fun SongbookSnackbar(
    message: String,
    actionLabel: String?,
    actionCallback: (() -> Unit)?,
    onDismissRequest: () -> Unit,
) {
    Snackbar(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onDismissRequest),
        shape = MaterialTheme.shapes.small,
        containerColor = MaterialTheme.colorScheme.inverseSurface,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                space = MaterialTheme.spacing.medium,
                alignment = Alignment.CenterHorizontally,
            ),
        ) {
            SongbookIcon(
                icon = IconWarning,
                modifier = Modifier.size(ICON_SIZE),
                iconStyle = defaultSongbookIconStyle().copy(
                    tint = MaterialTheme.colorScheme.inverseOnSurface,
                ),
            )
            SongbookText(
                modifier = Modifier.weight(1f),
                text = message,
                textStyle = defaultSongbookTextStyle().copy(
                    textColor = MaterialTheme.colorScheme.inverseOnSurface,
                    typography = MaterialTheme.typography.bodyMedium,
                ),
            )

            if (actionLabel != null) {
                SongbookText(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .clickable { actionCallback?.invoke() }
                        .padding(MaterialTheme.spacing.small),
                    text = actionLabel.uppercase(),
                    textStyle = defaultSongbookTextStyle().copy(
                        textColor = MaterialTheme.colorScheme.inversePrimary,
                        typography = MaterialTheme.typography.labelLarge,
                    ),
                )
            }
        }
    }
}

class SongbookSnackbarHostState(private val onShowSnackbarListener: SnackbarHostListener) {

    fun showSnackbar(
        message: StringResource,
        actionLabel: StringResource? = null,
        actionCallback: (() -> Unit)? = null,
        dismissCallback: (() -> Unit)? = null,
        duration: SnackbarDuration = SnackbarDuration.Short,
    ) = onShowSnackbarListener.showSnackbar(
        message = message,
        actionLabel = actionLabel,
        actionCallback = actionCallback,
        dismissCallback = dismissCallback,
        duration = duration,
    )

    fun interface SnackbarHostListener {
        fun showSnackbar(
            message: StringResource,
            actionLabel: StringResource?,
            actionCallback: (() -> Unit)?,
            dismissCallback: (() -> Unit)?,
            duration: SnackbarDuration,
        )
    }
}

@Composable
fun rememberSongbookSnackbarHostState(
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
): SongbookSnackbarHostState {
    val coroutineScope = rememberCoroutineScope()

    return remember {
        SongbookSnackbarHostState { message, actionLabel, actionCallback, dismissCallback, duration ->
            coroutineScope.launch {
                // Ignore a snackbar if the same is already displayed
                snackbarHostState.currentSnackbarData?.let { data ->
                    if (
                        data.visuals.message == getString(message) &&
                        data.visuals.actionLabel == actionLabel?.let { getString(it) } &&
                        data.visuals.duration == duration
                    ) {
                        return@launch
                    }
                }

                val result = snackbarHostState.showSnackbar(
                    message = getString(message),
                    actionLabel = actionLabel?.let { getString(it) },
                    duration = duration,
                )
                when (result) {
                    SnackbarResult.ActionPerformed -> actionCallback?.invoke()
                    SnackbarResult.Dismissed -> dismissCallback?.invoke()
                }
            }
        }
    }
}

internal val LocalSnackbarHostState = compositionLocalOf<SongbookSnackbarHostState> {
    error("No SongbookSnackbarHostState found")
}

private val ICON_SIZE = 24.dp
