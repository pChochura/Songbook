package com.pointlessapps.songbook.library.ui.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import com.pointlessapps.songbook.shared.ui.Res
import com.pointlessapps.songbook.shared.ui.common_cancel
import com.pointlessapps.songbook.shared.ui.common_confirm
import com.pointlessapps.songbook.shared.ui.library_add_setlist
import com.pointlessapps.songbook.shared.ui.library_add_setlist_description
import com.pointlessapps.songbook.shared.ui.library_add_setlist_placeholder
import com.pointlessapps.songbook.ui.components.SongbookButton
import com.pointlessapps.songbook.ui.components.SongbookDialog
import com.pointlessapps.songbook.ui.components.SongbookDialogDismissible
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.SongbookTextField
import com.pointlessapps.songbook.ui.components.defaultSongbookButtonStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookDialogStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextFieldStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.DEFAULT_BORDER_WIDTH
import com.pointlessapps.songbook.ui.theme.IconAddFolder
import com.pointlessapps.songbook.ui.theme.spacing
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AddSetlistDialog(
    onConfirmClicked: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val nameTextFieldState = rememberTextFieldState()
    val isConfirmEnabled by remember { derivedStateOf { nameTextFieldState.text.isNotEmpty() } }

    SideEffect { focusRequester.requestFocus() }

    SongbookDialog(
        onDismissRequest = onDismissRequest,
        dialogStyle = defaultSongbookDialogStyle().copy(
            label = stringResource(Res.string.library_add_setlist),
            icon = IconAddFolder,
            dismissible = SongbookDialogDismissible.None,
        ),
    ) {
        SongbookText(
            text = stringResource(Res.string.library_add_setlist_description),
            textStyle = defaultSongbookTextStyle().copy(
                typography = MaterialTheme.typography.bodyMedium,
                textColor = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            ),
        )

        SongbookTextField(
            state = nameTextFieldState,
            modifier = Modifier
                .focusRequester(focusRequester)
                .fillMaxWidth()
                .border(
                    width = DEFAULT_BORDER_WIDTH,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = MaterialTheme.shapes.small,
                )
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = MaterialTheme.shapes.small,
                )
                .padding(MaterialTheme.spacing.large),
            textFieldStyle = defaultSongbookTextFieldStyle().copy(
                placeholder = stringResource(Res.string.library_add_setlist_placeholder),
                placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    autoCorrectEnabled = true,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                ),
            ),
            onImeAction = { onConfirmClicked(nameTextFieldState.text.toString()) },
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SongbookButton(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(Res.string.common_confirm),
                onClick = { onConfirmClicked(nameTextFieldState.text.toString()) },
                buttonStyle = defaultSongbookButtonStyle().copy(
                    enabled = isConfirmEnabled,
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    textStyle = defaultSongbookTextStyle().copy(
                        textAlign = TextAlign.Center,
                        textColor = MaterialTheme.colorScheme.onPrimary,
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
