package com.pointlessapps.songbook.importsong.ui.dialogs

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
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
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.pointlessapps.songbook.shared.ui.Res
import com.pointlessapps.songbook.shared.ui.common_back
import com.pointlessapps.songbook.shared.ui.common_cancel
import com.pointlessapps.songbook.shared.ui.common_enter_manually
import com.pointlessapps.songbook.shared.ui.common_from_gallery
import com.pointlessapps.songbook.shared.ui.common_no_internet
import com.pointlessapps.songbook.shared.ui.common_scan_photo
import com.pointlessapps.songbook.shared.ui.common_take_a_photo
import com.pointlessapps.songbook.shared.ui.common_take_photo
import com.pointlessapps.songbook.ui.components.Position
import com.pointlessapps.songbook.ui.components.SongbookButton
import com.pointlessapps.songbook.ui.components.SongbookButtonOrientation
import com.pointlessapps.songbook.ui.components.SongbookDialog
import com.pointlessapps.songbook.ui.components.SongbookDialogDismissible
import com.pointlessapps.songbook.ui.components.SongbookIconButton
import com.pointlessapps.songbook.ui.components.SongbookLoader
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.SongbookTooltip
import com.pointlessapps.songbook.ui.components.defaultSongbookButtonStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookButtonTextStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookDialogStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookIconButtonStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.components.rememberSongbookTooltipState
import com.pointlessapps.songbook.ui.theme.DEFAULT_BORDER_WIDTH
import com.pointlessapps.songbook.ui.theme.IconArrowLeft
import com.pointlessapps.songbook.ui.theme.IconCamera
import com.pointlessapps.songbook.ui.theme.IconScan
import com.pointlessapps.songbook.ui.theme.spacing
import com.pointlessapps.songbook.utils.rememberPermissionRequester
import com.preat.peekaboo.image.picker.FilterOptions
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import com.preat.peekaboo.ui.camera.CameraMode
import com.preat.peekaboo.ui.camera.PeekabooCamera
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ScanDialog(
    description: StringResource,
    showEnterManuallyButton: Boolean,
    hasInternetConnection: Boolean,
    onImageCaptured: (ByteArray?) -> Unit,
    onOpenSettingsClicked: () -> Unit,
    onEnterManuallyClicked: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val permissionRequester = rememberPermissionRequester()
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

    if (!isCameraVisible) {
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
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    propagateMinConstraints = true,
                ) {
                    val tooltipState = rememberSongbookTooltipState(isPersistent = false)
                    SongbookTooltip(
                        state = tooltipState,
                        position = Position.ABOVE,
                        contentDescription = Res.string.common_no_internet,
                        allowUserInput = false,
                    ) {
                        SongbookButton(
                            modifier = Modifier.fillMaxWidth(),
                            label = stringResource(Res.string.common_from_gallery),
                            onClick = {
                                if (!hasInternetConnection) {
                                    coroutineScope.launch {
                                        tooltipState.show(MutatePriority.UserInput)
                                    }
                                } else {
                                    imagePickerLauncher.launch()
                                }
                            },
                            buttonStyle = defaultSongbookButtonStyle().copy(
                                containerColor = MaterialTheme.colorScheme.primary.copy(
                                    alpha = if (hasInternetConnection) 1f else 0.3f,
                                ),
                                orientation = SongbookButtonOrientation.Vertical,
                                shape = MaterialTheme.shapes.medium,
                                icon = IconCamera,
                                textStyle = defaultSongbookButtonTextStyle().copy(
                                    textColor = if (hasInternetConnection) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                    },
                                    textAlign = TextAlign.Center,
                                ),
                            ),
                        )
                    }
                }
                Box(
                    modifier = Modifier.weight(1f),
                    propagateMinConstraints = true,
                ) {
                    val tooltipState = rememberSongbookTooltipState(isPersistent = false)
                    SongbookTooltip(
                        state = tooltipState,
                        position = Position.ABOVE,
                        contentDescription = Res.string.common_no_internet,
                        allowUserInput = false,
                    ) {
                        SongbookButton(
                            modifier = Modifier.fillMaxWidth(),
                            label = stringResource(Res.string.common_take_photo),
                            onClick = {
                                coroutineScope.launch {
                                    if (!hasInternetConnection) {
                                        tooltipState.show(MutatePriority.UserInput)
                                    } else if (permissionRequester.requestCameraPermission()) {
                                        isCameraVisible = true
                                    }
                                }
                            },
                            buttonStyle = defaultSongbookButtonStyle().copy(
                                containerColor = MaterialTheme.colorScheme.primary.copy(
                                    alpha = if (hasInternetConnection) 1f else 0.3f,
                                ),
                                orientation = SongbookButtonOrientation.Vertical,
                                shape = MaterialTheme.shapes.medium,
                                icon = IconCamera,
                                textStyle = defaultSongbookButtonTextStyle().copy(
                                    textColor = if (hasInternetConnection) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                    },
                                    textAlign = TextAlign.Center,
                                ),
                            ),
                        )
                    }
                }
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
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            PeekabooCamera(
                modifier = Modifier.fillMaxSize(),
                cameraMode = CameraMode.Back,
                captureIcon = @Composable { capture ->
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
                            onClick = capture,
                            iconButtonStyle = defaultSongbookIconButtonStyle().copy(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                outlineColor = Color.Transparent,
                            ),
                        )
                    }
                },
                progressIndicator = @Composable { SongbookLoader(true) },
                onCapture = onImageCaptured,
                permissionDeniedContent = {
                    CameraPermissionDeniedDialog(
                        onOpenSettingsClicked = onOpenSettingsClicked,
                        onDismissRequest = { isCameraVisible = false },
                    )
                },
            )

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
                    onClick = { isCameraVisible = false },
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
}

private val CAPTURE_ICON_SIZE = 76.dp
private val TOP_BAR_ICON_SIZE = 36.dp
