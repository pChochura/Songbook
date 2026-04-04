package com.pointlessapps.songbook.library.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import com.pointlessapps.songbook.LocalBottomBarPadding
import com.pointlessapps.songbook.LocalNavigator
import com.pointlessapps.songbook.Route
import com.pointlessapps.songbook.core.model.SyncStatus
import com.pointlessapps.songbook.library.LibraryEvent
import com.pointlessapps.songbook.library.LibraryViewModel
import com.pointlessapps.songbook.library.ui.components.AddSetlistCard
import com.pointlessapps.songbook.library.ui.components.AddSongCard
import com.pointlessapps.songbook.library.ui.components.SetlistCard
import com.pointlessapps.songbook.library.ui.components.SongCard
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.common_app_name
import com.pointlessapps.songbook.shared.common_syncing
import com.pointlessapps.songbook.shared.library_setlists_section_title
import com.pointlessapps.songbook.shared.library_songs_found
import com.pointlessapps.songbook.shared.library_songs_section_title
import com.pointlessapps.songbook.shared.library_sort_by_date
import com.pointlessapps.songbook.ui.TopBar
import com.pointlessapps.songbook.ui.TopBarButton
import com.pointlessapps.songbook.ui.components.SongbookChip
import com.pointlessapps.songbook.ui.components.SongbookLoader
import com.pointlessapps.songbook.ui.components.SongbookScaffoldLayout
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.IconSync
import com.pointlessapps.songbook.ui.theme.IconSyncFailed
import com.pointlessapps.songbook.ui.theme.spacing
import com.pointlessapps.songbook.utils.add
import com.pointlessapps.songbook.utils.collectWithLifecycle
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LibraryScreen(
    viewModel: LibraryViewModel,
) {
    val navigator = LocalNavigator.current
    val focusRequester = remember { FocusRequester() }

    viewModel.events.collectWithLifecycle { event ->
        when (event) {
            is LibraryEvent.NavigateTo -> when (val route = event.route) {
                is Route.Lyrics -> navigator.navigateToLyrics(route.songId)
                is Route.ImportSong -> navigator.navigateToImportSong()
                else -> Unit
            }

            LibraryEvent.FocusSearch -> focusRequester.requestFocus()
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
                rightButton = TopBarButton(
                    enabled = false,
                    icon = if (viewModel.state.syncStatus == SyncStatus.LOCAL) IconSync else IconSyncFailed,
                    tooltip = Res.string.common_syncing,
                    onClick = {},
                    modifier = Modifier.graphicsLayer { rotationZ = rotation.value },
                ).takeIf { viewModel.state.syncStatus == SyncStatus.LOCAL },
                title = Res.string.common_app_name,
            )
        },
    ) { paddingValues ->
        val horizontalPadding = MaterialTheme.spacing.huge
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 120.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = paddingValues.add(
                vertical = MaterialTheme.spacing.huge,
                horizontal = MaterialTheme.spacing.huge,
            ).add(bottom = LocalBottomBarPadding.current.padding.value),
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
                        onClick = viewModel::onAddSetlistClicked,
                    )

                    viewModel.state.setlists.forEach { setlist ->
                        SetlistCard(
                            modifier = Modifier.fillMaxHeight(),
                            setlist = setlist,
                            onClick = {},
                        )
                    }
                }
            }

            item(key = "all_songs_header", span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier
                        .animateItem()
                        .fillMaxWidth(),
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
                        label = stringResource(
                            Res.string.library_songs_found,
                            viewModel.state.songs.size,
                        ),
                        isSelected = false,
                        onClick = {},
                    )

                    SongbookText(
                        modifier = Modifier
                            .weight(1f)
                            .wrapContentWidth(Alignment.End),
                        text = stringResource(Res.string.library_sort_by_date),
                        textStyle = defaultSongbookTextStyle().copy(
                            textColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            typography = MaterialTheme.typography.labelMedium,
                        ),
                    )
                }
            }

            items(viewModel.state.songs, key = { it.id }) { song ->
                SongCard(
                    modifier = Modifier.animateItem(),
                    song = song,
                    onClick = { viewModel.onLyricsClicked(song.id) },
                )
            }

            item(key = "add_song_button") {
                AddSongCard(
                    modifier = Modifier.animateItem(),
                    onClick = viewModel::onImportSongClicked,
                )
            }
        }
    }

    SongbookLoader(viewModel.state.isLoading)
}
