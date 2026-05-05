package com.pointlessapps.songbook.library.ui.dialogs

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
import com.pointlessapps.songbook.core.auth.model.LoginStatus
import com.pointlessapps.songbook.shared.ui.Res
import com.pointlessapps.songbook.shared.ui.common_cancel
import com.pointlessapps.songbook.shared.ui.common_logout
import com.pointlessapps.songbook.shared.ui.common_logout_anonymous_description
import com.pointlessapps.songbook.shared.ui.common_logout_description
import com.pointlessapps.songbook.ui.components.SongbookButton
import com.pointlessapps.songbook.ui.components.SongbookDialog
import com.pointlessapps.songbook.ui.components.SongbookDialogDismissible
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.defaultSongbookButtonStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookDialogStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.DEFAULT_BORDER_WIDTH
import com.pointlessapps.songbook.ui.theme.IconLogout
import com.pointlessapps.songbook.ui.theme.spacing
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LogoutDialog(
    loginStatus: LoginStatus,
    onConfirmClicked: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    SongbookDialog(
        onDismissRequest = onDismissRequest,
        dialogStyle = defaultSongbookDialogStyle().copy(
            label = stringResource(Res.string.common_logout),
            icon = IconLogout,
            dismissible = SongbookDialogDismissible.Both,
        ),
    ) {
        SongbookText(
            text = stringResource(
                if (loginStatus == LoginStatus.ANONYMOUS)
                    Res.string.common_logout_anonymous_description
                else {
                    Res.string.common_logout_description
                },
            ),
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
                label = stringResource(Res.string.common_logout),
                onClick = onConfirmClicked,
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
                onClick = onDismissRequest,
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
