package com.pointlessapps.songbook.setlist.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.pointlessapps.songbook.LocalBottomBarPadding
import com.pointlessapps.songbook.LocalNavigator
import com.pointlessapps.songbook.core.song.model.Song
import com.pointlessapps.songbook.library.DisplayMode
import com.pointlessapps.songbook.library.ui.components.SongCard
import com.pointlessapps.songbook.setlist.SetlistEvent
import com.pointlessapps.songbook.setlist.SetlistState
import com.pointlessapps.songbook.setlist.SetlistViewModel
import com.pointlessapps.songbook.setlist.ui.components.SetlistOptionsBottomSheet
import com.pointlessapps.songbook.setlist.ui.components.SetlistOptionsBottomSheetAction.Delete
import com.pointlessapps.songbook.setlist.ui.components.SetlistOptionsBottomSheetAction.Edit
import com.pointlessapps.songbook.setlist.ui.dialogs.EditSetlistDialog
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

@Composable
internal fun SetlistScreen(
    viewModel: SetlistViewModel,
) {
    val navigator = LocalNavigator.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val songs = viewModel.songs.collectAsLazyPagingItems()

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
            songs = songs,
            onLyricsClicked = viewModel::onLyricsClicked,
            onNameChanged = viewModel::onNameChanged,
            onDeleteSetlistConfirmClicked = viewModel::onDeleteSetlistConfirmClicked,
            onMove = viewModel::onMove,
            onReorderDone = viewModel::onReorderDone,
        )
    }
}

@Composable
private fun SetlistScreenContent(
    state: SetlistState.Loaded,
    songs: LazyPagingItems<Song>,
    onLyricsClicked: (Long) -> Unit,
    onNameChanged: (String) -> Unit,
    onDeleteSetlistConfirmClicked: () -> Unit,
    onMove: (Int, Int) -> Unit,
    onReorderDone: () -> Unit,
) {
    var isBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
    val lazyListState = rememberLazyListState()

    val coroutineScope = rememberCoroutineScope()
    val draggingOffsetAnimatable = remember { Animatable(0f) }
    var draggedItemIndex by remember { mutableIntStateOf(-1) }
    val interactionSource = remember { MutableInteractionSource() }

    val isDragged by interactionSource.collectIsDraggedAsState()
    LaunchedEffect(isDragged) {
        if (!isDragged) {
            draggingOffsetAnimatable.animateTo(0f)
            draggedItemIndex = -1
            onReorderDone()
        }
    }

    SongbookScaffoldLayout(
        topBar = @Composable {
            val rotation = remember { Animatable(0f) }
            LaunchedEffect(Unit) {
                rotation.animateTo(360f, infiniteRepeatable(tween(3000)))
            }

            TopBar(
                leftButton = null,
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
            items(
                count = songs.itemCount,
                key = songs.itemKey { it.id },
            ) { index ->
                val result = songs[index]
                if (result != null) {
                    SongCard(
                        modifier = Modifier.animateItem(),
                        song = result,
                        displayMode = DisplayMode.List,
                        onClick = { onLyricsClicked(result.id) },
                    )
                }
            }

            item { Spacer(Modifier.padding(LocalBottomBarPadding.current.padding.value)) }
        }
    }

    var isEditSetlistDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isConfirmDeleteDialogVisible by rememberSaveable { mutableStateOf(false) }

    SetlistOptionsBottomSheet(
        show = isBottomSheetVisible,
        state = state,
        onDismissRequest = { isBottomSheetVisible = false },
        onAction = {
            isBottomSheetVisible = false

            when (it) {
                Edit -> isEditSetlistDialogVisible = true
                Delete -> isConfirmDeleteDialogVisible = true
            }
        },
    )

    if (isEditSetlistDialogVisible) {
        EditSetlistDialog(
            name = state.setlist.name,
            onConfirmClicked = {
                onNameChanged(it)
                isEditSetlistDialogVisible = false
            },
            onDismissRequest = { isEditSetlistDialogVisible = false },
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
