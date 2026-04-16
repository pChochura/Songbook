package com.pointlessapps.songbook.library.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.pointlessapps.songbook.LocalBottomBarPadding
import com.pointlessapps.songbook.LocalNavigator
import com.pointlessapps.songbook.core.setlist.model.Setlist
import com.pointlessapps.songbook.library.DisplayMode.Grid
import com.pointlessapps.songbook.library.LibraryEvent
import com.pointlessapps.songbook.library.LibraryViewModel
import com.pointlessapps.songbook.library.ui.components.AddSetlistCard
import com.pointlessapps.songbook.library.ui.components.AddSongCard
import com.pointlessapps.songbook.library.ui.components.LibraryOptionsBottomSheet
import com.pointlessapps.songbook.library.ui.components.LibraryOptionsBottomSheetAction.DisplayMode
import com.pointlessapps.songbook.library.ui.components.LibraryOptionsBottomSheetAction.Login
import com.pointlessapps.songbook.library.ui.components.LibraryOptionsBottomSheetAction.Logout
import com.pointlessapps.songbook.library.ui.components.LibraryOptionsBottomSheetAction.Sync
import com.pointlessapps.songbook.library.ui.components.SetlistCard
import com.pointlessapps.songbook.library.ui.components.ShowMoreButton
import com.pointlessapps.songbook.library.ui.components.SongCard
import com.pointlessapps.songbook.library.ui.components.dialogs.AddSetlistDialog
import com.pointlessapps.songbook.library.ui.components.dialogs.DisplayModeDialog
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.common_app_name
import com.pointlessapps.songbook.shared.library_setlists_section_title
import com.pointlessapps.songbook.shared.library_songs_found
import com.pointlessapps.songbook.shared.library_songs_section_title
import com.pointlessapps.songbook.shared.library_sort_by_date
import com.pointlessapps.songbook.ui.TopBar
import com.pointlessapps.songbook.ui.TopBarButton
import com.pointlessapps.songbook.ui.components.SongbookChip
import com.pointlessapps.songbook.ui.components.SongbookScaffoldLayout
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.spacing
import com.pointlessapps.songbook.utils.add
import com.pointlessapps.songbook.utils.collectWithLifecycle
import com.pointlessapps.songbook.utils.syncingTopBarButton
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LibraryScreen(
    viewModel: LibraryViewModel,
) {
    val navigator = LocalNavigator.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val songs = viewModel.songs.collectAsLazyPagingItems()
    var isBottomSheetVisible by rememberSaveable { mutableStateOf(false) }

    viewModel.events.collectWithLifecycle { event ->
        when (event) {
            is LibraryEvent.NavigateToIntroduction -> navigator.navigateToIntroduction()
            is LibraryEvent.NavigateToImportSong -> navigator.navigateToImportSong()
            is LibraryEvent.NavigateToLyrics -> navigator.navigateToLyrics(event.id)
            is LibraryEvent.NavigateToSetlist -> navigator.navigateToSetlist(event.id)
        }
    }

    SongbookScaffoldLayout(
        topBar = @Composable {
            TopBar(
                leftButton = syncingTopBarButton(state.syncStatus),
                rightButton = TopBarButton.menu(
                    onClick = { isBottomSheetVisible = true },
                ),
                title = stringResource(Res.string.common_app_name),
            )
        },
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = if (state.displayMode == Grid) {
                GridCells.Adaptive(minSize = 120.dp)
            } else {
                GridCells.Fixed(1)
            },
            modifier = Modifier.fillMaxSize(),
            contentPadding = paddingValues.add(MaterialTheme.spacing.huge),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
        ) {
            item(key = "setlists_header", span = { GridItemSpan(maxLineSpan) }) {
                SongbookText(
                    modifier = Modifier.animateItem().fillMaxWidth(),
                    text = stringResource(Res.string.library_setlists_section_title),
                    textStyle = defaultSongbookTextStyle().copy(
                        textColor = MaterialTheme.colorScheme.onSurface,
                        typography = MaterialTheme.typography.titleLarge,
                    ),
                )
            }

            item(key = "setlists", span = { GridItemSpan(maxLineSpan) }) {
                SetlistsRow(
                    setlists = state.setlists,
                    onSetlistClicked = viewModel::onSetlistClicked,
                    onAddSetlistClicked = viewModel::onAddSetlistClicked,
                )
            }

            item(key = "all_songs_header", span = { GridItemSpan(maxLineSpan) }) {
                AllSongsHeader(
                    numberOfSongs = songs.itemCount,
                    modifier = Modifier.animateItem(),
                )
            }

            items(
                count = songs.itemCount,
                key = songs.itemKey { it.id },
            ) { index ->
                val result = songs[index]
                if (result != null) {
                    SongCard(
                        modifier = Modifier.animateItem(),
                        song = result,
                        displayMode = state.displayMode,
                        onClick = { viewModel.onLyricsClicked(result.id) },
                    )
                }
            }

            item(key = "add_song_button") {
                AddSongCard(
                    modifier = Modifier.animateItem(),
                    onClick = viewModel::onImportSongClicked,
                )
            }

            item { Spacer(Modifier.padding(LocalBottomBarPadding.current.padding.value)) }
        }
    }

    var isDisplayModeDialogVisible by rememberSaveable { mutableStateOf(false) }

    LibraryOptionsBottomSheet(
        show = isBottomSheetVisible,
        state = state,
        onDismissRequest = { isBottomSheetVisible = false },
        onAction = {
            isBottomSheetVisible = false

            when (it) {
                Login -> viewModel.loginClicked()
                Logout -> viewModel.logoutClicked()
                DisplayMode -> isDisplayModeDialogVisible = true
                Sync -> viewModel.onSyncClicked()
            }
        },
    )

    if (isDisplayModeDialogVisible) {
        DisplayModeDialog(
            mode = state.displayMode,
            onModeSelected = {
                viewModel.onDisplayModeChanged(it)
                isDisplayModeDialogVisible = false
            },
            onDismissRequest = { isDisplayModeDialogVisible = false },
        )
    }
}

@Composable
private fun LazyGridItemScope.SetlistsRow(
    setlists: List<Setlist>,
    onSetlistClicked: (String) -> Unit,
    onAddSetlistClicked: (String) -> Unit,
) {
    var isAddSetlistDialogVisible by remember { mutableStateOf(false) }
    val horizontalPadding = MaterialTheme.spacing.huge

    Row(
        modifier = Modifier
            .animateItem()
            .height(IntrinsicSize.Max)
            .layout { measurable, constraints ->
                val paddingPx = horizontalPadding.roundToPx()
                val expandedWidth = constraints.maxWidth + 2 * paddingPx
                val placeable = measurable.measure(
                    constraints.copy(
                        maxWidth = expandedWidth,
                        minWidth = expandedWidth,
                    ),
                )
                layout(placeable.width - 2 * paddingPx, placeable.height) {
                    placeable.place(-paddingPx, 0)
                }
            }
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = horizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
    ) {
        AddSetlistCard(
            modifier = Modifier.fillMaxHeight(),
            onClick = { isAddSetlistDialogVisible = true },
        )

        setlists.forEach { setlist ->
            SetlistCard(
                modifier = Modifier.fillMaxHeight(),
                setlist = setlist,
                onClick = { onSetlistClicked(setlist.id) },
            )
        }

        ShowMoreButton(
            modifier = Modifier.fillMaxHeight(),
            onClick = {},
        )
    }

    if (isAddSetlistDialogVisible) {
        AddSetlistDialog(
            onConfirmClicked = {
                isAddSetlistDialogVisible = false
                onAddSetlistClicked(it)
            },
            onDismissRequest = { isAddSetlistDialogVisible = false },
        )
    }
}

@Composable
private fun AllSongsHeader(
    numberOfSongs: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SongbookText(
            text = stringResource(Res.string.library_songs_section_title),
            textStyle = defaultSongbookTextStyle().copy(
                textColor = MaterialTheme.colorScheme.onSurface,
                typography = MaterialTheme.typography.titleLarge,
            ),
        )

        SongbookChip(
            label = stringResource(Res.string.library_songs_found, numberOfSongs),
            isSelected = false,
            onClick = {},
        )

        SongbookText(
            modifier = Modifier
                .weight(1f)
                .wrapContentWidth(Alignment.End),
            text = stringResource(Res.string.library_sort_by_date),
            textStyle = defaultSongbookTextStyle().copy(
                textAlign = TextAlign.End,
                textColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                typography = MaterialTheme.typography.labelMedium,
            ),
        )
    }
}
