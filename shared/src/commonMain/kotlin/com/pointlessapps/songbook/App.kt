package com.pointlessapps.songbook

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.pointlessapps.songbook.ui.components.*
import com.pointlessapps.songbook.ui.theme.LyricFlowTheme
import com.pointlessapps.songbook.ui.theme.spacing
import org.jetbrains.compose.resources.stringResource
import com.pointlessapps.songbook.shared.generated.resources.*

@Composable
fun App() {
    var selectedDestination by remember { mutableStateOf(NavigationDestination.NowPlaying) }
    
    LyricFlowTheme {
        Row(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            LyricFlowNavigationRail(
                selectedDestination = selectedDestination,
                onDestinationSelected = { selectedDestination = it }
            )
            
            Scaffold(
                topBar = {
                    Column {
                        LyricFlowHeader()
                        SongControlBar()
                    }
                },
                bottomBar = {
                    SongStatusBar(tempo = 120, capo = 0, isLiveMode = true)
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { },
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary,
                        shape = CircleShape
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = stringResource(Res.string.common_play)
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface
            ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = MaterialTheme.spacing.huge),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
                ) {
                    item {
                        SongHeader(
                            title = "Fly Me to the Moon",
                            artist = "Frank Sinatra",
                            tags = listOf("JAZZ STANDARD", "# Am")
                        )
                    }
                    
                    item {
                        LyricsSection(label = "Verse 1") {
                            Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)) {
                                LyricsLine(
                                    text = "Fly me to the moon, and let me play among the stars",
                                    chords = listOf(
                                        ChordMarker("Am7", 0),
                                        ChordMarker("Dm7", 15),
                                        ChordMarker("G7", 30),
                                        ChordMarker("Cmaj7", 45)
                                    )
                                )
                                LyricsLine(
                                    text = "Let me see what spring is like on Jupiter and Mars",
                                    chords = listOf(
                                        ChordMarker("Fmaj7", 0),
                                        ChordMarker("Bm7b5", 15),
                                        ChordMarker("E7", 30),
                                        ChordMarker("Am7", 45)
                                    )
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
                                        ChordMarker("Dm7", 0),
                                        ChordMarker("G7", 10),
                                        ChordMarker("Cmaj7", 20)
                                    )
                                )
                                LyricsLine(
                                    text = "In other words, baby, kiss me",
                                    chords = listOf(
                                        ChordMarker("Dm7", 0),
                                        ChordMarker("G7", 10),
                                        ChordMarker("Cmaj7", 20)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
