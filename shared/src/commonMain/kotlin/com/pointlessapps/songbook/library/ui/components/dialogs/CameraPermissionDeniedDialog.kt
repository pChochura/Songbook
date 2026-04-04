package com.pointlessapps.songbook.library.ui.components.dialogs

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.common_camera_permission
import com.pointlessapps.songbook.shared.common_camera_permission_description
import com.pointlessapps.songbook.shared.common_cancel
import com.pointlessapps.songbook.shared.common_open_settings
import com.pointlessapps.songbook.ui.components.SongbookButton
import com.pointlessapps.songbook.ui.components.SongbookDialog
import com.pointlessapps.songbook.ui.components.SongbookDialogDismissible
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.defaultSongbookButtonStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookDialogStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.DEFAULT_BORDER_WIDTH
import com.pointlessapps.songbook.ui.theme.IconWarning
import com.pointlessapps.songbook.ui.theme.spacing
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CameraPermissionDeniedDialog(
    onOpenSettingsClicked: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    SongbookDialog(
        onDismissRequest = onDismissRequest,
        dialogStyle = defaultSongbookDialogStyle().copy(
            label = stringResource(Res.string.common_camera_permission),
            icon = IconWarning,
            dismissible = SongbookDialogDismissible.None,
        ),
    ) {
        SongbookText(
            text = stringResource(Res.string.common_camera_permission_description),
            textStyle = defaultSongbookTextStyle().copy(
                typography = MaterialTheme.typography.bodyMedium,
                textColor = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            ),
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SongbookButton(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(Res.string.common_open_settings),
                onClick = onOpenSettingsClicked,
                buttonStyle = defaultSongbookButtonStyle().copy(
                    containerColor = MaterialTheme.colorScheme.error,
                    textStyle = defaultSongbookTextStyle().copy(
                        textAlign = TextAlign.Center,
                        textColor = MaterialTheme.colorScheme.onError,
                    ),
                ),
            )
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
}
