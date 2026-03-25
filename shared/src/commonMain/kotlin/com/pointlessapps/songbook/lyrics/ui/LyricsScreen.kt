package com.pointlessapps.songbook.lyrics.ui

import androidx.compose.runtime.Composable
import com.pointlessapps.songbook.lyrics.LyricsViewModel

@Composable
internal fun LyricsScreen(
    viewModel: LyricsViewModel,
) {
    val state = viewModel.state

//    Row(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(MaterialTheme.colorScheme.background),
//    ) {
//        Scaffold(
//            topBar = {
//                Column {
//                    LyricFlowHeader()
//                    SongControlBar(
//                        transposition = state.transposition,
//                        onTransposeUp = viewModel::transposeUp,
//                        onTransposeDown = viewModel::transposeDown,
//                        onReset = viewModel::resetTransposition,
//                    )
//                }
//            },
//            bottomBar = {
//                SongStatusBar(
//                    onFullscreenClicked = { viewModel.setOcrActive(true) },
//                )
//            },
//            containerColor = MaterialTheme.colorScheme.surface,
//        ) { paddingValues ->
//            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
//                LazyColumn(
//                    modifier = Modifier
//                        .fillMaxHeight()
//                        .widthIn(max = 800.dp)
//                        .padding(horizontal = MaterialTheme.spacing.huge),
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
//                    contentPadding = paddingValues,
//                ) {
//                    item {
//                        SongHeader(
//                            title = state.title,
//                            artist = state.artist,
//                        )
//                    }
//
//                    state.parsedSections.forEachIndexed { sectionIndex, section ->
//                        item {
//                            LyricsSection(label = stringResource(Res.string.lyrics_section_label)) {
//                                Column(
//                                    verticalArrangement = Arrangement.spacedBy(
//                                        MaterialTheme.spacing.medium,
//                                    ),
//                                ) {
//                                    section.forEachIndexed { lineIndex, line ->
//                                        Box {
//                                            LyricsLine(
//                                                text = line.text,
//                                                chords = line.chords.map {
//                                                    it.copy(
//                                                        chord = it.chord.transpose(
//                                                            state.transposition,
//                                                        ),
//                                                    )
//                                                },
//                                                onCursorFinalized = { index, offset ->
//                                                    viewModel.onCursorFinalized(
//                                                        sectionIndex,
//                                                        lineIndex,
//                                                        index,
//                                                        offset,
//                                                    )
//                                                },
//                                                onChordClicked = { marker, offset ->
//                                                    viewModel.onChordClicked(
//                                                        sectionIndex,
//                                                        lineIndex,
//                                                        marker,
//                                                        offset,
//                                                    )
//                                                },
//                                                onChordMoved = { marker, newCharIndex ->
//                                                    viewModel.onChordMoved(
//                                                        sectionIndex,
//                                                        lineIndex,
//                                                        marker,
//                                                        newCharIndex,
//                                                    )
//                                                },
//                                            )
//
//                                            state.popupState?.let { popupState ->
//                                                if (popupState.sectionIndex == sectionIndex && popupState.lineIndex == lineIndex) {
//                                                    ChordSelectionPopup(
//                                                        offset = IntOffset(
//                                                            popupState.offset.x.toInt(),
//                                                            popupState.offset.y.toInt(),
//                                                        ),
//                                                        selectedChord = popupState.editingMarker?.chord,
//                                                        onChordSelected = viewModel::onChordSelected,
//                                                        onDismissRequest = viewModel::dismissPopup,
//                                                    )
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
}
