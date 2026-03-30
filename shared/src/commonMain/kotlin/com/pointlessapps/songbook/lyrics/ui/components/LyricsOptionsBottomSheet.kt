package com.pointlessapps.songbook.lyrics.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.pointlessapps.songbook.lyrics.LyricsMode
import com.pointlessapps.songbook.lyrics.LyricsState
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.lyrics_menu_add_to_setlist
import com.pointlessapps.songbook.shared.lyrics_menu_broadcast_to_team
import com.pointlessapps.songbook.shared.lyrics_menu_broadcast_to_team_description
import com.pointlessapps.songbook.shared.lyrics_menu_delete
import com.pointlessapps.songbook.shared.lyrics_menu_delete_description
import com.pointlessapps.songbook.shared.lyrics_menu_edit
import com.pointlessapps.songbook.shared.lyrics_menu_mode
import com.pointlessapps.songbook.shared.lyrics_menu_show_queue
import com.pointlessapps.songbook.shared.lyrics_menu_text_scale
import com.pointlessapps.songbook.shared.lyrics_mode_inline
import com.pointlessapps.songbook.shared.lyrics_mode_side_by_side
import com.pointlessapps.songbook.shared.lyrics_mode_text_only
import com.pointlessapps.songbook.ui.OptionsBottomSheet
import com.pointlessapps.songbook.ui.OptionsBottomSheetItem
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.IconAddFolder
import com.pointlessapps.songbook.ui.theme.IconDelete
import com.pointlessapps.songbook.ui.theme.IconEdit
import com.pointlessapps.songbook.ui.theme.IconMode
import com.pointlessapps.songbook.ui.theme.IconQueue
import com.pointlessapps.songbook.ui.theme.IconTextSize
import com.pointlessapps.songbook.ui.theme.IconVoice
import com.pointlessapps.songbook.ui.theme.spacing
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LyricsOptionsBottomSheet(
    show: Boolean,
    state: LyricsState,
    onDismissRequest: () -> Unit,
    onAction: (LyricsOptionsBottomSheetAction) -> Unit,
) {
    if (show) {
        OptionsBottomSheet(
            state = rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
            ),
            onDismissRequest = onDismissRequest,
            header = {
                LyricsOptionsBottomSheetHeader(
                    title = state.title,
                    artist = state.artist,
                )
            },
            items = listOf(
                OptionsBottomSheetItem.Divider,
                OptionsBottomSheetItem.new(
                    icon = IconEdit,
                    label = Res.string.lyrics_menu_edit,
                    onClick = { onAction(LyricsOptionsBottomSheetAction.Edit) },
                ),
                OptionsBottomSheetItem.Divider,
                OptionsBottomSheetItem.new(
                    icon = IconMode,
                    label = Res.string.lyrics_menu_mode,
                    description = stringResource(
                        when (state.mode) {
                            LyricsMode.Inline -> Res.string.lyrics_mode_inline
                            LyricsMode.SideBySide -> Res.string.lyrics_mode_side_by_side
                            LyricsMode.TextOnly -> Res.string.lyrics_mode_text_only
                        },
                    ),
                    onClick = { onAction(LyricsOptionsBottomSheetAction.Mode) },
                ),
                OptionsBottomSheetItem.new(
                    icon = IconTextSize,
                    label = Res.string.lyrics_menu_text_scale,
                    description = "${state.textScale}%",
                    onClick = { onAction(LyricsOptionsBottomSheetAction.TextScale) },
                ),
                OptionsBottomSheetItem.Divider,
                OptionsBottomSheetItem.new(
                    icon = IconAddFolder,
                    label = Res.string.lyrics_menu_add_to_setlist,
                    onClick = { onAction(LyricsOptionsBottomSheetAction.AddToSetlist) },
                ),
                OptionsBottomSheetItem.new(
                    icon = IconQueue,
                    label = Res.string.lyrics_menu_show_queue,
                    onClick = { onAction(LyricsOptionsBottomSheetAction.ShowQueue) },
                ),
                OptionsBottomSheetItem.new(
                    icon = IconVoice,
                    label = Res.string.lyrics_menu_broadcast_to_team,
                    description = stringResource(Res.string.lyrics_menu_broadcast_to_team_description),
                    onClick = { onAction(LyricsOptionsBottomSheetAction.Broadcast) },
                ),
                OptionsBottomSheetItem.Divider,
                OptionsBottomSheetItem.new(
                    icon = IconDelete,
                    color = MaterialTheme.colorScheme.error,
                    label = Res.string.lyrics_menu_delete,
                    description = stringResource(Res.string.lyrics_menu_delete_description),
                    onClick = { onAction(LyricsOptionsBottomSheetAction.Delete) },
                ),
            ),
        )
    }
}

@Composable
private fun LyricsOptionsBottomSheetHeader(title: String, artist: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SongbookText(
            modifier = Modifier.fillMaxWidth(),
            text = title,
            textStyle = defaultSongbookTextStyle().copy(
                textColor = MaterialTheme.colorScheme.onSurface,
                typography = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            ),
        )

        SongbookText(
            modifier = Modifier.fillMaxWidth(),
            text = artist,
            textStyle = defaultSongbookTextStyle().copy(
                textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                typography = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            ),
        )

        Spacer(Modifier.height(MaterialTheme.spacing.small))
    }
}

internal enum class LyricsOptionsBottomSheetAction {
    Edit, Mode, TextScale, AddToSetlist, ShowQueue, Broadcast, Delete
}
