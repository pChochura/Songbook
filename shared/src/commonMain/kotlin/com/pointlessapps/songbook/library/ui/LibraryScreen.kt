package com.pointlessapps.songbook.library.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.pointlessapps.songbook.LocalNavigator
import com.pointlessapps.songbook.Route
import com.pointlessapps.songbook.data.SongEntity
import com.pointlessapps.songbook.library.LibraryEvent
import com.pointlessapps.songbook.library.LibraryViewModel
import com.pointlessapps.songbook.shared.generated.resources.Res
import com.pointlessapps.songbook.shared.generated.resources.library_add_song_subtitle
import com.pointlessapps.songbook.shared.generated.resources.library_add_song_title
import com.pointlessapps.songbook.shared.generated.resources.library_header_description
import com.pointlessapps.songbook.shared.generated.resources.library_header_tagline
import com.pointlessapps.songbook.shared.generated.resources.library_header_title
import com.pointlessapps.songbook.shared.generated.resources.library_search_clear_filter
import com.pointlessapps.songbook.shared.generated.resources.library_search_filter_letter
import com.pointlessapps.songbook.shared.generated.resources.library_search_placeholder
import com.pointlessapps.songbook.shared.generated.resources.library_song_bpm
import com.pointlessapps.songbook.shared.generated.resources.library_song_duration_default
import com.pointlessapps.songbook.shared.generated.resources.library_song_key_default
import com.pointlessapps.songbook.shared.generated.resources.library_songs_found
import com.pointlessapps.songbook.shared.generated.resources.library_songs_section_title
import com.pointlessapps.songbook.shared.generated.resources.library_sort_by_date
import com.pointlessapps.songbook.shared.generated.resources.library_stat_artists
import com.pointlessapps.songbook.shared.generated.resources.library_stat_songs
import com.pointlessapps.songbook.ui.components.LyricFlowHeader
import com.pointlessapps.songbook.ui.theme.spacing
import io.github.ismoy.imagepickerkmp.features.ocr.annotations.ExperimentalOCRApi
import io.github.ismoy.imagepickerkmp.features.ocr.data.providers.CloudOCRProvider
import io.github.ismoy.imagepickerkmp.features.ocr.model.ImagePickerOCRConfig
import io.github.ismoy.imagepickerkmp.features.ocr.model.ScanMode
import io.github.ismoy.imagepickerkmp.features.ocr.presentation.ImagePickerLauncherOCR
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.compose.resources.stringResource

private data object CurrentInfo : NavigationEventInfo()

@OptIn(ExperimentalOCRApi::class)
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
                is LibraryEvent.NavigateTo -> navigator.navigateToLyrics((event.route as Route.Lyrics).songId)
                LibraryEvent.FocusSearch -> focusRequester.requestFocus()
            }
        }
    }

    val navigationEventState = rememberNavigationEventState(
        currentInfo = CurrentInfo,
        backInfo = listOf(NavigationEventInfo.None),
    )
    NavigationBackHandler(
        state = navigationEventState,
        isBackEnabled = state.isOcrActive,
        onBackCancelled = {},
        onBackCompleted = { viewModel.onOcrScanned("") },
    )

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Scaffold(
            topBar = { LyricFlowHeader() },
            bottomBar = {
                SearchBar(
                    query = state.searchQuery,
                    filterLetter = state.filterLetter,
                    onQueryChange = viewModel::setSearchQuery,
                    onClearFilter = { viewModel.setFilterLetter(null) },
                    focusRequester = focusRequester,
                )
            },
            containerColor = MaterialTheme.colorScheme.background,
        ) { paddingValues ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = MaterialTheme.spacing.huge),
                contentPadding = PaddingValues(
                    top = MaterialTheme.spacing.huge,
                    bottom = MaterialTheme.spacing.huge,
                ),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LibraryHeader(state.totalSongs, state.totalArtists)
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                        ) {
                            Text(
                                text = stringResource(Res.string.library_songs_section_title),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = stringResource(
                                    Res.string.library_songs_found,
                                    state.filteredSongs.size,
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        RoundedCornerShape(4.dp),
                                    )
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                            )
                        }

                        Text(
                            text = stringResource(Res.string.library_sort_by_date),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                }

                items(state.filteredSongs) { song ->
                    SongCard(
                        song = song,
                        onClick = { navigator.navigateToLyrics(song.id) },
                    )
                }

                item {
                    AddSongCard(onClick = { viewModel.showImportDialog() })
                }
            }
        }
    }

    if (state.showImportDialog) {
        ImportSongDialog(
            initialOcrText = state.ocrScannedText,
            onDismiss = viewModel::hideImportDialog,
            onOcrRequested = {
                viewModel.setOcrActive(true)
                viewModel.hideImportDialog()
            },
            onManualConfirmed = viewModel::onManualInputConfirmed,
        )
    }

    if (state.isOcrActive) {
        Box(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            ImagePickerLauncherOCR(
                config = ImagePickerOCRConfig(
                    scanMode = ScanMode.Cloud(
                        provider = CloudOCRProvider.Gemini(
                            apiKey = "AIzaSyBM2JGn74cCsqf2aotqWmiyn55A56AigVg",
                            model = "gemini-3.1-flash-lite",
                        ),
                    ),
                    onOCRCompleted = { result ->
                        @Suppress("UNCHECKED_CAST")
                        val rawText =
                            ((result.metadata?.get("gemini_structured_data") as? Map<String, Any>)
                                ?.get("text_content") as? JsonArray)?.mapNotNull { element ->
                                element.jsonObject["text"]?.jsonPrimitive?.content
                            }?.joinToString("\n") ?: ""
                        viewModel.onOcrScanned(rawText)
                    },
                    onError = { viewModel.setOcrActive(false) },
                    onCancel = { viewModel.setOcrActive(false) },
                    enableCrop = true,
                ),
            )
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    filterLetter: Char?,
    onQueryChange: (String) -> Unit,
    onClearFilter: () -> Unit,
    focusRequester: FocusRequester,
) {
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        cursorColor = MaterialTheme.colorScheme.primary,
        focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
        unfocusedLeadingIconColor = MaterialTheme.colorScheme.outline,
        focusedTrailingIconColor = MaterialTheme.colorScheme.outline,
        unfocusedTrailingIconColor = MaterialTheme.colorScheme.outline,
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
            .background(MaterialTheme.colorScheme.surface)
            .padding(
                horizontal = MaterialTheme.spacing.huge,
                vertical = MaterialTheme.spacing.medium,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester)
                .animateContentSize(),
            singleLine = true,
            placeholder = {
                Text(
                    text = stringResource(Res.string.library_search_placeholder),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            },
            trailingIcon = if (query.isNotEmpty()) {
                {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(Res.string.library_search_clear_filter),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            } else null,
            shape = RoundedCornerShape(12.dp),
            colors = textFieldColors,
        )

        AnimatedContent(
            targetState = filterLetter,
            contentKey = { it != null },
            transitionSpec = { fadeIn() togetherWith fadeOut() },
        ) { filterLetter ->
            if (filterLetter == null) return@AnimatedContent

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .height(OutlinedTextFieldDefaults.MinHeight)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(onClick = onClearFilter)
                    .padding(
                        horizontal = MaterialTheme.spacing.medium,
                        vertical = MaterialTheme.spacing.small,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(
                            Res.string.library_search_filter_letter,
                            filterLetter,
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(Res.string.library_search_clear_filter),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun LibraryHeader(totalSongs: Int, totalArtists: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.library_header_tagline),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(Res.string.library_header_title),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(Res.string.library_header_description, totalSongs),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline,
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)) {
            StatCard(
                value = totalSongs.toString(),
                label = stringResource(Res.string.library_stat_songs),
            )
            StatCard(
                value = totalArtists.toString(),
                label = stringResource(Res.string.library_stat_artists),
            )
        }
    }
}

@Composable
private fun StatCard(value: String, label: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.size(80.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@Composable
private fun SongCard(song: SongEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(MaterialTheme.spacing.large)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(24.dp),
                )
                Icon(
                    imageVector = Icons.Default.StarBorder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

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

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = song.key ?: stringResource(Res.string.library_song_key_default),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                Text(
                    text = song.duration
                        ?: stringResource(Res.string.library_song_duration_default),
                    style = MaterialTheme.typography.labelSmall,
                )
                Text(
                    text = stringResource(Res.string.library_song_bpm, song.bpm ?: 0),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(4.dp),
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun AddSongCard(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
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
            )
            Text(
                text = stringResource(Res.string.library_add_song_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}
