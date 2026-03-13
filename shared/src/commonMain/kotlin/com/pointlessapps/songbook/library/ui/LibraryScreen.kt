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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pointlessapps.songbook.LocalNavigator
import com.pointlessapps.songbook.Route
import com.pointlessapps.songbook.data.SongEntity
import com.pointlessapps.songbook.library.LibraryEvent
import com.pointlessapps.songbook.library.LibraryViewModel
import com.pointlessapps.songbook.ui.components.LyricFlowHeader
import com.pointlessapps.songbook.ui.components.LyricFlowNavigationRail
import com.pointlessapps.songbook.ui.components.NavigationDestination
import com.pointlessapps.songbook.ui.theme.spacing
import io.github.ismoy.imagepickerkmp.features.ocr.annotations.ExperimentalOCRApi
import io.github.ismoy.imagepickerkmp.features.ocr.data.providers.CloudOCRProvider
import io.github.ismoy.imagepickerkmp.features.ocr.model.ImagePickerOCRConfig
import io.github.ismoy.imagepickerkmp.features.ocr.model.ScanMode
import io.github.ismoy.imagepickerkmp.features.ocr.presentation.ImagePickerLauncherOCR
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

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

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LyricFlowNavigationRail(
            selectedDestination = state.selectedDestination,
            onDestinationSelected = { destination ->
                viewModel.onDestinationSelected(destination)
                if (destination == NavigationDestination.NowPlaying) {
                    navigator.navigateToLyrics()
                }
            },
        )

        Scaffold(
            topBar = { LyricFlowHeader() },
            containerColor = MaterialTheme.colorScheme.background,
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = MaterialTheme.spacing.huge),
            ) {
                LibraryHeader(state.totalSongs, state.totalArtists)

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.huge))

                Text(
                    text = "QUICK FILTERS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                FilterSection(
                    label = "Genre:",
                    filters = listOf("All", "Rock", "Worship", "Jazz", "Blues", "Pop", "Country"),
                    selectedFilter = "All",
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                FilterSection(
                    label = "Key:",
                    filters = listOf("C Major", "G Major", "D Major", "A Major", "E Major", "F Major", "Bb Major"),
                    selectedFilter = null,
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.huge))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "All Songs",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))
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

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(bottom = MaterialTheme.spacing.huge),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
                ) {
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
                        val rawText = ((result.metadata?.get("gemini_structured_data") as? Map<String, Any>)
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
private fun ImportSongDialog(
    initialOcrText: String?,
    onDismiss: () -> Unit,
    onOcrRequested: () -> Unit,
    onManualConfirmed: (title: String, artist: String, lyricsText: String) -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    // OCR tab state
    var ocrText by remember(initialOcrText) { mutableStateOf(initialOcrText) }
    var ocrTitle by remember { mutableStateOf("Untitled Song") }
    var ocrArtist by remember { mutableStateOf("Unknown Artist") }

    // Manual tab state
    var manualTitle by remember { mutableStateOf("") }
    var manualArtist by remember { mutableStateOf("") }
    var manualLyrics by remember { mutableStateOf("") }

    val showConfirm = selectedTab == 1 || ocrText != null
    val confirmEnabled = (selectedTab == 0 && ocrText != null && ocrTitle.isNotBlank()) ||
            (selectedTab == 1 && manualTitle.isNotBlank())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Song") },
        text = {
            Column {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Scan with Camera") },
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Type Manually") },
                    )
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

                when (selectedTab) {
                    0 -> {
                        if (ocrText == null) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Button(onClick = onOcrRequested) {
                                    Text("Start Scan")
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)) {
                                OutlinedTextField(
                                    value = ocrTitle,
                                    onValueChange = { ocrTitle = it },
                                    label = { Text("Title") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                )
                                OutlinedTextField(
                                    value = ocrArtist,
                                    onValueChange = { ocrArtist = it },
                                    label = { Text("Artist") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                )
                                OutlinedTextField(
                                    value = ocrText!!,
                                    onValueChange = { ocrText = it },
                                    label = { Text("Lyrics") },
                                    modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp),
                                    maxLines = 10,
                                )
                            }
                        }
                    }
                    1 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)) {
                            OutlinedTextField(
                                value = manualTitle,
                                onValueChange = { manualTitle = it },
                                label = { Text("Title") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                            )
                            OutlinedTextField(
                                value = manualArtist,
                                onValueChange = { manualArtist = it },
                                label = { Text("Artist") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                            )
                            OutlinedTextField(
                                value = manualLyrics,
                                onValueChange = { manualLyrics = it },
                                label = { Text("Lyrics") },
                                modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp),
                                maxLines = 10,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (showConfirm) {
                Button(
                    onClick = {
                        if (selectedTab == 0) {
                            onManualConfirmed(ocrTitle, ocrArtist, ocrText ?: "")
                        } else {
                            onManualConfirmed(manualTitle, manualArtist, manualLyrics)
                        }
                    },
                    enabled = confirmEnabled,
                ) {
                    Text("Add Song")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
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
