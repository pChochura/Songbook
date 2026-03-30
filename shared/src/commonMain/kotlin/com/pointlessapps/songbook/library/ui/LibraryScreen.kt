package com.pointlessapps.songbook.library.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import com.pointlessapps.songbook.LocalBottomBarPadding
import com.pointlessapps.songbook.LocalNavigator
import com.pointlessapps.songbook.Route
import com.pointlessapps.songbook.library.LibraryEvent
import com.pointlessapps.songbook.library.LibraryViewModel
import com.pointlessapps.songbook.library.ui.components.AddSongCard
import com.pointlessapps.songbook.library.ui.components.SetlistCard
import com.pointlessapps.songbook.library.ui.components.SongCard
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.common_app_name
import com.pointlessapps.songbook.shared.library_setlists_section_title
import com.pointlessapps.songbook.shared.library_songs_found
import com.pointlessapps.songbook.shared.library_songs_section_title
import com.pointlessapps.songbook.shared.library_sort_by_date
import com.pointlessapps.songbook.ui.TopBar
import com.pointlessapps.songbook.ui.components.SongbookChip
import com.pointlessapps.songbook.ui.components.SongbookScaffoldLayout
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.spacing
import com.pointlessapps.songbook.utils.add
import com.pointlessapps.songbook.utils.collectWithLifecycle
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LibraryScreen(
    viewModel: LibraryViewModel,
) {
    val state = viewModel.state
    val navigator = LocalNavigator.current
    val focusRequester = remember { FocusRequester() }

    viewModel.events.collectWithLifecycle { event ->
        when (event) {
            is LibraryEvent.NavigateTo -> when (val route = event.route) {
                is Route.Lyrics -> navigator.navigateToLyrics(route.songId)
                Route.ImportSong -> navigator.navigateToImportSong()
                else -> Unit
            }

            LibraryEvent.FocusSearch -> focusRequester.requestFocus()
        }
    }

    SongbookScaffoldLayout(
        topBar = @Composable {
            TopBar(
                leftButton = null,
                rightButton = null,
                title = Res.string.common_app_name,
            )
        },
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 120.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = MaterialTheme.spacing.huge),
            contentPadding = paddingValues.add(
                vertical = MaterialTheme.spacing.huge,
            ).add(bottom = LocalBottomBarPadding.current.padding.value),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
        ) {
            item(key = "setlists_header", span = { GridItemSpan(maxLineSpan) }) {
                SongbookText(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(Res.string.library_setlists_section_title),
                    textStyle = defaultSongbookTextStyle().copy(
                        textColor = MaterialTheme.colorScheme.onSurface,
                        typography = MaterialTheme.typography.titleLarge,
                    ),
                )
            }

            items(state.setlists) { setlist ->
                SetlistCard(
                    setlist = setlist,
                    onClick = {},
                )
            }

            item(key = "all_songs_header", span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                            state.songs.size,
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

            items(state.songs, key = { it.id }) { song ->
                SongCard(
                    song = song,
                    onClick = { viewModel.onLyricsRequested(song.id) },
                )
            }

            item(key = "add_song_button") {
                AddSongCard(onClick = viewModel::onImportSongRequested)
            }
        }
    }
}
