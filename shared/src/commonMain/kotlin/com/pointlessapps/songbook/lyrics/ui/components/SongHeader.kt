package com.pointlessapps.songbook.lyrics.ui.components

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.pointlessapps.songbook.LocalSharedTransitionScope
import com.pointlessapps.songbook.shared.ui.Res
import com.pointlessapps.songbook.shared.ui.common_unknown
import com.pointlessapps.songbook.shared.ui.common_unnamed
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.spacing
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SongHeader(
    songId: String?,
    title: String,
    artist: String,
    modifier: Modifier = Modifier,
) {
    LocalSharedTransitionScope { animatedContentScope ->
        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(
                space = MaterialTheme.spacing.small,
                alignment = Alignment.CenterVertically,
            ),
        ) {
            SongbookText(
                modifier = Modifier.sharedBounds(
                    sharedContentState = rememberSharedContentState("title-${songId}"),
                    animatedVisibilityScope = animatedContentScope,
                    resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(),
                ),
                text = title.takeIf { it.isNotEmpty() }
                    ?: stringResource(Res.string.common_unnamed),
                textStyle = defaultSongbookTextStyle().copy(
                    textColor = MaterialTheme.colorScheme.onSurface,
                    typography = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                ),
            )
            SongbookText(
                modifier = Modifier.sharedBounds(
                    sharedContentState = rememberSharedContentState("artist-${songId}"),
                    animatedVisibilityScope = animatedContentScope,
                    resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(),
                ),
                text = artist.takeIf { it.isNotEmpty() }
                    ?: stringResource(Res.string.common_unknown),
                textStyle = defaultSongbookTextStyle().copy(
                    textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    typography = MaterialTheme.typography.labelMedium,
                ),
            )

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
        }
    }
}
