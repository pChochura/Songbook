package com.pointlessapps.songbook.library.ui

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

private data object CurrentInfo : NavigationEventInfo()

@OptIn(ExperimentalOCRApi::class)
@Composable
internal fun LibraryScreen(
    viewModel: LibraryViewModel,
) {
    val state = viewModel.state
    val navigator = LocalNavigator.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LibraryEvent.NavigateTo -> navigator.navigateToLyrics((event.route as Route.Lyrics).songId)
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
                                text = "All Songs",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "${state.songs.size} Found",
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
                            text = "Sort by: Date Added",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                }

                items(state.songs) { song ->
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
private fun LibraryHeader(totalSongs: Int, totalArtists: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "YOUR PERSONAL SONGBOOK",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Song Library",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Access $totalSongs carefully transcribed songs with dynamic chord display options.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline,
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)) {
            StatCard(value = totalSongs.toString(), label = "SONGS")
            StatCard(value = totalArtists.toString(), label = "ARTISTS")
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
private fun FilterSection(label: String, filters: List<String>, selectedFilter: String?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.width(60.dp),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
            filters.forEach { filter ->
                val isSelected = filter == selectedFilter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        )
                        .clickable { }
                        .padding(horizontal = MaterialTheme.spacing.medium, vertical = 4.dp),
                ) {
                    Text(
                        text = filter,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = song.key ?: "C Major",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                Text(
                    text = song.duration ?: "0:00",
                    style = MaterialTheme.typography.labelSmall,
                )
                Text(
                    text = "${song.bpm ?: 0} BPM",
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
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.outline,
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
            Text(
                text = "Add New Song",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Import text, PDF, or ChordPro",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}
