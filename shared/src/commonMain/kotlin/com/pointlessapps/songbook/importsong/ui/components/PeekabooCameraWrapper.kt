package com.pointlessapps.songbook.importsong.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pointlessapps.songbook.importsong.ui.dialogs.CameraPermissionDeniedDialog
import com.pointlessapps.songbook.shared.ui.Res
import com.pointlessapps.songbook.shared.ui.common_back
import com.pointlessapps.songbook.shared.ui.common_take_a_photo
import com.pointlessapps.songbook.ui.components.Position
import com.pointlessapps.songbook.ui.components.SongbookIconButton
import com.pointlessapps.songbook.ui.components.SongbookLoader
import com.pointlessapps.songbook.ui.components.defaultSongbookIconButtonStyle
import com.pointlessapps.songbook.ui.theme.IconArrowLeft
import com.pointlessapps.songbook.ui.theme.IconCamera
import com.pointlessapps.songbook.ui.theme.spacing
import com.preat.peekaboo.ui.camera.PeekabooCamera
import com.preat.peekaboo.ui.camera.rememberPeekabooCameraState

@Composable
internal fun PeekabooCameraWrapper(
    onOpenSettingsClicked: () -> Unit,
    onCaptureClicked: (byteArray: ByteArray?) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberPeekabooCameraState(onCapture = onCaptureClicked)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        PeekabooCamera(
            state = state,
            modifier = modifier,
            permissionDeniedContent = @Composable {
                CameraPermissionDeniedDialog(
                    onOpenSettingsClicked = onOpenSettingsClicked,
                    onDismissRequest = onDismissRequest,
                )
            },
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(MaterialTheme.spacing.extraLarge),
            contentAlignment = Alignment.BottomCenter,
        ) {
            SongbookIconButton(
                modifier = Modifier
                    .size(CAPTURE_ICON_SIZE)
                    .padding(MaterialTheme.spacing.extraLarge),
                icon = IconCamera,
                tooltipLabel = Res.string.common_take_a_photo,
                onClick = state::capture,
                iconButtonStyle = defaultSongbookIconButtonStyle().copy(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    outlineColor = Color.Transparent,
                ),
            )
        }

        SongbookLoader(state.isCapturing)

        Box(
            modifier = Modifier
                .systemBarsPadding()
                .padding(MaterialTheme.spacing.extraLarge)
                .align(Alignment.TopStart),
        ) {
            SongbookIconButton(
                modifier = Modifier
                    .padding(MaterialTheme.spacing.extraSmall)
                    .size(TOP_BAR_ICON_SIZE),
                icon = IconArrowLeft,
                tooltipLabel = Res.string.common_back,
                onClick = onDismissRequest,
                iconButtonStyle = defaultSongbookIconButtonStyle().copy(
                    tooltipPosition = Position.BELOW,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    outlineColor = Color.Transparent,
                ),
            )
        }
    }
}

private val CAPTURE_ICON_SIZE = 76.dp
private val TOP_BAR_ICON_SIZE = 36.dp
