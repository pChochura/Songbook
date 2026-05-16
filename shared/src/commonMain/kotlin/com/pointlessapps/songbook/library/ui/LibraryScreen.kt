package com.pointlessapps.songbook.library.ui

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.pointlessapps.songbook.BottomBarPadding.Companion.bottomBarHeight
import com.pointlessapps.songbook.LocalNavigator
import com.pointlessapps.songbook.core.setlist.model.Setlist
import com.pointlessapps.songbook.library.DisplayMode.Grid
import com.pointlessapps.songbook.library.LibraryEvent
import com.pointlessapps.songbook.library.LibraryViewModel
import com.pointlessapps.songbook.library.SortBy
import com.pointlessapps.songbook.library.ui.components.AddSetlistCard
import com.pointlessapps.songbook.library.ui.components.AddSongCard
import com.pointlessapps.songbook.library.ui.components.LibraryOptionsBottomSheet
import com.pointlessapps.songbook.library.ui.components.LibraryOptionsBottomSheetAction.DisplayMode
import com.pointlessapps.songbook.library.ui.components.LibraryOptionsBottomSheetAction.Settings
import com.pointlessapps.songbook.library.ui.components.SetlistCard
import com.pointlessapps.songbook.library.ui.components.ShowMoreButton
import com.pointlessapps.songbook.library.ui.components.SongCard
import com.pointlessapps.songbook.library.ui.dialogs.AddSetlistDialog
import com.pointlessapps.songbook.library.ui.dialogs.DisplayModeDialog
import com.pointlessapps.songbook.library.ui.dialogs.SortByDialog
import com.pointlessapps.songbook.shared.ui.Res
import com.pointlessapps.songbook.shared.ui.common_app_name
import com.pointlessapps.songbook.shared.ui.library_setlists_section_title
import com.pointlessapps.songbook.shared.ui.library_songs_found
import com.pointlessapps.songbook.shared.ui.library_songs_section_title
import com.pointlessapps.songbook.shared.ui.library_sort_by
import com.pointlessapps.songbook.shared.ui.library_sort_by_artist
import com.pointlessapps.songbook.shared.ui.library_sort_by_date_added
import com.pointlessapps.songbook.shared.ui.library_sort_by_title
import com.pointlessapps.songbook.shared.ui.library_starting_with
import com.pointlessapps.songbook.ui.MenuTopBarButton
import com.pointlessapps.songbook.ui.TopBar
import com.pointlessapps.songbook.ui.components.SongOptionsBottomSheetHandler
import com.pointlessapps.songbook.ui.components.SongbookChip
import com.pointlessapps.songbook.ui.components.SongbookLoader
import com.pointlessapps.songbook.ui.components.SongbookScaffoldLayout
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.defaultSongbookChipStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.IconAscending
import com.pointlessapps.songbook.ui.theme.IconClose
import com.pointlessapps.songbook.ui.theme.IconDescending
import com.pointlessapps.songbook.ui.theme.spacing
import com.pointlessapps.songbook.utils.SongOptionsBottomSheetEvent.NavigateToImportSong
import com.pointlessapps.songbook.utils.SyncingTopBarButton
import com.pointlessapps.songbook.utils.add
import com.pointlessapps.songbook.utils.collectWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LibraryScreen(
    viewModel: LibraryViewModel,
) {
    val navigator = LocalNavigator.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val songs = viewModel.songs.collectAsLazyPagingItems()
    var isBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
    var isSongOptionsBottomSheetVisible by rememberSaveable { mutableStateOf(false) }

    viewModel.events.collectWithLifecycle { event ->
        when (event) {
            is LibraryEvent.NavigateToSettings -> navigator.navigateToSettings()
            is LibraryEvent.NavigateToIntroduction -> navigator.navigateToIntroduction()
            is LibraryEvent.NavigateToImportSong -> navigator.navigateToImportSong()
            is LibraryEvent.NavigateToLyrics -> navigator.navigateToLyrics()
            is LibraryEvent.NavigateToSetlist -> navigator.navigateToSetlist(event.id)
        }
    }

    viewModel.songEvents.collectWithLifecycle {
        when (it) {
            is NavigateToImportSong -> navigator.navigateToImportSong(
                id = it.songId,
                title = it.title,
                artist = it.artist,
                lyrics = it.lyrics,
            )
        }
    }

    SongbookScaffoldLayout(
        topBar = @Composable {
            TopBar(
                leftButton = @Composable { SyncingTopBarButton(state.syncStatus) },
                rightButton = @Composable { MenuTopBarButton { isBottomSheetVisible = true } },
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
            contentPadding = paddingValues.add(
                vertical = MaterialTheme.spacing.huge,
                horizontal = MaterialTheme.spacing.large,
            ),
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
                    modifier = Modifier.animateItem(),
                    sortBy = state.sortBy,
                    onSortBySelected = viewModel::onSortBySelected,
                    numberOfSongs = songs.itemCount,
                    initialFilterLetter = state.initialFilterLetter,
                    onClearInitialFilterLetterClicked = viewModel::onClearInitialFilterLetterClicked,
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
                        enableSharedElementTransitions = true,
                        onClicked = { viewModel.onLyricsClicked(result.id) },
                        onLongClicked = {
                            viewModel.onSongLongClicked(
                                songId = result.id,
                                title = result.title,
                                artist = result.artist,
                                lyrics = result.lyrics,
                            )
                            isSongOptionsBottomSheetVisible = true
                        },
                    )
                }
            }

            item(
                key = "add_song_button",
                span = { GridItemSpan(maxLineSpan) },
            ) {
                AddSongCard(
                    modifier = Modifier.animateItem(),
                    onClick = viewModel::onImportSongClicked,
                )
            }

            item { Spacer(Modifier.bottomBarHeight()) }
        }
    }

    SongOptionsBottomSheetHandler(
        show = isSongOptionsBottomSheetVisible,
        delegate = viewModel,
        onDismissRequest = { isSongOptionsBottomSheetVisible = false },
    )

    var isDisplayModeDialogVisible by rememberSaveable { mutableStateOf(false) }

    LibraryOptionsBottomSheet(
        show = isBottomSheetVisible,
        state = state,
        onDismissRequest = { isBottomSheetVisible = false },
        onAction = {
            isBottomSheetVisible = false

            when (it) {
                DisplayMode -> isDisplayModeDialogVisible = true
                Settings -> viewModel.onSettingsClicked()
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

    SongbookLoader(state.isLoading)
}

@Composable
private fun LazyGridItemScope.SetlistsRow(
    setlists: ImmutableList<Setlist>,
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
    sortBy: SortBy,
    onSortBySelected: (SortBy) -> Unit,
    initialFilterLetter: String?,
    onClearInitialFilterLetterClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isSortByDialogVisible by rememberSaveable { mutableStateOf(false) }

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

        AnimatedContent(
            modifier = Modifier
                .weight(1f)
                .wrapContentWidth(Alignment.End),
            targetState = initialFilterLetter,
        ) { letter ->
            if (letter == null) {
                SongbookChip(
                    label = stringResource(
                        resource = Res.string.library_sort_by,
                        stringResource(
                            when (sortBy.field) {
                                SortBy.Field.Title -> Res.string.library_sort_by_title
                                SortBy.Field.Artist -> Res.string.library_sort_by_artist
                                SortBy.Field.DateAdded -> Res.string.library_sort_by_date_added
                            },
                        ),
                    ),
                    isSelected = true,
                    onClick = { isSortByDialogVisible = true },
                    chipStyle = defaultSongbookChipStyle().copy(
                        icon = if (sortBy.ascending) IconAscending else IconDescending,
                        iconAlignment = Alignment.End,
                        selectedContainerColor = Color.Transparent,
                        selectedOutlineColor = Color.Transparent,
                        selectedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        iconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    ),
                )
            } else {
                SongbookChip(
                    label = stringResource(Res.string.library_starting_with, letter),
                    isSelected = true,
                    onClick = onClearInitialFilterLetterClicked,
                    chipStyle = defaultSongbookChipStyle().copy(
                        icon = IconClose,
                        iconAlignment = Alignment.End,
                    ),
                )
            }
        }
    }

    if (isSortByDialogVisible) {
        SortByDialog(
            sortBy = sortBy,
            onSortBySelected = {
                isSortByDialogVisible = false
                onSortBySelected(it)
            },
            onDismissRequest = { isSortByDialogVisible = false },
        )
    }
}
