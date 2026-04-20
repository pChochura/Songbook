package com.pointlessapps.songbook.lyrics.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessapps.songbook.core.song.ChordLibrary
import com.pointlessapps.songbook.core.song.model.Chord
import com.pointlessapps.songbook.core.song.model.Section
import com.pointlessapps.songbook.ui.components.SongbookChip
import com.pointlessapps.songbook.ui.components.SongbookChipStyle
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.SongbookTextStyle
import com.pointlessapps.songbook.ui.theme.spacing
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
internal fun SideBySideLyricsSection(
    section: Section,
    keyOffset: Int,
    lineTextStyle: SongbookTextStyle,
    chordChipStyle: SongbookChipStyle,
    shouldShowInline: Boolean,
    editable: Boolean,
    onChordClicked: (String) -> Unit,
    onChordMoved: (Chord, Int) -> Unit,
    onCursorFinalized: (Int, Int, Rect) -> Unit,
) {
    val lineSpacing = MaterialTheme.spacing.small
    val chordMargin = MaterialTheme.spacing.extraHuge
    val colSpacing = MaterialTheme.spacing.extraSmall

    val measurablesPerLine = 2

    Layout(
        modifier = Modifier.fillMaxWidth(),
        content = {
            var currentLineOffset = 0
            section.lines.forEach { line ->
                val lineOffset = currentLineOffset
                when {
                    shouldShowInline -> InlineLyricsLine(
                        line = line,
                        keyOffset = keyOffset,
                        lineTextStyle = lineTextStyle,
                        chordChipStyle = chordChipStyle,
                        editable = editable,
                        onChordClicked = onChordClicked,
                        onChordMoved = onChordMoved,
                        onCursorFinalized = { index, rect ->
                            onCursorFinalized(section.id, lineOffset + index, rect)
                        },
                    )

                    else -> TextOnlyLyricsLine(
                        line = line,
                        lineTextStyle = lineTextStyle,
                    )
                }

                currentLineOffset += line.line.length + 1

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
        val allPlaceables =
            measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }

        val textPlaceables =
            allPlaceables.filterIndexed { index, _ -> index % measurablesPerLine == 0 }
        val chordPlaceables =
            allPlaceables.filterIndexed { index, _ -> index % measurablesPerLine == 1 }

        val maxTextWidth = textPlaceables.maxOfOrNull { it.width } ?: 0
        val maxChordWidth = chordPlaceables.maxOfOrNull { it.width } ?: 0

        val heights = List(lineCount) { lineIndex ->
            maxOf(textPlaceables[lineIndex].height, chordPlaceables[lineIndex].height)
        }

        val totalHeight =
            (heights.sum() + (lineCount - 1) * lineSpacing.roundToPx()).coerceAtLeast(0)

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
internal fun InlineLyricsLine(
    line: Section.Line,
    keyOffset: Int,
    lineTextStyle: SongbookTextStyle,
    chordChipStyle: SongbookChipStyle,
    editable: Boolean,
    onChordClicked: (String) -> Unit,
    onChordMoved: (Chord, Int) -> Unit,
    onCursorFinalized: (Int, Rect) -> Unit,
) {
    val textMeasurer = rememberTextMeasurer()
    val currentTextStyle = remember(line.chords.isEmpty(), lineTextStyle) {
        calculateInlineTextStyle(line, lineTextStyle)
    }

    var textLayoutResultState by remember { mutableStateOf<TextLayoutResult?>(null) }
    var textPosition by remember { mutableStateOf(Offset.Zero) }

    var draggingChordIdx by remember { mutableStateOf<Int?>(null) }
    var draggingChordX by remember { mutableFloatStateOf(0f) }

    val cursorXAnim = remember { Animatable(0f) }
    val cursorYAnim = remember { Animatable(0f) }
    val cursorAlphaAnim = remember { Animatable(0f) }

    val density = LocalDensity.current
    val cursorBaseHeightPx = with(density) { lineTextStyle.typography.fontSize.toPx() * 2f }

    var layoutCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Layout(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { layoutCoordinates = it }
            .cursorDrag(
                enabled = editable,
                textLayoutResult = textLayoutResultState,
                textPosition = textPosition,
                cursorBaseHeightPx = cursorBaseHeightPx,
                cursorXAnim = cursorXAnim,
                cursorYAnim = cursorYAnim,
                cursorAlphaAnim = cursorAlphaAnim,
                onCursorFinalized = onCursorFinalized,
                layoutCoordinates = layoutCoordinates,
            ),
        content = {
            SongbookText(
                text = line.line,
                textStyle = currentTextStyle,
                onTextLayout = { textLayoutResultState = it },
                modifier = Modifier.onGloballyPositioned {
                    textPosition = it.positionInParent()
                },
            )
            line.chords.forEachIndexed { index, chord ->
                SongbookChip(
                    label = ChordLibrary.transpose(chord.value, keyOffset),
                    isSelected = false,
                    onClick = { onChordClicked(chord.value) },
                    chipStyle = chordChipStyle,
                    modifier = Modifier.chordDrag(
                        enabled = editable,
                        index = index,
                        chord = chord,
                        textLayoutResult = textLayoutResultState,
                        onDraggingChanged = { idx, x ->
                            draggingChordIdx = idx
                            draggingChordX = x
                        },
                        onChordMoved = onChordMoved,
                    ),
                )
            }
            Cursor(
                xAnim = cursorXAnim,
                yAnim = cursorYAnim,
                alphaAnim = cursorAlphaAnim,
                baseHeightPx = cursorBaseHeightPx,
            )
        },
    ) { measurables, constraints ->
        val textMeasurable = measurables[0]
        val chordMeasurables = measurables.subList(1, 1 + line.chords.size)
        val cursorMeasurable = measurables.last()

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
        val cursorPlaceable = cursorMeasurable.measure(
            constraints.copy(minWidth = 0, minHeight = 0),
        )

        layout(textPlaceable.width, textPlaceable.height) {
            textPlaceable.placeRelative(0, 0)
            chordPlaceables.forEachIndexed { index, placeable ->
                val chord = line.chords[index]
                val lineIndex = textLayoutResult.getLineForOffset(chord.linePosition)
                val horizontalPosition = if (index == draggingChordIdx) {
                    draggingChordX
                } else {
                    textLayoutResult.getHorizontalPosition(
                        offset = chord.linePosition,
                        usePrimaryDirection = true,
                    )
                }
                val verticalPosition = textLayoutResult.getLineTop(lineIndex)

                placeable.placeRelative(
                    x = horizontalPosition.toInt(),
                    y = verticalPosition.toInt(),
                )
            }
            cursorPlaceable.placeRelative(0, 0)
        }
    }
}

@Composable
internal fun TextOnlyLyricsLine(
    line: Section.Line,
    lineTextStyle: SongbookTextStyle,
) {
    SongbookText(
        text = line.line,
        modifier = Modifier.fillMaxWidth(),
        textStyle = lineTextStyle,
    )
}

private fun calculateInlineTextStyle(
    line: Section.Line,
    lineTextStyle: SongbookTextStyle,
): SongbookTextStyle {
    if (line.chords.isEmpty()) return lineTextStyle

    val fontSize = lineTextStyle.typography.fontSize
    return lineTextStyle.copy(
        typography = lineTextStyle.typography.copy(
            lineHeight = (fontSize.value * 2.5f + 12).sp,
            lineHeightStyle = LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Bottom,
                trim = LineHeightStyle.Trim.None,
            ),
        ),
    )
}

@Composable
private fun Cursor(
    xAnim: Animatable<Float, *>,
    yAnim: Animatable<Float, *>,
    alphaAnim: Animatable<Float, *>,
    baseHeightPx: Float,
) {
    val density = LocalDensity.current
    Box(
        modifier = Modifier
            .graphicsLayer {
                alpha = alphaAnim.value
                translationX = xAnim.value - size.width / 2
                translationY = yAnim.value
                transformOrigin = TransformOrigin(0.5f, 0f)
            }
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.primary)
            .width(2.dp)
            .height(with(density) { baseHeightPx.toDp() }),
    )
}

private fun Modifier.cursorDrag(
    enabled: Boolean,
    textLayoutResult: TextLayoutResult?,
    textPosition: Offset,
    cursorBaseHeightPx: Float,
    cursorXAnim: Animatable<Float, *>,
    cursorYAnim: Animatable<Float, *>,
    cursorAlphaAnim: Animatable<Float, *>,
    onCursorFinalized: (Int, Rect) -> Unit,
    layoutCoordinates: LayoutCoordinates?,
): Modifier =
    if (!enabled) this else pointerInput(textLayoutResult, textPosition, layoutCoordinates) {
        val layoutResult = textLayoutResult ?: return@pointerInput
        var currentIndex = 0
        var cursorRect = Rect.Zero
        coroutineScope {
            detectDragGesturesAfterLongPress(
                onDragStart = { startOffset ->
                    currentIndex = layoutResult.getOffsetForPosition(startOffset - textPosition)
                    val x = layoutResult.getHorizontalPosition(currentIndex, true)
                    val lineIdx = layoutResult.getLineForOffset(currentIndex)
                    val y = layoutResult.getLineBottom(lineIdx) - cursorBaseHeightPx

                    launch { cursorAlphaAnim.snapTo(1f) }
                    launch { cursorXAnim.snapTo(x) }
                    launch { cursorYAnim.snapTo(y) }
                },
                onDrag = { change, _ ->
                    currentIndex = layoutResult.getOffsetForPosition(change.position - textPosition)
                    val x = layoutResult.getHorizontalPosition(currentIndex, true)
                    val lineIdx = layoutResult.getLineForOffset(currentIndex)
                    val y = layoutResult.getLineBottom(lineIdx) - cursorBaseHeightPx

                    cursorRect = layoutCoordinates?.let {
                        val localOffset = textPosition + Offset(x, y)
                        val windowOffset = it.positionInWindow() + localOffset
                        Rect(windowOffset, Size(0f, cursorBaseHeightPx))
                    } ?: Rect.Zero

                    launch {
                        cursorXAnim.animateTo(x, spring(stiffness = 1200f))
                    }
                    launch { cursorYAnim.snapTo(y) }
                },
                onDragEnd = {
                    onCursorFinalized(currentIndex, cursorRect)
                    launch { cursorAlphaAnim.animateTo(0f) }
                },
                onDragCancel = {
                    launch { cursorAlphaAnim.animateTo(0f) }
                },
            )
        }
    }

private fun Modifier.chordDrag(
    enabled: Boolean,
    index: Int,
    chord: Chord,
    textLayoutResult: TextLayoutResult?,
    onDraggingChanged: (Int?, Float) -> Unit,
    onChordMoved: (Chord, Int) -> Unit,
): Modifier = if (!enabled) this else pointerInput(chord, textLayoutResult) {
    val layoutResult = textLayoutResult ?: return@pointerInput
    var draggingChordX = 0f
    detectDragGesturesAfterLongPress(
        onDragStart = {
            draggingChordX = layoutResult.getHorizontalPosition(chord.linePosition, true)
            onDraggingChanged(index, draggingChordX)
        },
        onDrag = { _, dragAmount ->
            draggingChordX = (draggingChordX + dragAmount.x)
                .coerceIn(0f, layoutResult.size.width.toFloat())
            onDraggingChanged(index, draggingChordX)
        },
        onDragEnd = {
            val midLineY = (layoutResult.getLineTop(0) + layoutResult.getLineBottom(0)) / 2f
            val newCharIndex = layoutResult.getOffsetForPosition(Offset(draggingChordX, midLineY))
            onChordMoved(chord, newCharIndex)
            onDraggingChanged(null, 0f)
        },
        onDragCancel = {
            onDraggingChanged(null, 0f)
        },
    )
}
