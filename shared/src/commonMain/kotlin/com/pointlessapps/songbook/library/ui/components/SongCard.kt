package com.pointlessapps.songbook.library.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.pointlessapps.songbook.core.song.model.Song
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.library_add_to_favourites
import com.pointlessapps.songbook.ui.components.SongbookCard
import com.pointlessapps.songbook.ui.components.SongbookIcon
import com.pointlessapps.songbook.ui.components.SongbookIconButton
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.defaultSongbookIconButtonStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookIconStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.IconFavouriteEmpty
import com.pointlessapps.songbook.ui.theme.IconNote
import com.pointlessapps.songbook.ui.theme.spacing

@Composable
internal fun SongCard(song: Song, onClick: () -> Unit) {
    SongbookCard(
        onClick = onClick,
        onLongClick = { },
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.large),
            verticalArrangement = Arrangement.spacedBy(
                space = MaterialTheme.spacing.medium,
                alignment = Alignment.CenterVertically,
            ),
            horizontalAlignment = Alignment.Start,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SongbookIcon(
                    icon = IconNote,
                    iconStyle = defaultSongbookIconStyle().copy(
                        tint = MaterialTheme.colorScheme.primary,
                    ),
                )
                SongbookIconButton(
                    icon = IconFavouriteEmpty,
                    tooltipLabel = Res.string.library_add_to_favourites,
                    onClick = {},
                    iconButtonStyle = defaultSongbookIconButtonStyle().copy(
                        outlineColor = Color.Transparent,
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                )
            }

            Column {
                SongbookText(
                    text = song.title,
                    textStyle = defaultSongbookTextStyle().copy(
                        textColor = MaterialTheme.colorScheme.onSurface,
                        typography = MaterialTheme.typography.titleMedium,
                    ),
                )
                SongbookText(
                    text = song.artist,
                    textStyle = defaultSongbookTextStyle().copy(
                        textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        typography = MaterialTheme.typography.bodySmall,
                    ),
                )
            }
        }
    }
}
