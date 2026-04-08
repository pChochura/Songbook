package com.pointlessapps.songbook.lyrics.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.pointlessapps.songbook.lyrics.DisplayMode
import com.pointlessapps.songbook.lyrics.LyricsState
import com.pointlessapps.songbook.lyrics.WrapMode
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.common_unknown
import com.pointlessapps.songbook.shared.common_unnamed
import com.pointlessapps.songbook.shared.lyrics_display_mode_both
import com.pointlessapps.songbook.shared.lyrics_display_mode_inline
import com.pointlessapps.songbook.shared.lyrics_display_mode_side_by_side
import com.pointlessapps.songbook.shared.lyrics_display_mode_text_only
import com.pointlessapps.songbook.shared.lyrics_menu_add_to_setlist
import com.pointlessapps.songbook.shared.lyrics_menu_broadcast_to_team
import com.pointlessapps.songbook.shared.lyrics_menu_broadcast_to_team_description
import com.pointlessapps.songbook.shared.lyrics_menu_delete
import com.pointlessapps.songbook.shared.lyrics_menu_delete_description
import com.pointlessapps.songbook.shared.lyrics_menu_display_mode
import com.pointlessapps.songbook.shared.lyrics_menu_edit
import com.pointlessapps.songbook.shared.lyrics_menu_key_offset
import com.pointlessapps.songbook.shared.lyrics_menu_show_queue
import com.pointlessapps.songbook.shared.lyrics_menu_text_scale
import com.pointlessapps.songbook.shared.lyrics_menu_toggle_fullscreen
import com.pointlessapps.songbook.shared.lyrics_menu_toggle_fullscreen_description
import com.pointlessapps.songbook.shared.lyrics_menu_wrap_mode
import com.pointlessapps.songbook.shared.lyrics_wrap_mode_no_wrap
import com.pointlessapps.songbook.shared.lyrics_wrap_mode_wrap
import com.pointlessapps.songbook.ui.OptionsBottomSheet
import com.pointlessapps.songbook.ui.OptionsBottomSheetItem
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.IconAddFolder
import com.pointlessapps.songbook.ui.theme.IconDelete
import com.pointlessapps.songbook.ui.theme.IconDisplayMode
import com.pointlessapps.songbook.ui.theme.IconEdit
import com.pointlessapps.songbook.ui.theme.IconFullscreen
import com.pointlessapps.songbook.ui.theme.IconKey
import com.pointlessapps.songbook.ui.theme.IconQueue
import com.pointlessapps.songbook.ui.theme.IconTextSize
import com.pointlessapps.songbook.ui.theme.IconVoice
import com.pointlessapps.songbook.ui.theme.IconWrapMode
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
    OptionsBottomSheet(
        show = show,
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
                icon = IconFullscreen,
                label = Res.string.lyrics_menu_toggle_fullscreen,
                description = stringResource(Res.string.lyrics_menu_toggle_fullscreen_description),
                onClick = { onAction(LyricsOptionsBottomSheetAction.Fullscreen) },
            ),
            OptionsBottomSheetItem.new(
                icon = IconWrapMode,
                label = Res.string.lyrics_menu_wrap_mode,
                description = stringResource(
                    when (state.wrapMode) {
                        WrapMode.Wrap -> Res.string.lyrics_wrap_mode_wrap
                        WrapMode.NoWrap -> Res.string.lyrics_wrap_mode_no_wrap
                    },
                ),
                onClick = { onAction(LyricsOptionsBottomSheetAction.WrapMode) },
            ),
            OptionsBottomSheetItem.new(
                icon = IconDisplayMode,
                label = Res.string.lyrics_menu_display_mode,
                description = stringResource(
                    when (state.displayMode) {
                        DisplayMode.Inline -> Res.string.lyrics_display_mode_inline
                        DisplayMode.SideBySide -> Res.string.lyrics_display_mode_side_by_side
                        DisplayMode.Both -> Res.string.lyrics_display_mode_both
                        DisplayMode.TextOnly -> Res.string.lyrics_display_mode_text_only
                    },
                ),
                onClick = { onAction(LyricsOptionsBottomSheetAction.DisplayMode) },
            ),
            OptionsBottomSheetItem.new(
                icon = IconTextSize,
                label = Res.string.lyrics_menu_text_scale,
                description = "${state.textScale}%",
                onClick = { onAction(LyricsOptionsBottomSheetAction.TextScale) },
            ),
            OptionsBottomSheetItem.new(
                icon = IconKey,
                label = Res.string.lyrics_menu_key_offset,
                description = "${if (state.keyOffset > 0) "+" else ""}${
                    state.keyOffset
                }".takeIf { state.keyOffset != 0 },
                onClick = { onAction(LyricsOptionsBottomSheetAction.KeyOffset) },
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

@Composable
private fun LyricsOptionsBottomSheetHeader(title: String, artist: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SongbookText(
            modifier = Modifier.fillMaxWidth(),
            text = title.takeIf { it.isNotEmpty() }
                ?: stringResource(Res.string.common_unnamed),
            textStyle = defaultSongbookTextStyle().copy(
                textColor = MaterialTheme.colorScheme.onSurface,
                typography = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            ),
        )

        SongbookText(
            modifier = Modifier.fillMaxWidth(),
            text = artist.takeIf { it.isNotEmpty() }
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

internal enum class LyricsOptionsBottomSheetAction {
    Edit, Fullscreen, WrapMode, DisplayMode, TextScale, KeyOffset, AddToSetlist, ShowQueue, Broadcast, Delete
}
