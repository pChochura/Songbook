package com.pointlessapps.songbook.lyrics.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.pointlessapps.songbook.core.song.ChordLibrary
import com.pointlessapps.songbook.core.song.model.Section
import com.pointlessapps.songbook.lyrics.DisplayMode
import com.pointlessapps.songbook.lyrics.WrapMode
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
    keyOffset: Int,
    displayMode: DisplayMode,
    wrapMode: WrapMode,
    onChordClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val textScaleFloat = textScale / 100f
    val fontSize = MaterialTheme.typography.bodyLarge.fontSize * textScaleFloat

    val lineTextStyle = defaultSongbookTextStyle().copy(
        softWrap = wrapMode == WrapMode.Wrap,
        textColor = MaterialTheme.colorScheme.onSurface,
        typography = MaterialTheme.typography.bodyLarge.copy(
            fontSize = fontSize,
            lineHeight = fontSize * 1.5f,
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

    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (wrapMode == WrapMode.NoWrap || displayMode.shouldShowSideBySide) {
                    Modifier.horizontalScroll(scrollState)
                } else {
                    Modifier
                },
            )
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
                displayMode.shouldShowSideBySide -> SideBySideLyricsSection(
                    section = section,
                    keyOffset = keyOffset,
                    lineTextStyle = lineTextStyle,
                    chordChipStyle = chordChipStyle,
                    shouldShowInline = displayMode.shouldShowInline,
                    onChordClicked = onChordClicked,
                )

                displayMode.shouldShowInline -> section.lines.forEach { line ->
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

    val measurablesPerLine = 2

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

                Row(
                    horizontalArrangement = Arrangement.spacedBy(colSpacing),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    line.chords.forEach { chord ->
                        SongbookChip(
                            label = ChordLibrary.transpose(chord.value, keyOffset),
                            isSelected = false,
                            onClick = { onChordClicked(chord.value) },
                            chipStyle = chordChipStyle,
                        )
                    }
                }
            }
        },
    ) { measurables, constraints ->
        val lineCount = section.lines.size
        val allPlaceables = measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }

        val textPlaceables = allPlaceables.filterIndexed { index, _ -> index % measurablesPerLine == 0 }
        val chordPlaceables = allPlaceables.filterIndexed { index, _ -> index % measurablesPerLine == 1 }

        val maxTextWidth = textPlaceables.maxOfOrNull { it.width } ?: 0
        val maxChordWidth = chordPlaceables.maxOfOrNull { it.width } ?: 0

        val heights = List(lineCount) { lineIndex ->
            maxOf(textPlaceables[lineIndex].height, chordPlaceables[lineIndex].height)
        }

        val totalHeight = (heights.sum() + (lineCount - 1) * lineSpacing.roundToPx()).coerceAtLeast(0)

        val layoutWidth = if (constraints.hasBoundedWidth) {
            constraints.maxWidth
        } else {
            (maxTextWidth + chordMargin.roundToPx() + maxChordWidth).coerceAtLeast(constraints.minWidth)
        }

        layout(layoutWidth, totalHeight) {
            var currentY = 0
            for (lineIndex in 0 until lineCount) {
                val lineHeight = heights[lineIndex]
                val textPlaceable = textPlaceables[lineIndex]
                val chordPlaceable = chordPlaceables[lineIndex]

                textPlaceable.placeRelative(0, currentY + (lineHeight - textPlaceable.height) / 2)

                if (chordPlaceable.width > 0) {
                    chordPlaceable.placeRelative(
                        maxTextWidth + chordMargin.roundToPx(),
                        currentY + (lineHeight - chordPlaceable.height) / 2,
                    )
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
    val textMeasurer = rememberTextMeasurer()
    val currentTextStyle = remember(line.chords.isEmpty(), lineTextStyle) {
        if (line.chords.isEmpty()) {
            lineTextStyle
        } else {
            val fontSize = lineTextStyle.typography.fontSize
            lineTextStyle.copy(
                typography = lineTextStyle.typography.copy(
                    lineHeight = (fontSize.value * 2.5f + 12).sp,
                    lineHeightStyle = LineHeightStyle(
                        alignment = LineHeightStyle.Alignment.Bottom,
                        trim = LineHeightStyle.Trim.None,
                    ),
                ),
            )
        }
    }

    Layout(
        modifier = Modifier.fillMaxWidth(),
        content = {
            SongbookText(
                text = line.line,
                textStyle = currentTextStyle,
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

        val textLayoutResult: TextLayoutResult = textMeasurer.measure(
            text = AnnotatedString(line.line),
            style = currentTextStyle.typography.merge(
                TextStyle(
                    color = currentTextStyle.textColor,
                    textAlign = currentTextStyle.textAlign,
                ),
            ),
            constraints = constraints,
            softWrap = currentTextStyle.softWrap,
            overflow = currentTextStyle.textOverflow,
            maxLines = currentTextStyle.maxLines,
        )

        val textPlaceable = textMeasurable.measure(constraints)
        val chordPlaceables = chordMeasurables.map {
            it.measure(constraints.copy(minWidth = 0, minHeight = 0))
        }

        layout(textPlaceable.width, textPlaceable.height) {
            textPlaceable.placeRelative(0, 0)
            chordPlaceables.forEachIndexed { index, placeable ->
                val chord = line.chords[index]
                val lineIndex = textLayoutResult.getLineForOffset(chord.position)
                val horizontalPosition = textLayoutResult.getHorizontalPosition(
                    offset = chord.position,
                    usePrimaryDirection = true,
                )
                val verticalPosition = textLayoutResult.getLineTop(lineIndex)

                placeable.placeRelative(
                    x = horizontalPosition.toInt(),
                    y = verticalPosition.toInt(),
                )
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
