package com.pointlessapps.songbook.lyrics.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.pointlessapps.songbook.lyrics.LyricsViewModel
import com.pointlessapps.songbook.shared.generated.resources.Res
import com.pointlessapps.songbook.shared.generated.resources.lyrics_section_label
import com.pointlessapps.songbook.ui.components.ChordSelectionPopup
import com.pointlessapps.songbook.ui.components.LyricFlowHeader
import com.pointlessapps.songbook.ui.components.LyricsLine
import com.pointlessapps.songbook.ui.components.LyricsSection
import com.pointlessapps.songbook.ui.components.SongControlBar
import com.pointlessapps.songbook.ui.components.SongHeader
import com.pointlessapps.songbook.ui.components.SongStatusBar
import com.pointlessapps.songbook.ui.theme.spacing
import org.jetbrains.compose.resources.stringResource

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
                }
            }
        }
    }
}
