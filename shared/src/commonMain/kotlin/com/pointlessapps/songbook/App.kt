package com.pointlessapps.songbook

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.pointlessapps.songbook.ui.components.ChordMarker
import com.pointlessapps.songbook.ui.components.ChordSelectionPopup
import com.pointlessapps.songbook.ui.components.LyricFlowHeader
import com.pointlessapps.songbook.ui.components.LyricFlowNavigationRail
import com.pointlessapps.songbook.ui.components.LyricsLine
import com.pointlessapps.songbook.ui.components.LyricsSection
import com.pointlessapps.songbook.ui.components.NavigationDestination
import com.pointlessapps.songbook.ui.components.SongControlBar
import com.pointlessapps.songbook.ui.components.SongHeader
import com.pointlessapps.songbook.ui.components.SongStatusBar
import com.pointlessapps.songbook.ui.theme.LyricFlowTheme
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
fun App() {
    var selectedDestination by rememberSaveable { mutableStateOf(NavigationDestination.NowPlaying) }
    var transposition by rememberSaveable { mutableStateOf(0) }
    var isOcrActive by rememberSaveable { mutableStateOf(false) }
    var parsedSections by remember { mutableStateOf<List<List<ParsedLine>>?>(null) }
    var popupState by remember { mutableStateOf<PopupState?>(null) }

    LyricFlowTheme {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            LyricFlowNavigationRail(
                selectedDestination = selectedDestination,
                onDestinationSelected = { selectedDestination = it },
            )

            Scaffold(
                topBar = {
                    Column {
                        LyricFlowHeader()
                        SongControlBar(
                            transposition = transposition,
                            onTransposeUp = { transposition++ },
                            onTransposeDown = { transposition-- },
                            onReset = { transposition = 0 },
                        )
                    }
                },
                bottomBar = {
                    SongStatusBar(
                        tempo = 120,
                        onFullscreenClicked = { isOcrActive = true },
                    )
                },
                containerColor = MaterialTheme.colorScheme.surface,
            ) { paddingValues ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxHeight()
                            .widthIn(max = 800.dp)
                            .padding(paddingValues)
                            .padding(horizontal = MaterialTheme.spacing.huge),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                    ) {
                        if (parsedSections != null) {
                            item {
                                SongHeader(
                                    title = "Parsed Song",
                                    artist = "Unknown Artist",
                                )
                            }

                            parsedSections?.forEachIndexed { sectionIndex, section ->
                                item {
                                    LyricsSection(label = "Lyrics") {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(
                                                MaterialTheme.spacing.medium,
                                            ),
                                        ) {
                                            section.forEachIndexed { lineIndex, line ->
                                                Box {
                                                    LyricsLine(
                                                        text = line.text,
                                                        chords = line.chords.map {
                                                            it.copy(
                                                                chord = it.chord.transpose(
                                                                    transposition,
                                                                ),
                                                            )
                                                        },
                                                        onCursorFinalized = { index, offset ->
                                                            popupState = PopupState(
                                                                sectionIndex = sectionIndex,
                                                                lineIndex = lineIndex,
                                                                charIndex = index,
                                                                offset = offset,
                                                            )
                                                        },
                                                        onChordClicked = { marker, offset ->
                                                            popupState = PopupState(
                                                                sectionIndex = sectionIndex,
                                                                lineIndex = lineIndex,
                                                                charIndex = marker.offset,
                                                                offset = offset,
                                                                editingMarker = marker,
                                                            )
                                                        },
                                                    )

                                                    popupState?.let { state ->
                                                        if (state.sectionIndex == sectionIndex && state.lineIndex == lineIndex) {
                                                            ChordSelectionPopup(
                                                                offset = IntOffset(
                                                                    state.offset.x.toInt(),
                                                                    state.offset.y.toInt(),
                                                                ),
                                                                selectedChord = state.editingMarker?.chord,
                                                                onChordSelected = { chord ->
                                                                    parsedSections =
                                                                        parsedSections?.mapIndexed { sIndex, sLines ->
                                                                            if (sIndex == sectionIndex) {
                                                                                sLines.mapIndexed { lIndex, lData ->
                                                                                    if (lIndex == lineIndex) {
                                                                                        if (state.editingMarker != null) {
                                                                                            // Update existing chord
                                                                                            lData.copy(
                                                                                                chords = lData.chords.map {
                                                                                                    if (it == state.editingMarker) it.copy(
                                                                                                        chord = chord,
                                                                                                    ) else it
                                                                                                },
                                                                                            )
                                                                                        } else {
                                                                                            // Add new chord
                                                                                            lData.copy(
                                                                                                chords = (lData.chords + ChordMarker(
                                                                                                    chord,
                                                                                                    state.charIndex,
                                                                                                )).sortedBy { it.offset },
                                                                                            )
                                                                                        }
                                                                                    } else lData
                                                                                }
                                                                            } else sLines
                                                                        }
                                                                    popupState = null
                                                                },
                                                                onDismissRequest = {
                                                                    popupState = null
                                                                },
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillParentMaxSize()
                                        .padding(MaterialTheme.spacing.huge),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                                    ) {
                                        Text(
                                            text = "No song captured yet",
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = MaterialTheme.colorScheme.outline,
                                        )
                                        Button(onClick = { isOcrActive = true }) {
                                            Text("Capture Song via OCR")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (isOcrActive) {
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
                        isOcrActive = false
                        val sections =
                            ((result.metadata?.get("gemini_structured_data") as? Map<String, Any>)
                                ?.get("text_content") as? JsonArray)?.mapNotNull { element ->
                                element.jsonObject["text"]?.jsonPrimitive?.content?.split("\n")
                                    ?.map { ParsedLine(it) }
                            }
                        parsedSections = sections
                        println("LOG!, $sections")
                    },
                    onError = {
                        isOcrActive = false
                    },
                    onCancel = {
                        isOcrActive = false
                    },
                    enableCrop = true,
                ),
            )
        }
    }
}

private data class ParsedLine(
    val text: String,
    val chords: List<ChordMarker> = emptyList(),
)

private data class PopupState(
    val sectionIndex: Int,
    val lineIndex: Int,
    val charIndex: Int,
    val offset: Offset,
    val editingMarker: ChordMarker? = null,
)
