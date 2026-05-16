package com.pointlessapps.songbook.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pointlessapps.songbook.shared.ui.Res
import com.pointlessapps.songbook.shared.ui.common_delete_song
import com.pointlessapps.songbook.shared.ui.common_delete_song_description
import com.pointlessapps.songbook.shared.ui.common_unknown
import com.pointlessapps.songbook.shared.ui.common_unnamed
import com.pointlessapps.songbook.shared.ui.song_menu_add_to_queue
import com.pointlessapps.songbook.shared.ui.song_menu_add_to_setlists
import com.pointlessapps.songbook.shared.ui.song_menu_delete
import com.pointlessapps.songbook.shared.ui.song_menu_delete_description
import com.pointlessapps.songbook.shared.ui.song_menu_edit
import com.pointlessapps.songbook.ui.OptionsBottomSheet
import com.pointlessapps.songbook.ui.OptionsBottomSheetItem
import com.pointlessapps.songbook.ui.components.SongOptionsBottomSheetAction.AddToFavourites
import com.pointlessapps.songbook.ui.components.SongOptionsBottomSheetAction.AddToQueue
import com.pointlessapps.songbook.ui.components.SongOptionsBottomSheetAction.AddToSetlists
import com.pointlessapps.songbook.ui.components.SongOptionsBottomSheetAction.Delete
import com.pointlessapps.songbook.ui.components.SongOptionsBottomSheetAction.Edit
import com.pointlessapps.songbook.ui.dialogs.ConfirmationDialog
import com.pointlessapps.songbook.ui.dialogs.SetlistsDialog
import com.pointlessapps.songbook.ui.theme.IconAddFolder
import com.pointlessapps.songbook.ui.theme.IconDelete
import com.pointlessapps.songbook.ui.theme.IconEdit
import com.pointlessapps.songbook.ui.theme.IconQueue
import com.pointlessapps.songbook.ui.theme.spacing
import com.pointlessapps.songbook.utils.SongOptionsBottomSheetDelegate
import com.pointlessapps.songbook.utils.SongOptionsBottomSheetState
import com.pointlessapps.songbook.utils.SongOptionsBottomSheetState.Loaded
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SongOptionsBottomSheet(
    show: Boolean,
    state: SongOptionsBottomSheetState,
    onDismissRequest: () -> Unit,
    onAction: (SongOptionsBottomSheetAction) -> Unit,
) {
    if (state is Loaded) {
        OptionsBottomSheet(
            show = show,
            onDismissRequest = onDismissRequest,
            headerContent = {
                SongOptionsBottomSheetHeader(
                    title = state.song.title,
                    artist = state.song.artist,
                )
            },
            items = persistentListOf(
                OptionsBottomSheetItem.Divider,
                OptionsBottomSheetItem.new(
                    icon = IconEdit,
                    label = Res.string.song_menu_edit,
                    onClick = { onAction(Edit) },
                ),
                OptionsBottomSheetItem.Divider,
                OptionsBottomSheetItem.new(
                    icon = IconQueue,
                    label = Res.string.song_menu_add_to_queue,
                    onClick = { onAction(AddToQueue) },
                ),
                OptionsBottomSheetItem.new(
                    icon = IconAddFolder,
                    label = Res.string.song_menu_add_to_setlists,
                    description = state.setlists.filterValues { it }.let { selectedSetlists ->
                        selectedSetlists.keys.joinToString { it.name }
                            .takeIf { selectedSetlists.isNotEmpty() }
                    },
                    onClick = { onAction(AddToSetlists) },
                ),
                // TODO
//                OptionsBottomSheetItem.new(
//                    icon = IconFavouriteEmpty,
//                    label = Res.string.song_menu_add_to_favourites,
//                    onClick = { onAction(SongOptionsBottomSheetAction.AddToFavourites) },
//                ),
                OptionsBottomSheetItem.Divider,
                OptionsBottomSheetItem.new(
                    icon = IconDelete,
                    color = MaterialTheme.colorScheme.error,
                    label = Res.string.song_menu_delete,
                    description = stringResource(Res.string.song_menu_delete_description),
                    onClick = { onAction(Delete) },
                ),
            ),
        )
    }
}

@Composable
private fun SongOptionsBottomSheetHeader(title: String, artist: String) {
    Column(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SongbookText(
            modifier = Modifier.fillMaxWidth(),
            text = title.takeIf(String::isNotEmpty)
                ?: stringResource(Res.string.common_unnamed),
            textStyle = defaultSongbookTextStyle().copy(
                textColor = MaterialTheme.colorScheme.onSurface,
                typography = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            ),
        )

        SongbookText(
            modifier = Modifier.fillMaxWidth(),
            text = artist.takeIf(String::isNotEmpty)
                ?: stringResource(Res.string.common_unknown),
            textStyle = defaultSongbookTextStyle().copy(
                textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                typography = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            ),
        )

        Spacer(Modifier.height(MaterialTheme.spacing.small))
    }
}

@Composable
internal fun SongOptionsBottomSheetHandler(
    show: Boolean,
    delegate: SongOptionsBottomSheetDelegate,
    onDismissRequest: () -> Unit,
) {
    val songState by delegate.songState.collectAsStateWithLifecycle()
    var isSetlistsDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isRemoveSongDialogVisible by rememberSaveable { mutableStateOf(false) }

    SongOptionsBottomSheet(
        show = show,
        state = songState,
        onDismissRequest = onDismissRequest,
        onAction = {
            onDismissRequest()

            when (it) {
                Edit -> delegate.onSongEditClicked()
                AddToQueue -> delegate.onSongAddToQueueClicked()
                AddToSetlists -> isSetlistsDialogVisible = true
                AddToFavourites -> delegate.onSongAddToFavouritesClicked()
                Delete -> isRemoveSongDialogVisible = true
            }
        },
    )

    if (isSetlistsDialogVisible) {
        SetlistsDialog(
            setlists = (songState as? Loaded)?.setlists ?: persistentMapOf(),
            onSetlistsSelected = { selectedSetlists ->
                isSetlistsDialogVisible = false
                delegate.onSongSetlistsSelected(selectedSetlists)
            },
            onDismissRequest = { isSetlistsDialogVisible = false },
        )
    }

    if (isRemoveSongDialogVisible) {
        ConfirmationDialog(
            title = Res.string.common_delete_song,
            description = Res.string.common_delete_song_description,
            onConfirmClicked = {
                isRemoveSongDialogVisible = false
                delegate.onSongDeleteClicked()
            },
            onDismissRequest = { isRemoveSongDialogVisible = false },
        )
    }
}

internal enum class SongOptionsBottomSheetAction {
    Edit, AddToQueue, AddToSetlists, AddToFavourites, Delete
}
