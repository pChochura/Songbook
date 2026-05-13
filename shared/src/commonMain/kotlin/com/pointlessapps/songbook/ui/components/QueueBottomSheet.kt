package com.pointlessapps.songbook.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pointlessapps.songbook.core.queue.QueueManager
import com.pointlessapps.songbook.core.song.model.Song
import com.pointlessapps.songbook.shared.ui.Res
import com.pointlessapps.songbook.shared.ui.common_currently_played
import com.pointlessapps.songbook.shared.ui.common_done
import com.pointlessapps.songbook.shared.ui.common_drag_to_reorder
import com.pointlessapps.songbook.shared.ui.common_edit
import com.pointlessapps.songbook.shared.ui.common_remove
import com.pointlessapps.songbook.shared.ui.common_unknown
import com.pointlessapps.songbook.shared.ui.common_unnamed
import com.pointlessapps.songbook.shared.ui.library_queue_bottomsheet_title
import com.pointlessapps.songbook.ui.OptionsBottomSheetTitleHeader
import com.pointlessapps.songbook.ui.theme.DEFAULT_BORDER_WIDTH
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
    SongbookBottomSheet(
        show = show,
        onDismissRequest = onDismissRequest,
    ) {
        val queueManager: QueueManager = koinInject()
        val queue by queueManager.queueFlow.collectAsStateWithLifecycle()
        val currentlyPlayedSong by queueManager.currentSongFlow.collectAsStateWithLifecycle()

        val localSongs = remember(queue) { queue.toMutableStateList() }
        val selectedSongIds = rememberSaveable { mutableStateSetOf<String>() }
        val alreadyPlayedSongsIndex =
            localSongs.indexOfFirst { it.id == currentlyPlayedSong?.id }

        val lazyListState = rememberLazyListState()
        val hapticFeedback = LocalHapticFeedback.current
        val reorderableLazyListState =
            rememberReorderableLazyListState(lazyListState) { from, to ->
                localSongs.add(to.index, localSongs.removeAt(from.index))
                hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
            }

        var isInEditMode by rememberSaveable { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            lazyListState.requestScrollToItem(alreadyPlayedSongsIndex)
        }

        Column(
            modifier = Modifier
                .animateContentSize()
                .systemBarsPadding()
                .padding(MaterialTheme.spacing.extraLarge),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                OptionsBottomSheetTitleHeader(
                    title = stringResource(Res.string.library_queue_bottomsheet_title),
                )

                SongbookButton(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    label = stringResource(
                        if (isInEditMode) {
                            Res.string.common_done
                        } else {
                            Res.string.common_edit
                        },
                    ),
                    onClick = {
                        isInEditMode = !isInEditMode
                        selectedSongIds.clear()
                    },
                    buttonStyle = defaultSongbookButtonStyle().copy(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        textStyle = defaultSongbookButtonTextStyle().copy(
                            textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        ),
                    ),
                )
            }
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
                        isInEditMode = isInEditMode,
                        isSelected = selectedSongIds.contains(song.id),
                        isAlreadyPlayed = index < alreadyPlayedSongsIndex,
                        isCurrentlyPlayed = song.id == currentlyPlayedSong?.id,
                        reorderableLazyListState = reorderableLazyListState,
                        onReorderDone = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            queueManager.updateOrder(localSongs.map(Song::id))
                        },
                        onClicked = {
                            if (isInEditMode) {
                                if (selectedSongIds.contains(song.id)) {
                                    selectedSongIds.remove(song.id)
                                } else {
                                    selectedSongIds.add(song.id)
                                }
                            } else {
                                queueManager.setSong(song.id)
                            }
                        },
                    )
                }
            }

            AnimatedVisibility(
                visible = isInEditMode,
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Top, clip = false),
                exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top, clip = false),
            ) {
                val borderColor = MaterialTheme.colorScheme.outlineVariant
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            drawLine(
                                color = borderColor,
                                start = Offset.Zero,
                                end = Offset(size.width, 0f),
                                strokeWidth = DEFAULT_BORDER_WIDTH.toPx(),
                            )
                        }
                        .padding(vertical = MaterialTheme.spacing.medium),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SongbookButton(
                        label = stringResource(Res.string.common_remove),
                        buttonStyle = defaultSongbookButtonStyle().copy(
                            enabled = selectedSongIds.isNotEmpty(),
                        ),
                        onClick = {
                            queueManager.removeFromQueue(selectedSongIds.toList())
                            isInEditMode = false
                            selectedSongIds.clear()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun LazyItemScope.QueueItemCard(
    song: Song,
    isInEditMode: Boolean,
    isSelected: Boolean,
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
                    enabled = !isCurrentlyPlayed || isInEditMode,
                    role = Role.Button,
                    onClick = onClicked,
                )
                .alpha(if (isAlreadyPlayed) 0.5f else 1f)
                .padding(vertical = MaterialTheme.spacing.large),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedVisibility(
                visible = isInEditMode,
                enter = fadeIn() + expandHorizontally(clip = false),
                exit = fadeOut() + shrinkHorizontally(clip = false),
            ) {
                SongbookCheckbox(
                    modifier = Modifier.padding(
                        end = MaterialTheme.spacing.medium,
                    ),
                    checked = isSelected,
                    onCheckChanged = onClicked,
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                SongbookText(
                    text = song.title.takeIf(String::isNotEmpty)
                        ?: stringResource(Res.string.common_unnamed),
                    textStyle = defaultSongbookTextStyle().copy(
                        textColor = MaterialTheme.colorScheme.onSurface,
                        typography = MaterialTheme.typography.titleMedium,
                    ),
                )
                SongbookText(
                    text = song.artist.takeIf(String::isNotEmpty)
                        ?: stringResource(Res.string.common_unknown),
                    textStyle = defaultSongbookTextStyle().copy(
                        textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        typography = MaterialTheme.typography.bodySmall,
                    ),
                )
            }

            Spacer(Modifier.width(MaterialTheme.spacing.medium))

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
