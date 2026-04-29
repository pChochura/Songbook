package com.pointlessapps.songbook.website.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pointlessapps.songbook.shared.ui.Res
import com.pointlessapps.songbook.shared.ui.common_app_name
import com.pointlessapps.songbook.shared.ui.login_to_remove_account_description
import com.pointlessapps.songbook.shared.ui.remove_account_button
import com.pointlessapps.songbook.shared.ui.remove_account_confirmation_description
import com.pointlessapps.songbook.shared.ui.remove_account_confirmation_title
import com.pointlessapps.songbook.shared.ui.remove_account_description
import com.pointlessapps.songbook.shared.ui.remove_account_title
import com.pointlessapps.songbook.ui.components.SongbookButton
import com.pointlessapps.songbook.ui.components.SongbookIcon
import com.pointlessapps.songbook.ui.components.SongbookLoader
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.defaultSongbookButtonStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookButtonTextStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookIconStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.dialogs.ConfirmDeleteDialog
import com.pointlessapps.songbook.ui.theme.IconDelete
import com.pointlessapps.songbook.ui.theme.IconNote
import com.pointlessapps.songbook.ui.theme.spacing
import com.pointlessapps.songbook.website.RemoveAccountViewModel
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RemoveAccountScreen(
    viewModel: RemoveAccountViewModel,
) {
    var showConfirmationDialog by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .widthIn(max = 600.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(MaterialTheme.spacing.extraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = MaterialTheme.spacing.medium,
            alignment = Alignment.CenterVertically,
        ),
    ) {
        SongbookIcon(
            modifier = Modifier
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.primary)
                .padding(MaterialTheme.spacing.medium)
                .size(64.dp),
            icon = IconNote,
            iconStyle = defaultSongbookIconStyle().copy(
                tint = MaterialTheme.colorScheme.onPrimary,
            ),
        )
        SongbookText(
            text = stringResource(Res.string.common_app_name),
            textStyle = defaultSongbookTextStyle().copy(
                textAlign = TextAlign.Center,
                textColor = MaterialTheme.colorScheme.primary,
                typography = MaterialTheme.typography.displayMedium,
            ),
        )

        Spacer(Modifier.height(MaterialTheme.spacing.extraLarge))

        SongbookText(
            text = stringResource(Res.string.remove_account_title),
            textStyle = defaultSongbookTextStyle().copy(
                textAlign = TextAlign.Center,
                textColor = MaterialTheme.colorScheme.onSurface,
                typography = MaterialTheme.typography.titleLarge,
            ),
        )
        SongbookText(
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.extraLarge),
            text = stringResource(
                if (state.isLoggedIn) {
                    Res.string.remove_account_description
                } else {
                    Res.string.login_to_remove_account_description
                },
            ),
            textStyle = defaultSongbookTextStyle().copy(
                textAlign = TextAlign.Center,
                textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                typography = MaterialTheme.typography.bodyLarge,
            ),
        )

        if (state.isLoggedIn) {
            Spacer(Modifier.height(MaterialTheme.spacing.extraLarge))

            SongbookButton(
                modifier = Modifier.fillMaxWidth().width(100.dp),
                label = stringResource(Res.string.remove_account_button),
                onClick = { showConfirmationDialog = true },
                buttonStyle = defaultSongbookButtonStyle().copy(
                    containerColor = MaterialTheme.colorScheme.error,
                    textStyle = defaultSongbookButtonTextStyle().copy(
                        textColor = MaterialTheme.colorScheme.onError,
                        typography = MaterialTheme.typography.titleMedium,
                    ),
                    icon = IconDelete,
                ),
            )
        }
    }

    SongbookLoader(state.isLoading)

    if (showConfirmationDialog) {
        ConfirmDeleteDialog(
            title = Res.string.remove_account_confirmation_title,
            description = Res.string.remove_account_confirmation_description,
            onConfirmClicked = {
                showConfirmationDialog = false
                viewModel.onRemoveAccountClicked()
            },
            onDismissRequest = { showConfirmationDialog = false },
        )
    }
}
