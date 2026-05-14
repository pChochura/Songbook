package com.pointlessapps.songbook.search.ui.components

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
import com.pointlessapps.songbook.core.song.model.PublicLyrics
import com.pointlessapps.songbook.shared.ui.Res
import com.pointlessapps.songbook.shared.ui.common_unknown
import com.pointlessapps.songbook.shared.ui.common_unnamed
import com.pointlessapps.songbook.shared.ui.import_menu_preview
import com.pointlessapps.songbook.ui.components.SongbookCard
import com.pointlessapps.songbook.ui.components.SongbookIconButton
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.defaultSongbookIconButtonStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.IconVisibility
import com.pointlessapps.songbook.ui.theme.spacing
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PublicLyricsCard(
    lyrics: PublicLyrics,
    onClicked: () -> Unit,
    onPreviewClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SongbookCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClicked,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.large),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                SongbookText(
                    text = lyrics.trackName.takeIf(String::isNotEmpty)
                        ?: stringResource(Res.string.common_unnamed),
                    textStyle = defaultSongbookTextStyle().copy(
                        textColor = MaterialTheme.colorScheme.onSurface,
                        typography = MaterialTheme.typography.titleMedium,
                    ),
                )
                SongbookText(
                    text = lyrics.artistName.takeIf(String::isNotEmpty)
                        ?: stringResource(Res.string.common_unknown),
                    textStyle = defaultSongbookTextStyle().copy(
                        textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        typography = MaterialTheme.typography.bodySmall,
                    ),
                )
            }

            SongbookIconButton(
                icon = IconVisibility,
                tooltipLabel = Res.string.import_menu_preview,
                onClick = onPreviewClicked,
                iconButtonStyle = defaultSongbookIconButtonStyle().copy(
                    outlineColor = Color.Transparent,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        }
    }
}
