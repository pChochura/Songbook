package com.pointlessapps.songbook.library.ui.components.dialogs

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.common_cancel
import com.pointlessapps.songbook.shared.common_enter_manually
import com.pointlessapps.songbook.shared.common_from_gallery
import com.pointlessapps.songbook.shared.common_scan_photo
import com.pointlessapps.songbook.shared.common_take_photo
import com.pointlessapps.songbook.ui.components.SongbookButton
import com.pointlessapps.songbook.ui.components.SongbookButtonOrientation
import com.pointlessapps.songbook.ui.components.SongbookDialog
import com.pointlessapps.songbook.ui.components.SongbookDialogDismissible
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.defaultSongbookButtonStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookDialogStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.DEFAULT_BORDER_WIDTH
import com.pointlessapps.songbook.ui.theme.IconCamera
import com.pointlessapps.songbook.ui.theme.IconImage
import com.pointlessapps.songbook.ui.theme.IconScan
import com.pointlessapps.songbook.ui.theme.spacing
import com.preat.peekaboo.image.picker.FilterOptions
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import com.preat.peekaboo.ui.camera.CameraMode
import com.preat.peekaboo.ui.camera.PeekabooCamera
import com.preat.peekaboo.ui.camera.rememberPeekabooCameraState
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ScanDialog(
    description: StringResource,
    showEnterManuallyButton: Boolean,
    onImageCaptured: (ByteArray?) -> Unit,
    onEnterManuallyClicked: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    var isCameraVisible by rememberSaveable { mutableStateOf(false) }
    val imagePickerLauncher = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = rememberCoroutineScope(),
        filterOptions = FilterOptions.GrayScale,
        onResult = { onImageCaptured(it.firstOrNull()) },
    )

    NavigationBackHandler(
        state = rememberNavigationEventState(
            currentInfo = NavigationEventInfo.None,
        ),
        isBackEnabled = isCameraVisible,
        onBackCompleted = { isCameraVisible = false },
    )

    SongbookDialog(
        onDismissRequest = onDismissRequest,
        dialogStyle = defaultSongbookDialogStyle().copy(
            label = stringResource(Res.string.common_scan_photo),
            icon = IconScan,
            dismissible = SongbookDialogDismissible.Both,
        ),
    ) {
        SongbookText(
            text = stringResource(description),
            textStyle = defaultSongbookTextStyle().copy(
                typography = MaterialTheme.typography.bodyMedium,
                textColor = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            ),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        ) {
            SongbookButton(
                modifier = Modifier.weight(1f),
                label = stringResource(Res.string.common_from_gallery),
                onClick = { imagePickerLauncher.launch() },
                buttonStyle = defaultSongbookButtonStyle().copy(
                    orientation = SongbookButtonOrientation.Vertical,
                    shape = MaterialTheme.shapes.medium,
                    icon = IconImage,
                ),
            )
            SongbookButton(
                modifier = Modifier.weight(1f),
                label = stringResource(Res.string.common_take_photo),
                onClick = { isCameraVisible = true },
                buttonStyle = defaultSongbookButtonStyle().copy(
                    orientation = SongbookButtonOrientation.Vertical,
                    shape = MaterialTheme.shapes.medium,
                    icon = IconCamera,
                ),
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (showEnterManuallyButton) {
                SongbookButton(
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(Res.string.common_enter_manually),
                    onClick = { onEnterManuallyClicked() },
                    buttonStyle = defaultSongbookButtonStyle().copy(
                        containerColor = MaterialTheme.colorScheme.primary,
                        textStyle = defaultSongbookTextStyle().copy(
                            textAlign = TextAlign.Center,
                            textColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ),
                )
            }

            SongbookButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = DEFAULT_BORDER_WIDTH,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape,
                    ),
                label = stringResource(Res.string.common_cancel),
                onClick = { onDismissRequest() },
                buttonStyle = defaultSongbookButtonStyle().copy(
                    containerColor = Color.Transparent,
                    textStyle = defaultSongbookTextStyle().copy(
                        textAlign = TextAlign.Center,
                        textColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ),
            )
        }
    }

    if (isCameraVisible) {
        PeekabooCamera(
            state = rememberPeekabooCameraState(
                initialCameraMode = CameraMode.Back,
                onCapture = { onImageCaptured(it) },
            ),
            modifier = Modifier.fillMaxSize(),
        )
    }
}
