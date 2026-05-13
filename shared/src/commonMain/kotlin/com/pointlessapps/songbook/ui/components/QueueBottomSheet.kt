package com.pointlessapps.songbook.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pointlessapps.songbook.core.queue.QueueManager
import com.pointlessapps.songbook.core.song.model.Song
import com.pointlessapps.songbook.shared.ui.Res
import com.pointlessapps.songbook.shared.ui.common_currently_played
import com.pointlessapps.songbook.shared.ui.common_drag_to_reorder
import com.pointlessapps.songbook.shared.ui.library_queue_bottomsheet_title
import com.pointlessapps.songbook.ui.OptionsBottomSheetTitleHeader
import com.pointlessapps.songbook.ui.theme.IconMoveHandle
import com.pointlessapps.songbook.ui.theme.IconPlay
import com.pointlessapps.songbook.ui.theme.spacing
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyListState
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
internal fun QueueBottomSheet(
    show: Boolean,
    onDismissRequest: () -> Unit,
) {
    val queueManager: QueueManager = koinInject()
    val queue by queueManager.queueFlow.collectAsStateWithLifecycle()
    val currentlyPlayedSong by queueManager.currentSongFlow.collectAsStateWithLifecycle()

    val localSongs = remember(queue) { queue.toMutableStateList() }
    val alreadyPlayedSongsIndex = localSongs.indexOfFirst { it.id == currentlyPlayedSong?.id }

    val lazyListState = rememberLazyListState()
    val hapticFeedback = LocalHapticFeedback.current
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        localSongs.add(to.index, localSongs.removeAt(from.index))
        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }

    SongbookBottomSheet(
        show = show,
        onDismissRequest = onDismissRequest,
    ) {
        Column(
            modifier = Modifier
                .animateContentSize()
                .statusBarsPadding()
                .padding(all = MaterialTheme.spacing.extraLarge),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        ) {
            OptionsBottomSheetTitleHeader(stringResource(Res.string.library_queue_bottomsheet_title))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth(),
            ) {
                itemsIndexed(
                    items = localSongs,
                    key = { _, song -> song.id },
                ) { index, song ->
                    QueueItemCard(
                        song = song,
                        isAlreadyPlayed = index < alreadyPlayedSongsIndex,
                        isCurrentlyPlayed = song.id == currentlyPlayedSong?.id,
                        reorderableLazyListState = reorderableLazyListState,
                        onReorderDone = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            queueManager.updateOrder(localSongs.map(Song::id))
                        },
                        onClicked = { queueManager.setSong(song.id) },
                    )
                }
            }

            Spacer(Modifier.fillMaxWidth().navigationBarsPadding())
        }
    }
}

@Composable
private fun LazyItemScope.QueueItemCard(
    song: Song,
    isAlreadyPlayed: Boolean,
    isCurrentlyPlayed: Boolean,
    reorderableLazyListState: ReorderableLazyListState,
    onReorderDone: () -> Unit,
    onClicked: () -> Unit,
) {
    ReorderableItem(
        state = reorderableLazyListState,
        key = song.id,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = !isCurrentlyPlayed,
                    role = Role.Button,
                    onClick = onClicked,
                )
                .alpha(if (isAlreadyPlayed) 0.5f else 1f)
                .padding(vertical = MaterialTheme.spacing.large),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
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

            AnimatedContent(
                targetState = isCurrentlyPlayed,
                transitionSpec = { fadeIn() + scaleIn() togetherWith fadeOut() + scaleOut() },
            ) { isCurrentlyPlayed ->
                SongbookTooltip(
                    modifier = Modifier.draggableHandle(
                        onDragStopped = onReorderDone,
                    ),
                    position = Position.ABOVE,
                    contentDescription = if (isCurrentlyPlayed) {
                        Res.string.common_currently_played
                    } else {
                        Res.string.common_drag_to_reorder
                    },
                ) {
                    if (isCurrentlyPlayed) {
                        SongbookIcon(
                            icon = IconPlay,
                            iconStyle = defaultSongbookIconStyle().copy(
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    } else {
                        SongbookIcon(
                            icon = IconMoveHandle,
                            iconStyle = defaultSongbookIconStyle().copy(
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                    alpha = 0.3f,
                                ),
                            ),
                        )
                    }
                }
            }
        }
    }
}
