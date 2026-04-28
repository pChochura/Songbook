package com.pointlessapps.songbook.setlist.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.pointlessapps.songbook.core.song.model.Song
import com.pointlessapps.songbook.library.DisplayMode
import com.pointlessapps.songbook.library.ui.components.SongCard
import com.pointlessapps.songbook.shared.ui.Res
import com.pointlessapps.songbook.shared.ui.setlist_delete_from_setlist
import com.pointlessapps.songbook.ui.components.SongbookIcon
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.defaultSongbookIconStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.IconDelete
import com.pointlessapps.songbook.ui.theme.spacing
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SetlistSongItem(
    song: Song,
    onLyricsClicked: (Song) -> Unit,
    onRemoveSongFromSetlistClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val swipeToDismissState = rememberSwipeToDismissBoxState()

    SwipeToDismissBox(
        modifier = modifier,
        state = swipeToDismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = { SetlistSwipeToDismissBackground() },
        onDismiss = {
            coroutineScope.launch { swipeToDismissState.reset() }
            onRemoveSongFromSetlistClicked(song.id)
        },
    ) {
        SongCard(
            song = song,
            displayMode = DisplayMode.List,
            onClick = { onLyricsClicked(song) },
        )
    }
}

@Composable
private fun SetlistSwipeToDismissBackground() {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.medium),
        horizontalArrangement = Arrangement.spacedBy(
            space = MaterialTheme.spacing.medium,
            alignment = Alignment.End,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SongbookText(
            text = stringResource(Res.string.setlist_delete_from_setlist),
            textStyle = defaultSongbookTextStyle().copy(
                textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                typography = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.End,
            ),
        )

        SongbookIcon(
            icon = IconDelete,
            iconStyle = defaultSongbookIconStyle().copy(
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )
    }
}
