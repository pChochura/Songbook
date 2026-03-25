package com.pointlessapps.songbook.library.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pointlessapps.songbook.LocalNavigator
import com.pointlessapps.songbook.Route
import com.pointlessapps.songbook.data.SongEntity
import com.pointlessapps.songbook.library.LibraryEvent
import com.pointlessapps.songbook.library.LibraryViewModel
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.common_app_name
import com.pointlessapps.songbook.shared.library_add_song_subtitle
import com.pointlessapps.songbook.shared.library_add_song_title
import com.pointlessapps.songbook.shared.library_header_title
import com.pointlessapps.songbook.shared.library_search_placeholder
import com.pointlessapps.songbook.shared.library_songs_found
import com.pointlessapps.songbook.shared.library_sort_by_date
import com.pointlessapps.songbook.ui.TopBar
import com.pointlessapps.songbook.ui.components.SongbookChip
import com.pointlessapps.songbook.ui.components.SongbookScaffoldLayout
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.SongbookTextField
import com.pointlessapps.songbook.ui.components.defaultSongbookChipStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextFieldStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.IconClose
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
                    value = state.searchQuery,
                    onValueChange = viewModel::setSearchQuery,
                    textFieldStyle = defaultSongbookTextFieldStyle().copy(
                        placeholder = stringResource(Res.string.library_search_placeholder),
                        placeholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textColor = MaterialTheme.colorScheme.onSurface,
                    ),
                )

                AnimatedContent(
                    targetState = state.filterLetter,
                    transitionSpec = { fadeIn() togetherWith fadeOut() using null },
                ) { filterLetter ->
                    if (filterLetter == null) return@AnimatedContent

                    SongbookChip(
                        modifier = Modifier.fillMaxHeight().widthIn(min = 64.dp),
                        label = filterLetter.toString(),
                        isSelected = true,
                        onClick = { viewModel.setFilterLetter(null) },
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
                            state.filteredSongs.size,
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

            items(state.filteredSongs, key = { it.id }) { song ->
                SongCard(
                    song = song,
                    onClick = { navigator.navigateToLyrics(song.id) },
                )
            }

            item(key = "add_song_button") {
                AddSongCard(onClick = viewModel::onImportSongRequested)
            }
        }
    }
}

@Composable
private fun LazyGridItemScope.SongCard(song: SongEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier.animateItem().fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.large),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(MaterialTheme.spacing.extraLarge),
                )
                Icon(
                    imageVector = Icons.Default.StarBorder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(MaterialTheme.spacing.extraLarge),
                )
            }

            Column {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}

@Composable
private fun LazyGridItemScope.AddSongCard(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .animateItem()
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.small,
            )
            .clickable(onClick = onClick)
            .padding(
                horizontal = MaterialTheme.spacing.large,
                vertical = MaterialTheme.spacing.medium,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.outline,
        )
        Text(
            text = stringResource(Res.string.library_add_song_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(Res.string.library_add_song_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center,
        )
    }
}
