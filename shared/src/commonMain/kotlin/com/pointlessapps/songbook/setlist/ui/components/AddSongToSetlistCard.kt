package com.pointlessapps.songbook.setlist.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.setlist_add_song
import com.pointlessapps.songbook.ui.components.SongbookCard
import com.pointlessapps.songbook.ui.components.SongbookIcon
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.dashedSongbookCardStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookIconStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.IconPlus
import com.pointlessapps.songbook.ui.theme.spacing
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AddSongToSetlistCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SongbookCard(
        modifier = modifier,
        onClick = onClick,
        cardStyle = dashedSongbookCardStyle(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(MaterialTheme.spacing.large),
            horizontalArrangement = Arrangement.spacedBy(
                space = MaterialTheme.spacing.medium,
                alignment = Alignment.CenterHorizontally,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SongbookIcon(
                icon = IconPlus,
                iconStyle = defaultSongbookIconStyle().copy(
                    tint = MaterialTheme.colorScheme.primary,
                ),
            )

            SongbookText(
                text = stringResource(Res.string.setlist_add_song),
                textStyle = defaultSongbookTextStyle().copy(
                    textColor = MaterialTheme.colorScheme.onSurface,
                    typography = MaterialTheme.typography.titleMedium,
                ),
            )
        }
    }
}
