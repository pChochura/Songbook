package com.pointlessapps.songbook.lyrics.ui

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.pointlessapps.songbook.core.domain.models.ParsedLine
import com.pointlessapps.songbook.lyrics.LyricsViewModel
import com.pointlessapps.songbook.shared.generated.resources.Res
import com.pointlessapps.songbook.shared.generated.resources.lyrics_capture_ocr_button
import com.pointlessapps.songbook.shared.generated.resources.lyrics_no_song_message
import com.pointlessapps.songbook.shared.generated.resources.lyrics_section_label
import com.pointlessapps.songbook.ui.components.ChordSelectionPopup
import com.pointlessapps.songbook.ui.components.LyricFlowHeader
import com.pointlessapps.songbook.ui.components.LyricsLine
import com.pointlessapps.songbook.ui.components.LyricsSection
import com.pointlessapps.songbook.ui.components.SongControlBar
import com.pointlessapps.songbook.ui.components.SongHeader
import com.pointlessapps.songbook.ui.components.SongStatusBar
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

@OptIn(ExperimentalOCRApi::class)
@Composable
internal fun LyricsScreen(
    viewModel: LyricsViewModel,
) {
    val state = viewModel.state

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Scaffold(
            topBar = {
                Column {
                    LyricFlowHeader()
                    SongControlBar(
                        transposition = state.transposition,
                        onTransposeUp = viewModel::transposeUp,
                        onTransposeDown = viewModel::transposeDown,
                        onReset = viewModel::resetTransposition,
                    )
                }
            },
            bottomBar = {
                SongStatusBar(
                    onFullscreenClicked = { viewModel.setOcrActive(true) },
                )
            },
            containerColor = MaterialTheme.colorScheme.surface,
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = 800.dp)
                        .padding(horizontal = MaterialTheme.spacing.huge),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                    contentPadding = paddingValues,
                ) {
                    if (state.parsedSections != null) {
                        item {
                            SongHeader(
                                title = state.title,
                                artist = state.artist,
                            )
                        }

                        state.parsedSections.forEachIndexed { sectionIndex, section ->
                            item {
                                LyricsSection(label = stringResource(Res.string.lyrics_section_label)) {
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
                                                                state.transposition,
                                                            ),
                                                        )
                                                    },
                                                    onCursorFinalized = { index, offset ->
                                                        viewModel.onCursorFinalized(
                                                            sectionIndex,
                                                            lineIndex,
                                                            index,
                                                            offset,
                                                        )
                                                    },
                                                    onChordClicked = { marker, offset ->
                                                        viewModel.onChordClicked(
                                                            sectionIndex,
                                                            lineIndex,
                                                            marker,
                                                            offset,
                                                        )
                                                    },
                                                    onChordMoved = { marker, newCharIndex ->
                                                        viewModel.onChordMoved(
                                                            sectionIndex,
                                                            lineIndex,
                                                            marker,
                                                            newCharIndex,
                                                        )
                                                    },
                                                )

                                                state.popupState?.let { popupState ->
                                                    if (popupState.sectionIndex == sectionIndex && popupState.lineIndex == lineIndex) {
                                                        ChordSelectionPopup(
                                                            offset = IntOffset(
                                                                popupState.offset.x.toInt(),
                                                                popupState.offset.y.toInt(),
                                                            ),
                                                            selectedChord = popupState.editingMarker?.chord,
                                                            onChordSelected = viewModel::onChordSelected,
                                                            onDismissRequest = viewModel::dismissPopup,
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
                                        text = stringResource(Res.string.lyrics_no_song_message),
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.outline,
                                    )
                                    Button(onClick = { viewModel.setOcrActive(true) }) {
                                        Text(stringResource(Res.string.lyrics_capture_ocr_button))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
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
                        val sections =
                            ((result.metadata?.get("gemini_structured_data") as? Map<String, Any>)
                                ?.get("text_content") as? JsonArray)?.mapNotNull { element ->
                                element.jsonObject["text"]?.jsonPrimitive?.content?.split("\n")
                                    ?.map { ParsedLine(it) }
                            }
                        viewModel.onOcrCompleted(sections)
                    },
                    onError = {
                        viewModel.setOcrActive(false)
                    },
                    onCancel = {
                        viewModel.setOcrActive(false)
                    },
                    enableCrop = true,
                ),
            )
        }
    }
}
