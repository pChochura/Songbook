package com.pointlessapps.songbook.utils

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.pointlessapps.songbook.core.sync.model.SyncStatus
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.common_syncing
import com.pointlessapps.songbook.ui.TopBarButton
import com.pointlessapps.songbook.ui.theme.IconSync
import com.pointlessapps.songbook.ui.theme.IconSyncFailed
import kotlinx.coroutines.delay

@Composable
internal fun syncingTopBarButton(syncStatus: SyncStatus): TopBarButton? {
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(syncStatus) {
        if (syncStatus != SyncStatus.SYNCED) {
            alpha.snapTo(1f)
        } else {
            delay(500)
            alpha.animateTo(0f, tween(300))
        }
    }

    val currentlyShown by remember { derivedStateOf { alpha.value != 0f } }
    if (!currentlyShown) return null

    val rotation = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        rotation.animateTo(360f, infiniteRepeatable(tween(3000)))
    }

    return TopBarButton(
        enabled = false,
        icon = if (syncStatus == SyncStatus.SYNC_FAILED) IconSyncFailed else IconSync,
        tooltip = Res.string.common_syncing,
        onClick = {},
        modifier = Modifier.graphicsLayer {
            this.alpha = alpha.value
            if (syncStatus != SyncStatus.SYNC_FAILED) {
                this.rotationZ = rotation.value
            }
        },
    )
}
