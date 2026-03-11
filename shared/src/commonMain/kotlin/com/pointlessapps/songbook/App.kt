package com.pointlessapps.songbook

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.pointlessapps.songbook.model.Chord
import com.pointlessapps.songbook.ui.components.*
import com.pointlessapps.songbook.ui.theme.LyricFlowTheme
import com.pointlessapps.songbook.ui.theme.spacing

@Composable
fun App() {
    var selectedDestination by remember { mutableStateOf(NavigationDestination.NowPlaying) }

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
                        SongControlBar()
                    }
                },
                bottomBar = { SongStatusBar(tempo = 120) },
                containerColor = MaterialTheme.colorScheme.surface,
            ) { paddingValues ->
                var line1Chords by remember {
                    mutableStateOf(
                        listOf(
                            ChordMarker(Chord.Am7, 0),
                            ChordMarker(Chord.Dm7, 15),
                            ChordMarker(Chord.G7, 30),
                            ChordMarker(Chord.CMaj7, 45),
                        )
                    )
                }

                var popupState by remember { mutableStateOf<PopupState?>(null) }

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
                        item {
                            SongHeader(
                                title = "Fly Me to the Moon",
                                artist = "Frank Sinatra",
                            )
                        }

                        item {
                            LyricsSection(label = "Verse 1") {
                                Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)) {
                                    Box {
                                        LyricsLine(
                                            text = "Fly me to the moon, and let me play among the stars",
                                            chords = line1Chords,
                                            onCursorFinalized = { index, offset ->
                                                popupState = PopupState(index, offset)
                                            }
                                        )

                                        popupState?.let { state ->
                                            ChordSelectionPopup(
                                                offset = IntOffset(state.offset.x.toInt(), state.offset.y.toInt()),
                                                onChordSelected = { chord ->
                                                    line1Chords = (line1Chords + ChordMarker(chord, state.index)).sortedBy { it.offset }
                                                    popupState = null
                                                },
                                                onDismissRequest = { popupState = null }
                                            )
                                        }
                                    }

                                    LyricsLine(
                                        text = "Let me see what spring is like on Jupiter and Mars",
                                        chords = listOf(
                                            ChordMarker(Chord.FMaj7, 0),
                                            ChordMarker(Chord.Bm7b5, 15),
                                            ChordMarker(Chord.E7, 30),
                                            ChordMarker(Chord.Am7, 45),
                                        ),
                                    )
                                }
                            }
                        }

                        item {
                            LyricsSection(label = "Chorus") {
                                Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)) {
                                    LyricsLine(
                                        text = "In other words, hold my hand",
                                        chords = listOf(
                                            ChordMarker(Chord.Dm7, 0),
                                            ChordMarker(Chord.G7, 10),
                                            ChordMarker(Chord.CMaj7, 20),
                                        ),
                                    )
                                    LyricsLine(
                                        text = "In other words, baby, kiss me",
                                        chords = listOf(
                                            ChordMarker(Chord.Dm7, 0),
                                            ChordMarker(Chord.G7, 10),
                                            ChordMarker(Chord.CMaj7, 20),
                                        ),
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

private data class PopupState(val index: Int, val offset: Offset)
