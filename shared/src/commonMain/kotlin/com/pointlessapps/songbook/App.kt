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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    var parsedSections by rememberSaveable { mutableStateOf<List<List<String>>?>(null) }

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

                            parsedSections?.forEach { section ->
                                item {
                                    LyricsSection(label = "Lyrics") {
                                        Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)) {
                                            section.forEach { line ->
                                                LyricsLine(
                                                    text = line,
                                                    chords = emptyList(),
                                                )
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
