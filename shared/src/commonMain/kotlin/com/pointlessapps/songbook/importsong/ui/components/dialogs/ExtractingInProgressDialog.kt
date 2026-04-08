package com.pointlessapps.songbook.importsong.ui.components.dialogs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.common_cancel
import com.pointlessapps.songbook.shared.import_extraction_in_progress
import com.pointlessapps.songbook.shared.import_extraction_in_progress_description
import com.pointlessapps.songbook.ui.components.SongbookButton
import com.pointlessapps.songbook.ui.components.SongbookDialog
import com.pointlessapps.songbook.ui.components.SongbookDialogDismissible
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.defaultSongbookButtonStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookDialogStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.IconImprove
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ExtractingInProgressDialog(
    onDismissRequest: () -> Unit,
) {
    SongbookDialog(
        onDismissRequest = onDismissRequest,
        dialogStyle = defaultSongbookDialogStyle().copy(
            label = stringResource(Res.string.import_extraction_in_progress),
            icon = IconImprove,
            dismissible = SongbookDialogDismissible.None,
        ),
    ) {
        SongbookText(
            text = stringResource(Res.string.import_extraction_in_progress_description),
            textStyle = defaultSongbookTextStyle().copy(
                typography = MaterialTheme.typography.bodyMedium,
                textColor = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            ),
        )

        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
        )

        SongbookButton(
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(Res.string.common_cancel),
            onClick = { onDismissRequest() },
            buttonStyle = defaultSongbookButtonStyle().copy(
                containerColor = MaterialTheme.colorScheme.error,
                textStyle = defaultSongbookTextStyle().copy(
                    textAlign = TextAlign.Center,
                    textColor = MaterialTheme.colorScheme.onError,
                ),
            ),
        )
    }
}
