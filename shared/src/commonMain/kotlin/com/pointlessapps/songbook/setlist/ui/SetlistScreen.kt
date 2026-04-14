package com.pointlessapps.songbook.setlist.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.pointlessapps.songbook.LocalBottomBarPadding
import com.pointlessapps.songbook.LocalNavigator
import com.pointlessapps.songbook.core.song.database.entity.SongSearchResult
import com.pointlessapps.songbook.core.song.model.Song
import com.pointlessapps.songbook.library.DisplayMode
import com.pointlessapps.songbook.library.ui.components.SongCard
import com.pointlessapps.songbook.setlist.SetlistEvent
import com.pointlessapps.songbook.setlist.SetlistState
import com.pointlessapps.songbook.setlist.SetlistViewModel
import com.pointlessapps.songbook.setlist.ui.components.AddSongToSetlistBottomSheet
import com.pointlessapps.songbook.setlist.ui.components.AddSongToSetlistCard
import com.pointlessapps.songbook.setlist.ui.components.SetlistOptionsBottomSheet
import com.pointlessapps.songbook.setlist.ui.components.SetlistOptionsBottomSheetAction.Delete
import com.pointlessapps.songbook.setlist.ui.components.SetlistOptionsBottomSheetAction.Rename
import com.pointlessapps.songbook.setlist.ui.dialogs.RenameSetlistDialog
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.setlist_delete_from_setlist
import com.pointlessapps.songbook.shared.setlist_delete_setlist
import com.pointlessapps.songbook.shared.setlist_delete_setlist_description
import com.pointlessapps.songbook.ui.TopBar
import com.pointlessapps.songbook.ui.TopBarButton
import com.pointlessapps.songbook.ui.components.SongbookIcon
import com.pointlessapps.songbook.ui.components.SongbookLoader
import com.pointlessapps.songbook.ui.components.SongbookScaffoldLayout
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.defaultSongbookIconStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.dialogs.ConfirmDeleteDialog
import com.pointlessapps.songbook.ui.theme.IconDelete
import com.pointlessapps.songbook.ui.theme.spacing
import com.pointlessapps.songbook.utils.add
import com.pointlessapps.songbook.utils.collectWithLifecycle
import com.pointlessapps.songbook.utils.syncingTopBarButton
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SetlistScreen(
    viewModel: SetlistViewModel,
) {
    val navigator = LocalNavigator.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val songSearchResult = viewModel.songSearchResults.collectAsLazyPagingItems()

    viewModel.events.collectWithLifecycle {
        when (it) {
            is SetlistEvent.NavigateBack -> navigator.navigateBack()
            is SetlistEvent.NavigateToLyrics -> navigator.navigateToLyrics(it.songId)
        }
    }

    when (val state = state) {
        SetlistState.Loading -> SongbookLoader(true)
        is SetlistState.Loaded -> SetlistScreenContent(
            state = state,
            songSearchQueryTextFieldState = viewModel.songSearchQueryTextFieldState,
            songSearchResults = songSearchResult,
            onLyricsClicked = viewModel::onLyricsClicked,
            onNameChanged = viewModel::onNameChanged,
            onDeleteSetlistConfirmClicked = viewModel::onDeleteSetlistConfirmClicked,
            onAddSongToSetlistClicked = viewModel::onAddSongToSetlistClicked,
            onRemoveSongFromSetlistClicked = viewModel::onRemoveSongFromSetlistClicked,
            onMove = viewModel::onMove,
            onReorderDone = viewModel::onReorderDone,
        )
    }
}

@Composable
private fun SetlistScreenContent(
    state: SetlistState.Loaded,
    songSearchQueryTextFieldState: TextFieldState,
    songSearchResults: LazyPagingItems<SongSearchResult>,
    onLyricsClicked: (String) -> Unit,
    onNameChanged: (String) -> Unit,
    onDeleteSetlistConfirmClicked: () -> Unit,
    onAddSongToSetlistClicked: (String) -> Unit,
    onRemoveSongFromSetlistClicked: (String) -> Unit,
    onMove: (Int, Int) -> Unit,
    onReorderDone: () -> Unit,
) {
    var isBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
    var isAddSongToSetlistBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
    val lazyListState = rememberLazyListState()

    val coroutineScope = rememberCoroutineScope()
    val reorderingOffsetAnimatable = remember { Animatable(0f) }
    var draggedItemIndex by remember { mutableIntStateOf(-1) }
    val reorderInteractionSource = remember { MutableInteractionSource() }

    val isReordered by reorderInteractionSource.collectIsDraggedAsState()
    LaunchedEffect(isReordered) {
        if (!isReordered) {
            reorderingOffsetAnimatable.animateTo(0f)
            draggedItemIndex = -1
            onReorderDone()
        }
    }

    SongbookScaffoldLayout(
        topBar = @Composable {
            TopBar(
                leftButton = syncingTopBarButton(state.syncStatus),
                rightButton = TopBarButton.menu(
                    onClick = { isBottomSheetVisible = true },
                ),
                title = state.setlist.name,
            )
        },
    ) { paddingValues ->
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = paddingValues.add(MaterialTheme.spacing.huge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
        ) {
            itemsIndexed(state.songs, key = { _, it -> it.id }) { index, song ->
                val swipeToDismissState = rememberSwipeToDismissBoxState()
                val isDragging = index == draggedItemIndex
                val zIndex = if (isDragging) 1f else 0f

                SwipeToDismissBox(
                    modifier = Modifier
                        .zIndex(zIndex)
                        .graphicsLayer {
                            if (isDragging) translationY = reorderingOffsetAnimatable.value
                        }
                        .draggable(
                            interactionSource = reorderInteractionSource,
                            state = rememberDraggableState {
                                val itemInfos =
                                    lazyListState.layoutInfo.visibleItemsInfo.take(state.songs.size)
                                val offset = reorderingOffsetAnimatable.value + it
                                coroutineScope.launch {
                                    reorderingOffsetAnimatable.snapTo(offset)
                                }

                                val draggedItemInfo = itemInfos.find {
                                    it.index == draggedItemIndex
                                } ?: return@rememberDraggableState

                                val currentOffset = draggedItemInfo.offset + offset

                                val targetItem = itemInfos.find { item ->
                                    item.index != draggedItemIndex &&
                                            currentOffset.toInt() in item.offset..(item.offset + item.size)
                                }

                                if (targetItem != null) {
                                    onMove(draggedItemIndex, targetItem.index)
                                    draggedItemIndex = targetItem.index
                                    coroutineScope.launch {
                                        reorderingOffsetAnimatable.snapTo(
                                            currentOffset - targetItem.offset,
                                        )
                                    }
                                }
                            },
                            orientation = Orientation.Vertical,
                            onDragStarted = { draggedItemIndex = index },
                        )
                        .then(
                            if (isDragging) Modifier
                            else Modifier.animateItem(),
                        ),
                    state = swipeToDismissState,
                    enableDismissFromStartToEnd = false,
                    backgroundContent = {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(MaterialTheme.spacing.medium),
                            horizontalArrangement = Arrangement.spacedBy(
                                space = MaterialTheme.spacing.medium,
                                alignment = Alignment.End,
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            SongbookText(
                                text = stringResource(Res.string.setlist_delete_from_setlist),
                                textStyle = defaultSongbookTextStyle().copy(
                                    textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    typography = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.End,
                                ),
                            )

                            SongbookIcon(
                                icon = IconDelete,
                                iconStyle = defaultSongbookIconStyle().copy(
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                            )
                        }
                    },
                    onDismiss = {
                        coroutineScope.launch {
                            swipeToDismissState.reset()
                        }
                        onRemoveSongFromSetlistClicked(song.id)
                    },
                ) {
                    SongCard(
                        song = song,
                        displayMode = DisplayMode.List,
                        onClick = { onLyricsClicked(song.id) },
                    )
                }
            }

            item(key = "add_to_setlist_button") {
                AddSongToSetlistCard(
                    modifier = Modifier.animateItem(),
                    onClick = { isAddSongToSetlistBottomSheetVisible = true },
                )
            }

            item { Spacer(Modifier.padding(LocalBottomBarPadding.current.padding.value)) }
        }
    }

    val setlistsSongIds = remember(state.songs) { state.songs.map(Song::id).toSet() }
    AddSongToSetlistBottomSheet(
        show = isAddSongToSetlistBottomSheetVisible,
        textFieldState = songSearchQueryTextFieldState,
        searchResults = songSearchResults,
        setlistsSongIds = setlistsSongIds,
        onItemClicked = {
            if (setlistsSongIds.contains(it)) {
                onRemoveSongFromSetlistClicked(it)
            } else {
                onAddSongToSetlistClicked(it)
            }
        },
        onDismissRequest = { isAddSongToSetlistBottomSheetVisible = false },
    )

    var isRenameSetlistDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isConfirmDeleteDialogVisible by rememberSaveable { mutableStateOf(false) }

    SetlistOptionsBottomSheet(
        show = isBottomSheetVisible,
        state = state,
        onDismissRequest = { isBottomSheetVisible = false },
        onAction = {
            isBottomSheetVisible = false

            when (it) {
                Rename -> isRenameSetlistDialogVisible = true
                Delete -> isConfirmDeleteDialogVisible = true
            }
        },
    )

    if (isRenameSetlistDialogVisible) {
        RenameSetlistDialog(
            name = state.setlist.name,
            onConfirmClicked = {
                onNameChanged(it)
                isRenameSetlistDialogVisible = false
            },
            onDismissRequest = { isRenameSetlistDialogVisible = false },
        )
    }

    if (isConfirmDeleteDialogVisible) {
        ConfirmDeleteDialog(
            title = Res.string.setlist_delete_setlist,
            description = Res.string.setlist_delete_setlist_description,
            onConfirmClicked = {
                onDeleteSetlistConfirmClicked()
                isConfirmDeleteDialogVisible = false
                isBottomSheetVisible = false
            },
            onDismissRequest = { isConfirmDeleteDialogVisible = false },
        )
    }
}
