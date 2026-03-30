package com.pointlessapps.songbook.lyrics.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.pointlessapps.songbook.LocalNavigator
import com.pointlessapps.songbook.core.song.model.Section
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
import com.pointlessapps.songbook.ui.components.defaultSongbookChipStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.IconArrowLeft
import com.pointlessapps.songbook.ui.theme.IconMoveHandle
import com.pointlessapps.songbook.ui.theme.spacing
import com.pointlessapps.songbook.utils.add
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
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(horizontal = MaterialTheme.spacing.huge)
                    .widthIn(max = MAX_WIDTH)
                    .fillMaxSize(),
                contentPadding = paddingValues.add(
                    vertical = MaterialTheme.spacing.huge,
                ),
                verticalArrangement = Arrangement.spacedBy(
                    space = MaterialTheme.spacing.small,
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

                item { Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall)) }

                state.sections.forEach { lyricsSection(it, state.fontScale) }
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

private fun LazyListScope.lyricsSection(section: Section, fontScale: Float) {
    if (section.name.isNotEmpty()) {
        item {
            SongbookText(
                text = section.name,
                textStyle = defaultSongbookTextStyle().copy(
                    textColor = MaterialTheme.colorScheme.primary,
                    typography = MaterialTheme.typography.labelSmall,
                ),
            )
        }
    }

    items(section.lines) { line ->
        Column {
            var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

            if (line.chords.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    textLayoutResult?.let { layout ->
                        line.chords.forEach { chord ->
                            val horizontalPosition = layout.getHorizontalPosition(
                                offset = chord.position,
                                usePrimaryDirection = true,
                            )

                            SongbookChip(
                                modifier = Modifier.offset {
                                    IntOffset(horizontalPosition.toInt(), 0)
                                },
                                label = chord.value,
                                isSelected = false,
                                onClick = {
                                    // TODO add chord explanation dialog
                                },
                                chipStyle = defaultSongbookChipStyle().copy(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    labelColor = MaterialTheme.colorScheme.onPrimary,
                                    outlineColor = Color.Transparent,
                                ),
                            )
                        }
                    }
                }
            }

            SongbookText(
                text = line.line,
                onTextLayout = { textLayoutResult = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = defaultSongbookTextStyle().copy(
                    textColor = MaterialTheme.colorScheme.onSurface,
                    typography = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize * fontScale,
                    ),
                ),
            )
        }
    }
}
//
//@Composable
//private fun SideBySideLyricsLine(line: ParsedLine) {
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
//        verticalAlignment = Alignment.CenterVertically,
//    ) {
//        SongbookText(
//            modifier = Modifier.weight(1f),
//            text = line.text,
//            textStyle = defaultSongbookTextStyle().copy(
//                typography = MaterialTheme.typography.bodyLarge,
//            ),
//        )
//
//        Row(
//            modifier = Modifier.widthIn(min = 120.dp),
//            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
//        ) {
//            line.chords.forEach { marker ->
//                SongbookChip(
//                    label = marker.chord.value,
//                    isSelected = false,
//                    onClick = {},
//                )
//            }
//        }
//    }
//}
//
//@Composable
//private fun TextOnlyLyricsLine(line: ParsedLine) {
//    SongbookText(
//        text = line.text,
//        textStyle = defaultSongbookTextStyle().copy(
//            typography = MaterialTheme.typography.bodyLarge,
//        ),
//    )
//}

@Composable
private fun SongHeader(title: String, artist: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(
            space = MaterialTheme.spacing.small,
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

private val MAX_WIDTH = 800.dp
