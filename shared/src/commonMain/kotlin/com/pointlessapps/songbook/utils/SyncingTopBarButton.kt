package com.pointlessapps.songbook.utils

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import com.pointlessapps.songbook.core.sync.model.SyncStatus
import com.pointlessapps.songbook.shared.ui.Res
import com.pointlessapps.songbook.shared.ui.common_offline
import com.pointlessapps.songbook.shared.ui.common_sync_failed
import com.pointlessapps.songbook.shared.ui.common_synced
import com.pointlessapps.songbook.shared.ui.common_syncing
import com.pointlessapps.songbook.ui.TOP_BAR_ICON_SIZE
import com.pointlessapps.songbook.ui.components.Position
import com.pointlessapps.songbook.ui.components.SongbookIconButton
import com.pointlessapps.songbook.ui.components.defaultSongbookIconButtonStyle
import com.pointlessapps.songbook.ui.components.rememberSongbookTooltipState
import com.pointlessapps.songbook.ui.theme.IconSync
import com.pointlessapps.songbook.ui.theme.IconSyncFailed
import com.pointlessapps.songbook.ui.theme.spacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun SyncingTopBarButton(syncStatus: SyncStatus) {
    val tooltipState = rememberSongbookTooltipState(isPersistent = false)
    val alpha = remember { Animatable(0f) }
    var isFirstLaunch by remember(Unit) { mutableStateOf(true) }

    LaunchedEffect(syncStatus) {
        if (isFirstLaunch) {
            isFirstLaunch = false
        } else {
            launch { tooltipState.show(MutatePriority.PreventUserInput) }
        }

        if (syncStatus != SyncStatus.SYNCED) {
            alpha.snapTo(1f)
        } else {
            delay(500.milliseconds)
            launch { alpha.animateTo(0f, tween(300)) }
            tooltipState.dismiss()
        }
    }

    val rotation by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(3000)),
    )

    SongbookIconButton(
        modifier = Modifier
            .graphicsLayer {
                this.alpha = alpha.value
                if (!syncStatus.failed) this.rotationZ = rotation
            }
            .size(TOP_BAR_ICON_SIZE)
            .padding(MaterialTheme.spacing.extraSmall),
        icon = if (syncStatus.failed) IconSyncFailed else IconSync,
        tooltipLabel = when (syncStatus) {
            SyncStatus.OFFLINE, SyncStatus.LOCAL -> Res.string.common_offline
            SyncStatus.SYNCING -> Res.string.common_syncing
            SyncStatus.SYNC_FAILED -> Res.string.common_sync_failed
            SyncStatus.SYNCED -> Res.string.common_synced
        },
        onClick = {},
        iconButtonStyle = defaultSongbookIconButtonStyle().copy(
            enabled = false,
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            outlineColor = Color.Transparent,
            disabledOutlineColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContentColor = MaterialTheme.colorScheme.onSurface,
            tooltipPosition = Position.BELOW,
            tooltipState = tooltipState,
        ),
    )
}
