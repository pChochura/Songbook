package com.pointlessapps.songbook.lyrics.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.TextLayoutResult
import com.pointlessapps.songbook.core.song.ChordLibrary
import com.pointlessapps.songbook.core.song.model.Section
import com.pointlessapps.songbook.lyrics.LyricsMode
import com.pointlessapps.songbook.ui.components.SongbookChip
import com.pointlessapps.songbook.ui.components.SongbookChipStyle
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.SongbookTextStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookChipStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.spacing

@Composable
internal fun LyricsSections(
    sections: List<Section>,
    textScale: Int,
    mode: LyricsMode,
    keyOffset: Int,
    onChordClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val textScaleFloat = textScale / 100f

    val lineTextStyle = defaultSongbookTextStyle().copy(
        softWrap = false,
        textColor = MaterialTheme.colorScheme.onSurface,
        typography = MaterialTheme.typography.bodyLarge.copy(
            fontSize = MaterialTheme.typography.bodyLarge.fontSize * textScaleFloat,
        ),
    )

    val chordChipStyle = defaultSongbookChipStyle().copy(
        containerColor = MaterialTheme.colorScheme.primary,
        labelColor = MaterialTheme.colorScheme.onPrimary,
        outlineColor = Color.Transparent,
        labelTypography = MaterialTheme.typography.labelLarge.copy(
            fontSize = MaterialTheme.typography.labelLarge.fontSize * textScaleFloat,
        ),
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .then(modifier),
        verticalArrangement = Arrangement.spacedBy(
            space = MaterialTheme.spacing.small,
            alignment = Alignment.Top,
        ),
    ) {
        sections.forEach { section ->
            if (section.name.isNotEmpty()) {
                SongbookText(
                    text = section.name,
                    textStyle = defaultSongbookTextStyle().copy(
                        textColor = MaterialTheme.colorScheme.primary,
                        typography = MaterialTheme.typography.labelSmall,
                    ),
                )
            }

            when {
                mode.shouldShowSideBySide -> SideBySideLyricsSection(
                    section = section,
                    keyOffset = keyOffset,
                    lineTextStyle = lineTextStyle,
                    chordChipStyle = chordChipStyle,
                    shouldShowInline = mode.shouldShowInline,
                    onChordClicked = onChordClicked,
                )

                mode.shouldShowInline -> section.lines.forEach { line ->
                    InlineLyricsLine(
                        line = line,
                        keyOffset = keyOffset,
                        lineTextStyle = lineTextStyle,
                        chordChipStyle = chordChipStyle,
                        onChordClicked = onChordClicked,
                    )
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
private fun SideBySideLyricsSection(
    section: Section,
    keyOffset: Int,
    lineTextStyle: SongbookTextStyle,
    chordChipStyle: SongbookChipStyle,
    shouldShowInline: Boolean,
    onChordClicked: (String) -> Unit,
) {
    val lineSpacing = MaterialTheme.spacing.small
    val chordMargin = MaterialTheme.spacing.extraHuge
    val colSpacing = MaterialTheme.spacing.extraSmall

    val uniquePositions = section.lines
        .flatMap { it.chords }
        .map { it.position }
        .distinct()
        .sorted()
    val numCols = uniquePositions.size
    val measurablesPerLine = 1 + numCols

    Layout(
        modifier = Modifier.fillMaxWidth(),
        content = {
            section.lines.forEach { line ->
                when {
                    shouldShowInline -> InlineLyricsLine(
                        line = line,
                        keyOffset = keyOffset,
                        lineTextStyle = lineTextStyle,
                        chordChipStyle = chordChipStyle,
                        onChordClicked = onChordClicked,
                    )

                    else -> TextOnlyLyricsLine(
                        line = line,
                        lineTextStyle = lineTextStyle,
                    )
                }
                uniquePositions.forEach { pos ->
                    val chord = line.chords.find { it.position == pos }
                    if (chord != null) {
                        SongbookChip(
                            label = ChordLibrary.transpose(chord.value, keyOffset),
                            isSelected = false,
                            onClick = { onChordClicked(chord.value) },
                            chipStyle = chordChipStyle,
                        )
                    } else {
                        Spacer(Modifier)
                    }
                }
            }
        },
    ) { measurables, constraints ->
        val lineCount = section.lines.size
        val allPlaceables = measurables.mapIndexed { index, measurable ->
            if (index % measurablesPerLine == 0) {
                measurable.measure(constraints.copy(minWidth = 0))
            } else {
                measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
            }
        }

        val textPlaceables =
            allPlaceables.filterIndexed { index, _ -> index % measurablesPerLine == 0 }
        val maxTextWidth = textPlaceables.maxOfOrNull { it.width } ?: 0

        val colWidths = IntArray(numCols) { colIndex ->
            var maxWidth = 0
            for (lineIndex in 0 until lineCount) {
                val placeable = allPlaceables[lineIndex * measurablesPerLine + 1 + colIndex]
                maxWidth = maxOf(maxWidth, placeable.width)
            }
            maxWidth
        }

        val heights = List(lineCount) { lineIndex ->
            var maxHeight = textPlaceables[lineIndex].height
            for (colIndex in 0 until numCols) {
                maxHeight = maxOf(
                    maxHeight,
                    allPlaceables[lineIndex * measurablesPerLine + 1 + colIndex].height,
                )
            }
            maxHeight
        }

        val totalHeight =
            (heights.sum() + (lineCount - 1) * lineSpacing.roundToPx()).coerceAtLeast(0)

        val layoutWidth = if (constraints.hasBoundedWidth) {
            constraints.maxWidth
        } else {
            val totalChordsWidth =
                colWidths.sum() + (numCols - 1).coerceAtLeast(0) * colSpacing.roundToPx()
            (maxTextWidth + chordMargin.roundToPx() + totalChordsWidth).coerceAtLeast(constraints.minWidth)
        }

        layout(layoutWidth, totalHeight) {
            var currentY = 0
            for (lineIndex in 0 until lineCount) {
                val lineHeight = heights[lineIndex]
                val textPlaceable = textPlaceables[lineIndex]

                textPlaceable.placeRelative(0, currentY + (lineHeight - textPlaceable.height) / 2)

                var currentX = maxTextWidth + chordMargin.roundToPx()
                for (colIndex in 0 until numCols) {
                    val placeable = allPlaceables[lineIndex * measurablesPerLine + 1 + colIndex]
                    if (placeable.width > 0) {
                        placeable.placeRelative(
                            currentX,
                            currentY + (lineHeight - placeable.height) / 2,
                        )
                    }
                    currentX += colWidths[colIndex] + colSpacing.roundToPx()
                }

                currentY += lineHeight + lineSpacing.roundToPx()
            }
        }
    }
}

@Composable
private fun InlineLyricsLine(
    line: Section.Line,
    keyOffset: Int,
    lineTextStyle: SongbookTextStyle,
    chordChipStyle: SongbookChipStyle,
    onChordClicked: (String) -> Unit,
) {
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    Layout(
        modifier = Modifier.fillMaxWidth(),
        content = {
            SongbookText(
                text = line.line,
                onTextLayout = { textLayoutResult = it },
                textStyle = lineTextStyle,
            )
            line.chords.forEach { chord ->
                SongbookChip(
                    label = ChordLibrary.transpose(chord.value, keyOffset),
                    isSelected = false,
                    onClick = { onChordClicked(chord.value) },
                    chipStyle = chordChipStyle,
                )
            }
        },
    ) { measurables, constraints ->
        val textMeasurable = measurables[0]
        val chordMeasurables = measurables.drop(1)

        val textPlaceable = textMeasurable.measure(constraints)
        val chordPlaceables = chordMeasurables.map {
            it.measure(constraints.copy(minWidth = 0, minHeight = 0))
        }

        val maxChordHeight = chordPlaceables.maxOfOrNull { it.height } ?: 0
        val totalHeight = textPlaceable.height + maxChordHeight

        layout(textPlaceable.width, totalHeight) {
            textPlaceable.placeRelative(0, maxChordHeight)
            textLayoutResult?.let { layout ->
                chordPlaceables.forEachIndexed { index, placeable ->
                    val chord = line.chords[index]
                    val lineIndex = layout.getLineForOffset(chord.position)
                    val horizontalPosition = layout.getHorizontalPosition(
                        offset = chord.position,
                        usePrimaryDirection = true,
                    )
                    val verticalPosition = layout.getLineTop(lineIndex)

                    placeable.placeRelative(
                        x = horizontalPosition.toInt(),
                        y = maxChordHeight + verticalPosition.toInt() - placeable.height,
                    )
                }
            }
        }
    }
}

@Composable
private fun TextOnlyLyricsLine(
    line: Section.Line,
    lineTextStyle: SongbookTextStyle,
) {
    SongbookText(
        text = line.line,
        modifier = Modifier.fillMaxWidth(),
        textStyle = lineTextStyle,
    )
}
