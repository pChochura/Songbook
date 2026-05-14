package com.pointlessapps.songbook.library.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.SharedContentConfig
import androidx.compose.animation.SharedTransitionScope.SharedContentState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.movableContentWithReceiverOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import com.pointlessapps.songbook.LocalSharedTransitionScope
import com.pointlessapps.songbook.core.song.model.Song
import com.pointlessapps.songbook.library.DisplayMode
import com.pointlessapps.songbook.shared.ui.Res
import com.pointlessapps.songbook.shared.ui.common_unknown
import com.pointlessapps.songbook.shared.ui.common_unnamed
import com.pointlessapps.songbook.shared.ui.library_add_to_favourites
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
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SongCard(
    song: Song,
    displayMode: DisplayMode,
    onClicked: () -> Unit,
    onLongClicked: () -> Unit = {},
    enableSharedElementTransitions: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val sharedContentConfig = object : SharedContentConfig {
        override val SharedContentState.isEnabled: Boolean
            get() = enableSharedElementTransitions
    }

    val songIdentityComposable = remember(song.id) {
        movableContentWithReceiverOf<Modifier> {
            LocalSharedTransitionScope { animatedContentScope ->
                Column(modifier = this@movableContentWithReceiverOf) {
                    val titleModifier = Modifier.sharedBounds(
                        sharedContentState = rememberSharedContentState(
                            key = "title-${song.id}",
                            config = sharedContentConfig,
                        ),
                        animatedVisibilityScope = animatedContentScope,
                        resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(),
                    )
                    SongbookText(
                        modifier = titleModifier,
                        text = song.title.takeIf(String::isNotEmpty)
                            ?: stringResource(Res.string.common_unnamed),
                        textStyle = defaultSongbookTextStyle().copy(
                            textColor = MaterialTheme.colorScheme.onSurface,
                            typography = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            textOverflow = TextOverflow.Ellipsis,
                        ),
                    )

                    val artistModifier = Modifier.sharedBounds(
                        sharedContentState = rememberSharedContentState(
                            key = "artist-${song.id}",
                            config = sharedContentConfig,
                        ),
                        animatedVisibilityScope = animatedContentScope,
                        resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(),
                    )
                    SongbookText(
                        modifier = artistModifier,
                        text = song.artist.takeIf(String::isNotEmpty)
                            ?: stringResource(Res.string.common_unknown),
                        textStyle = defaultSongbookTextStyle().copy(
                            textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            typography = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            textOverflow = TextOverflow.Ellipsis,
                        ),
                    )
                }
            }
        }
    }

    SongbookCard(
        modifier = modifier,
        onClick = onClicked,
        onLongClick = onLongClicked,
    ) {
        AnimatedContent(displayMode) { displayMode ->
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                ) {
                    SongbookIcon(
                        icon = IconNote,
                        iconStyle = defaultSongbookIconStyle().copy(
                            tint = MaterialTheme.colorScheme.primary,
                        ),
                    )

                    if (displayMode == DisplayMode.List) {
                        songIdentityComposable(Modifier.weight(1f))
                    } else {
                        Spacer(Modifier.weight(1f))
                    }

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

                if (displayMode == DisplayMode.Grid) {
                    songIdentityComposable(Modifier)
                }
            }
        }
    }
}
