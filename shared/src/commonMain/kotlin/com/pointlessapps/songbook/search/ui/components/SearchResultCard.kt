package com.pointlessapps.songbook.search.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import com.pointlessapps.songbook.core.song.model.SongSearchResult
import com.pointlessapps.songbook.search.ui.utils.parseSnippet
import com.pointlessapps.songbook.shared.ui.Res
import com.pointlessapps.songbook.shared.ui.common_unknown
import com.pointlessapps.songbook.shared.ui.common_unnamed
import com.pointlessapps.songbook.ui.components.SongbookCard
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.spacing
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SearchResultCard(
    result: SongSearchResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SongbookCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.large),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        ) {
            Column {
                SongbookText(
                    text = result.title.takeIf(String::isNotEmpty)
                        ?: stringResource(Res.string.common_unnamed),
                    textStyle = defaultSongbookTextStyle().copy(
                        textColor = MaterialTheme.colorScheme.onSurface,
                        typography = MaterialTheme.typography.titleMedium,
                    ),
                )
                SongbookText(
                    text = result.artist.takeIf(String::isNotEmpty)
                        ?: stringResource(Res.string.common_unknown),
                    textStyle = defaultSongbookTextStyle().copy(
                        textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        typography = MaterialTheme.typography.bodySmall,
                    ),
                )
            }

            SongbookText(
                text = parseSnippet(
                    snippet = result.snippet,
                    highlightStyle = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    ),
                ),
                textStyle = defaultSongbookTextStyle().copy(
                    textColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    typography = MaterialTheme.typography.bodyMedium,
                ),
            )
        }
    }
}
