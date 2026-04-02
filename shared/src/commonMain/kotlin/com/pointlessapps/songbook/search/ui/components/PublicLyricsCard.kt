package com.pointlessapps.songbook.search.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pointlessapps.songbook.core.song.model.PublicLyrics
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.common_unknown
import com.pointlessapps.songbook.shared.common_unnamed
import com.pointlessapps.songbook.ui.components.SongbookCard
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.spacing
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PublicLyricsCard(
    lyrics: PublicLyrics,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SongbookCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        onLongClick = { },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.large),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        ) {
            Column {
                SongbookText(
                    text = lyrics.trackName.takeIf { it.isNotEmpty() }
                        ?: stringResource(Res.string.common_unnamed),
                    textStyle = defaultSongbookTextStyle().copy(
                        textColor = MaterialTheme.colorScheme.onSurface,
                        typography = MaterialTheme.typography.titleMedium,
                    ),
                )
                SongbookText(
                    text = lyrics.artistName.takeIf { it.isNotEmpty() }
                        ?: stringResource(Res.string.common_unknown),
                    textStyle = defaultSongbookTextStyle().copy(
                        textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        typography = MaterialTheme.typography.bodySmall,
                    ),
                )
            }
        }
    }
}
