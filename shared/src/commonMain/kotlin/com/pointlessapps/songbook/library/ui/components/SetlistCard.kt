package com.pointlessapps.songbook.library.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pointlessapps.songbook.core.setlist.model.Setlist
import com.pointlessapps.songbook.shared.ui.Res
import com.pointlessapps.songbook.shared.ui.library_setlist_songs
import com.pointlessapps.songbook.ui.components.SongbookCard
import com.pointlessapps.songbook.ui.components.SongbookIcon
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.defaultSongbookIconStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.IconFolder
import com.pointlessapps.songbook.ui.theme.spacing
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SetlistCard(
    setlist: Setlist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SongbookCard(
        modifier = modifier,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 200.dp)
                .padding(MaterialTheme.spacing.large),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SongbookIcon(
                icon = IconFolder,
                iconStyle = defaultSongbookIconStyle().copy(
                    tint = MaterialTheme.colorScheme.primary,
                ),
            )

            Column {
                SongbookText(
                    text = setlist.name,
                    textStyle = defaultSongbookTextStyle().copy(
                        textColor = MaterialTheme.colorScheme.onSurface,
                        typography = MaterialTheme.typography.titleMedium,
                    ),
                )
                SongbookText(
                    text = stringResource(Res.string.library_setlist_songs, setlist.songCount),
                    textStyle = defaultSongbookTextStyle().copy(
                        textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        typography = MaterialTheme.typography.bodySmall,
                    ),
                )
            }
        }
    }
}
