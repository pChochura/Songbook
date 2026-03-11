package com.pointlessapps.songbook

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pointlessapps.songbook.shared.generated.resources.Res
import com.pointlessapps.songbook.shared.generated.resources.common_play
import com.pointlessapps.songbook.ui.components.ChordMarker
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
import org.jetbrains.compose.resources.stringResource

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
                bottomBar = {
                    SongStatusBar(tempo = 120, capo = 0, isLiveMode = true)
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { },
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary,
                        shape = CircleShape,
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = stringResource(Res.string.common_play),
                        )
                    }
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
                        item {
                            SongHeader(
                                title = "Fly Me to the Moon",
                                artist = "Frank Sinatra",
                                tags = listOf("JAZZ STANDARD", "# Am"),
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
                                            ChordMarker("Cmaj7", 45),
                                        ),
                                    )
                                    LyricsLine(
                                        text = "Let me see what spring is like on Jupiter and Mars",
                                        chords = listOf(
                                            ChordMarker("Fmaj7", 0),
                                            ChordMarker("Bm7b5", 15),
                                            ChordMarker("E7", 30),
                                            ChordMarker("Am7", 45),
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
                                            ChordMarker("Dm7", 0),
                                            ChordMarker("G7", 10),
                                            ChordMarker("Cmaj7", 20),
                                        ),
                                    )
                                    LyricsLine(
                                        text = "In other words, baby, kiss me",
                                        chords = listOf(
                                            ChordMarker("Dm7", 0),
                                            ChordMarker("G7", 10),
                                            ChordMarker("Cmaj7", 20),
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
