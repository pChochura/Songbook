package com.pointlessapps.songbook.lyrics.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.pointlessapps.songbook.LocalNavigator
import com.pointlessapps.songbook.core.domain.models.ParsedLine
import com.pointlessapps.songbook.lyrics.LyricsMode
import com.pointlessapps.songbook.lyrics.LyricsViewModel
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.common_back
import com.pointlessapps.songbook.shared.common_menu
import com.pointlessapps.songbook.shared.lyrics_section_label
import com.pointlessapps.songbook.ui.OptionsBottomSheet
import com.pointlessapps.songbook.ui.OptionsBottomSheetItem
import com.pointlessapps.songbook.ui.OptionsBottomSheetTitleHeader
import com.pointlessapps.songbook.ui.TopBar
import com.pointlessapps.songbook.ui.TopBarButton
import com.pointlessapps.songbook.ui.components.SongbookChip
import com.pointlessapps.songbook.ui.components.SongbookScaffoldLayout
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.IconArrowLeft
import com.pointlessapps.songbook.ui.theme.IconMoveHandle
import com.pointlessapps.songbook.ui.theme.spacing
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LyricsScreen(
    viewModel: LyricsViewModel,
) {
    val state = viewModel.state
    val navigator = LocalNavigator.current
    var isBottomSheetVisible by remember { mutableStateOf(false) }

    SongbookScaffoldLayout(
        topBar = @Composable {
            TopBar(
                leftButton = TopBarButton(
                    icon = IconArrowLeft,
                    tooltip = Res.string.common_back,
                    onClick = { navigator.navigateBack() },
                ),
                rightButton = TopBarButton(
                    icon = IconMoveHandle,
                    tooltip = Res.string.common_menu,
                    onClick = { isBottomSheetVisible = true },
                ),
                title = Res.string.lyrics_section_label,
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 800.dp)
                .padding(horizontal = MaterialTheme.spacing.huge),
            contentPadding = PaddingValues(
                top = MaterialTheme.spacing.huge + paddingValues.calculateTopPadding(),
                bottom = MaterialTheme.spacing.huge + paddingValues.calculateBottomPadding(),
            ),
            verticalArrangement = Arrangement.spacedBy(
                space = MaterialTheme.spacing.medium,
                alignment = Alignment.Top,
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item(key = "header") {
                SongHeader(
                    title = state.title,
                    artist = state.artist,
                )
            }

            items(state.parsedSections.indices.toList()) { sectionIndex ->
                LyricsSection(
                    title = "Section ${sectionIndex + 1}",
                    lines = state.parsedSections[sectionIndex],
                    mode = state.mode,
                )
            }

            item {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
            }
        }
    }

    if (isBottomSheetVisible) {
        OptionsBottomSheet(
            state = rememberModalBottomSheetState(),
            onDismissRequest = { isBottomSheetVisible = false },
            header = { OptionsBottomSheetTitleHeader(stringResource(Res.string.common_menu)) },
            items = listOf(
                OptionsBottomSheetItem.new(
                    label = Res.string.common_menu,
                    onClick = {

                    },
                ),
            ),
        )
    }
}

@Composable
private fun LyricsSection(
    title: String,
    lines: List<ParsedLine>,
    mode: LyricsMode,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 800.dp),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
    ) {
        SongbookText(
            text = title,
            textStyle = defaultSongbookTextStyle().copy(
                textColor = MaterialTheme.colorScheme.primary,
                typography = MaterialTheme.typography.labelLarge,
            ),
        )

        lines.forEach { line ->
            LyricsLine(line = line, mode = mode)
        }
    }
}

@Composable
private fun LyricsLine(
    line: ParsedLine,
    mode: LyricsMode,
) {
    when (mode) {
        LyricsMode.Inline -> InlineLyricsLine(line)
        LyricsMode.SideBySide -> SideBySideLyricsLine(line)
        LyricsMode.TextOnly -> TextOnlyLyricsLine(line)
    }
}

@Composable
private fun InlineLyricsLine(line: ParsedLine) {
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Column {
            if (line.chords.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp), // Height for the chords row
                ) {
                    textLayoutResult?.let { layout ->
                        line.chords.forEach { marker ->
                            val offset = layout.getHorizontalPosition(
                                marker.offset,
                                usePrimaryDirection = true,
                            )
                            SongbookChip(
                                modifier = Modifier.offset {
                                    IntOffset(offset.toInt(), 0)
                                },
                                label = marker.chord.value,
                                isSelected = false,
                                onClick = {},
                            )
                        }
                    }
                }
            }

            SongbookText(
                text = line.text,
                textStyle = defaultSongbookTextStyle().copy(
                    typography = MaterialTheme.typography.bodyLarge,
                ),
                onTextLayout = { textLayoutResult = it },
            )
        }
    }
}

@Composable
private fun SideBySideLyricsLine(line: ParsedLine) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SongbookText(
            modifier = Modifier.weight(1f),
            text = line.text,
            textStyle = defaultSongbookTextStyle().copy(
                typography = MaterialTheme.typography.bodyLarge,
            ),
        )

        Row(
            modifier = Modifier.widthIn(min = 120.dp),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
        ) {
            line.chords.forEach { marker ->
                SongbookChip(
                    label = marker.chord.value,
                    isSelected = false,
                    onClick = {},
                )
            }
        }
    }
}

@Composable
private fun TextOnlyLyricsLine(line: ParsedLine) {
    SongbookText(
        text = line.text,
        textStyle = defaultSongbookTextStyle().copy(
            typography = MaterialTheme.typography.bodyLarge,
        ),
    )
}

@Composable
private fun SongHeader(title: String, artist: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(
            space = MaterialTheme.spacing.medium,
            alignment = Alignment.CenterVertically,
        ),
    ) {
        SongbookText(
            text = title,
            textStyle = defaultSongbookTextStyle().copy(
                textColor = MaterialTheme.colorScheme.onSurface,
                typography = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                ),
            ),
        )
        SongbookText(
            text = artist,
            textStyle = defaultSongbookTextStyle().copy(
                textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                typography = MaterialTheme.typography.labelMedium,
            ),
        )

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}
