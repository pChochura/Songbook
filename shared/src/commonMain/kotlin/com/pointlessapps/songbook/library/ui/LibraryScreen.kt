package com.pointlessapps.songbook.library.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pointlessapps.songbook.LocalNavigator
import com.pointlessapps.songbook.Route
import com.pointlessapps.songbook.core.song.model.Song
import com.pointlessapps.songbook.library.LibraryEvent
import com.pointlessapps.songbook.library.LibraryViewModel
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.common_app_name
import com.pointlessapps.songbook.shared.library_add_song_subtitle
import com.pointlessapps.songbook.shared.library_add_song_title
import com.pointlessapps.songbook.shared.library_add_to_favourites
import com.pointlessapps.songbook.shared.library_header_title
import com.pointlessapps.songbook.shared.library_search_placeholder
import com.pointlessapps.songbook.shared.library_songs_found
import com.pointlessapps.songbook.shared.library_sort_by_date
import com.pointlessapps.songbook.ui.TopBar
import com.pointlessapps.songbook.ui.components.SongbookCard
import com.pointlessapps.songbook.ui.components.SongbookChip
import com.pointlessapps.songbook.ui.components.SongbookIcon
import com.pointlessapps.songbook.ui.components.SongbookIconButton
import com.pointlessapps.songbook.ui.components.SongbookScaffoldLayout
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.SongbookTextField
import com.pointlessapps.songbook.ui.components.dashedSongbookCardStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookChipStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookIconButtonStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookIconStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextFieldStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.IconClose
import com.pointlessapps.songbook.ui.theme.IconFavouriteEmpty
import com.pointlessapps.songbook.ui.theme.IconNote
import com.pointlessapps.songbook.ui.theme.IconPlus
import com.pointlessapps.songbook.ui.theme.spacing
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LibraryScreen(
    viewModel: LibraryViewModel,
) {
    val state = viewModel.state
    val navigator = LocalNavigator.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LibraryEvent.NavigateTo -> when (val route = event.route) {
                    is Route.Lyrics -> navigator.navigateToLyrics(route.songId)
                    Route.ImportSong -> navigator.navigateToImportSong()
                    else -> Unit
                }

                LibraryEvent.FocusSearch -> focusRequester.requestFocus()
            }
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
        fab = @Composable {
            BottomBar(
                query = state.searchQuery,
                filterLetter = state.filterLetter,
                onQueryChanged = viewModel::setSearchQuery,
                onFilterLetterRemoved = { viewModel.setFilterLetter(null) },
            )
        },
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 120.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = MaterialTheme.spacing.large),
            contentPadding = PaddingValues(
                top = MaterialTheme.spacing.huge,
                bottom = MaterialTheme.spacing.huge,
            ),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
        ) {
            item(key = "header", span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SongbookText(
                        text = stringResource(Res.string.library_header_title),
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

@Composable
private fun BottomBar(
    query: String,
    filterLetter: Char?,
    onQueryChanged: (String) -> Unit,
    onFilterLetterRemoved: () -> Unit,
) {
    Row(
        modifier = Modifier
            .navigationBarsPadding()
            .imePadding()
            .padding(
                horizontal = MaterialTheme.spacing.huge,
                vertical = MaterialTheme.spacing.medium,
            )
            .padding(bottom = MaterialTheme.spacing.large)
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SongbookTextField(
            modifier = Modifier
                .weight(1f)
                .clip(MaterialTheme.shapes.medium)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = MaterialTheme.shapes.medium,
                )
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(
                    horizontal = MaterialTheme.spacing.large,
                    vertical = MaterialTheme.spacing.medium,
                ),
            value = query,
            onValueChange = onQueryChanged,
            textFieldStyle = defaultSongbookTextFieldStyle().copy(
                placeholder = stringResource(Res.string.library_search_placeholder),
                placeholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textColor = MaterialTheme.colorScheme.onSurface,
            ),
        )

        AnimatedContent(
            targetState = filterLetter,
            transitionSpec = { fadeIn() togetherWith fadeOut() using null },
        ) { filterLetter ->
            if (filterLetter == null) return@AnimatedContent

            SongbookChip(
                modifier = Modifier.fillMaxHeight().widthIn(min = 64.dp),
                label = filterLetter.toString(),
                isSelected = true,
                onClick = onFilterLetterRemoved,
                chipStyle = defaultSongbookChipStyle().copy(
                    selectedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    selectedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedOutlineColor = MaterialTheme.colorScheme.outlineVariant,
                    iconRes = IconClose,
                    iconAlignment = Alignment.End,
                ),
            )
        }
    }
}

@Composable
private fun SongCard(song: Song, onClick: () -> Unit) {
    SongbookCard(
        onClick = onClick,
        onLongClick = { },
    ) {
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SongbookIcon(
                    iconRes = IconNote,
                    iconStyle = defaultSongbookIconStyle().copy(
                        tint = MaterialTheme.colorScheme.primary,
                    ),
                )
                SongbookIconButton(
                    iconRes = IconFavouriteEmpty,
                    tooltipLabel = Res.string.library_add_to_favourites,
                    onClick = {},
                    iconButtonStyle = defaultSongbookIconButtonStyle().copy(
                        outlineColor = Color.Transparent,
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                )
            }

            Column {
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
        }
    }
}

@Composable
private fun AddSongCard(onClick: () -> Unit) {
    SongbookCard(
        onClick = onClick,
        onLongClick = { },
        cardStyle = dashedSongbookCardStyle(),
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.large),
            verticalArrangement = Arrangement.spacedBy(
                space = MaterialTheme.spacing.medium,
                alignment = Alignment.CenterVertically,
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SongbookIcon(
                iconRes = IconPlus,
                iconStyle = defaultSongbookIconStyle().copy(
                    tint = MaterialTheme.colorScheme.primary,
                ),
            )
            SongbookText(
                text = stringResource(Res.string.library_add_song_title),
                textStyle = defaultSongbookTextStyle().copy(
                    textColor = MaterialTheme.colorScheme.onSurface,
                    typography = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                ),
            )
            SongbookText(
                text = stringResource(Res.string.library_add_song_subtitle),
                textStyle = defaultSongbookTextStyle().copy(
                    textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    typography = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                ),
            )
        }
    }
}
