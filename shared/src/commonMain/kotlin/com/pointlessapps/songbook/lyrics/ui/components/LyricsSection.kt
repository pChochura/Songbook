package com.pointlessapps.songbook.lyrics.ui.components

import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import com.pointlessapps.songbook.core.song.model.Chord
import com.pointlessapps.songbook.core.song.model.Section
import com.pointlessapps.songbook.lyrics.DisplayMode
import com.pointlessapps.songbook.lyrics.WrapMode
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.spacing
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun LyricsSections(
    sections: ImmutableList<Section>,
    textScale: Int,
    keyOffset: Int,
    displayMode: DisplayMode,
    wrapMode: WrapMode,
    editable: Boolean,
    onChordClicked: (Int, Chord, Rect) -> Unit,
    onChordMoved: (Int, Chord, Int) -> Unit = { _, _, _ -> },
    onCursorPlaced: (Int, Int, Rect) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier,
    userScrollEnabled: Boolean = true,
    scrollState: ScrollState = rememberScrollState(),
    overscrollEffect: OverscrollEffect? = null,
) {
    val lineTextStyle = calculateLineTextStyle(textScale, wrapMode)
    val chordChipStyle = calculateChordChipStyle(textScale)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (overscrollEffect != null) Modifier.overscroll(overscrollEffect) else Modifier)
            .then(
                if (wrapMode == WrapMode.NoWrap || displayMode.shouldShowSideBySide) {
                    Modifier.horizontalScroll(
                        state = scrollState,
                        enabled = userScrollEnabled,
                    )
                } else Modifier,
            )
            .then(modifier),
        verticalArrangement = Arrangement.spacedBy(
            space = MaterialTheme.spacing.small,
            alignment = Alignment.Top,
        ),
    ) {
        sections.forEach { section ->
            SectionTitle(section.name)

            when {
                displayMode.shouldShowSideBySide -> SideBySideLyricsSection(
                    section = section,
                    keyOffset = keyOffset,
                    lineTextStyle = lineTextStyle,
                    chordChipStyle = chordChipStyle,
                    shouldShowInline = displayMode.shouldShowInline,
                    editable = editable,
                    onChordClicked = { chord, rect ->
                        onChordClicked(section.id, chord, rect)
                    },
                    onChordMoved = { chord, position ->
                        onChordMoved(section.id, chord, position)
                    },
                    onCursorFinalized = onCursorPlaced,
                )

                displayMode.shouldShowInline -> {
                    var currentLineOffset = 0
                    section.lines.forEach { line ->
                        val lineOffset = currentLineOffset
                        InlineLyricsLine(
                            line = line,
                            keyOffset = keyOffset,
                            lineTextStyle = lineTextStyle,
                            chordChipStyle = chordChipStyle,
                            editable = editable,
                            onChordClicked = { chord, rect ->
                                onChordClicked(section.id, chord, rect)
                            },
                            onChordMoved = { chord, position ->
                                onChordMoved(section.id, chord, position)
                            },
                            onCursorFinalized = { position, rect ->
                                onCursorPlaced(section.id, lineOffset + position, rect)
                            },
                        )
                        currentLineOffset += line.line.length + 1
                    }
                }

                else -> section.lines.forEach { line ->
                    TextOnlyLyricsLine(
                        line = line,
                        lineTextStyle = lineTextStyle,
                    )
                }
            }

            Spacer(Modifier.height(MaterialTheme.spacing.huge))
        }
    }
}

@Composable
private fun SectionTitle(name: String) {
    if (name.isEmpty()) return
    SongbookText(
        text = name,
        textStyle = defaultSongbookTextStyle().copy(
            textColor = MaterialTheme.colorScheme.primary,
            typography = MaterialTheme.typography.labelSmall,
        ),
    )
}
