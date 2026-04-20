package com.pointlessapps.songbook.setlist.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.pointlessapps.songbook.LocalBottomBarPadding
import com.pointlessapps.songbook.LocalNavigator
import com.pointlessapps.songbook.core.song.database.entity.SongSearchResult
import com.pointlessapps.songbook.core.song.model.Song
import com.pointlessapps.songbook.setlist.SetlistEvent
import com.pointlessapps.songbook.setlist.SetlistState
import com.pointlessapps.songbook.setlist.SetlistViewModel
import com.pointlessapps.songbook.setlist.ui.components.AddSongToSetlistBottomSheet
import com.pointlessapps.songbook.setlist.ui.components.AddSongToSetlistCard
import com.pointlessapps.songbook.setlist.ui.components.SetlistOptionsBottomSheet
import com.pointlessapps.songbook.setlist.ui.components.SetlistOptionsBottomSheetAction.Delete
import com.pointlessapps.songbook.setlist.ui.components.SetlistOptionsBottomSheetAction.Rename
import com.pointlessapps.songbook.setlist.ui.components.SetlistSongItem
import com.pointlessapps.songbook.setlist.ui.dialogs.RenameSetlistDialog
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.setlist_delete_setlist
import com.pointlessapps.songbook.shared.setlist_delete_setlist_description
import com.pointlessapps.songbook.ui.TopBar
import com.pointlessapps.songbook.ui.TopBarButton
import com.pointlessapps.songbook.ui.components.SongbookLoader
import com.pointlessapps.songbook.ui.components.SongbookScaffoldLayout
import com.pointlessapps.songbook.ui.dialogs.ConfirmDeleteDialog
import com.pointlessapps.songbook.ui.theme.spacing
import com.pointlessapps.songbook.utils.add
import com.pointlessapps.songbook.utils.collectWithLifecycle
import com.pointlessapps.songbook.utils.syncingTopBarButton
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

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
    val hapticFeedback = LocalHapticFeedback.current
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        onMove(from.index, to.index)
        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }

    SongbookScaffoldLayout(
        topBar = @Composable {
            SetlistTopBar(
                state = state,
                onMenuClicked = { isBottomSheetVisible = true },
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
            items(state.songs, key = { it.id }) { song ->
                ReorderableItem(
                    state = reorderableLazyListState,
                    key = song.id,
                ) {
                    SetlistSongItem(
                        modifier = Modifier.longPressDraggableHandle(
                            onDragStopped = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                onReorderDone()
                            },
                        ),
                        song = song,
                        onLyricsClicked = onLyricsClicked,
                        onRemoveSongFromSetlistClicked = onRemoveSongFromSetlistClicked,
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
        onDismissRequest = {
            isAddSongToSetlistBottomSheetVisible = false
            songSearchQueryTextFieldState.clearText()
        },
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

@Composable
private fun SetlistTopBar(
    state: SetlistState.Loaded,
    onMenuClicked: () -> Unit,
) {
    TopBar(
        leftButton = syncingTopBarButton(state.syncStatus),
        rightButton = TopBarButton.menu(
            onClick = onMenuClicked,
        ),
        title = state.setlist.name,
    )
}
