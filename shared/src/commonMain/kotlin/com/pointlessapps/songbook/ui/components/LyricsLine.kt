package com.pointlessapps.songbook.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import com.pointlessapps.songbook.core.domain.models.ChordMarker
import com.pointlessapps.songbook.ui.theme.spacing

@Composable
fun LyricsLine(
    text: String,
    chords: List<ChordMarker>,
    modifier: Modifier = Modifier,
    cursorIndex: Int? = null,
    onCursorFinalized: (Int, Offset) -> Unit = { _, _ -> },
    onChordClicked: (ChordMarker, Offset) -> Unit = { _, _ -> },
) {
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var internalCursorIndex by remember { mutableStateOf<Int?>(null) }
    var textPosition by remember { mutableStateOf(Offset.Zero) }

    val displayCursorIndex = internalCursorIndex ?: cursorIndex

    // Animation logic
    var lastTargetX by remember { mutableFloatStateOf(0f) }
    var lastTargetY by remember { mutableFloatStateOf(0f) }
    var lastCursorHeight by remember { mutableFloatStateOf(0f) }

    val (targetX, targetY, cursorHeight) = remember(displayCursorIndex, textLayoutResult) {
        if (displayCursorIndex != null && textLayoutResult != null && displayCursorIndex <= text.length) {
            val lineIndex = textLayoutResult!!.getLineForOffset(displayCursorIndex)
            val x = textLayoutResult!!.getHorizontalPosition(displayCursorIndex, true)
            val y = textLayoutResult!!.getLineTop(lineIndex)
            val height = textLayoutResult!!.getLineBottom(lineIndex) - y

            lastTargetX = x
            lastTargetY = y
            lastCursorHeight = height

            Triple(x, y, height)
        } else {
            Triple(lastTargetX, lastTargetY, lastCursorHeight)
        }
    }

    val animatedX by animateFloatAsState(targetValue = targetX, label = "cursorX")
    val animatedY by animateFloatAsState(targetValue = targetY, label = "cursorY")
    val animatedHeight by animateFloatAsState(targetValue = cursorHeight, label = "cursorHeight")

    val cursorAlpha by animateFloatAsState(
        targetValue = if (displayCursorIndex != null) 1f else 0f,
        label = "cursorAlpha",
    )

    val density = LocalDensity.current
    val spacingExtraSmall = MaterialTheme.spacing.extraSmall
    val spacingSmall = MaterialTheme.spacing.small

    Layout(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = spacingSmall)
            .pointerInput(textLayoutResult) {
                val layoutResult = textLayoutResult ?: return@pointerInput
                detectDragGesturesAfterLongPress(
                    onDragStart = { startOffset ->
                        internalCursorIndex = layoutResult.getOffsetForPosition(startOffset - textPosition)
                    },
                    onDrag = { change, _ ->
                        internalCursorIndex = layoutResult.getOffsetForPosition(change.position - textPosition)
                    },
                    onDragEnd = {
                        internalCursorIndex?.let {
                            onCursorFinalized(it, Offset(layoutResult.getHorizontalPosition(it, true), 0f))
                        }
                        internalCursorIndex = null
                    },
                    onDragCancel = {
                        internalCursorIndex = null
                    },
                )
            },
        content = {
            chords.forEach { marker ->
                ChordChip(
                    chord = marker.chord,
                    onClick = {
                        val x = textLayoutResult?.getHorizontalPosition(marker.offset, true) ?: 0f
                        onChordClicked(marker, Offset(x, 0f))
                    },
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                onTextLayout = { textLayoutResult = it },
                modifier = Modifier.onGloballyPositioned {
                    textPosition = it.positionInParent()
                },
            )
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        alpha = cursorAlpha
                        translationX = animatedX - size.width / 2
                        translationY = animatedY
                    }
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.primary)
                    .width(2.dp)
                    .height(with(density) { animatedHeight.toDp() }),
            )
        },
    ) { measurables, constraints ->
        val chordPlaceables = measurables.subList(0, chords.size).map {
            it.measure(constraints.copy(minWidth = 0, minHeight = 0))
        }
        val textPlaceable = measurables[chords.size].measure(constraints)
        val cursorPlaceable = measurables.last().measure(
            constraints.copy(
                minWidth = 0,
                minHeight = 0,
            ),
        )

        val chordsHeight = if (chordPlaceables.isEmpty()) 0 else chordPlaceables.maxOf { it.height }
        val spacingPx = if (chordPlaceables.isEmpty()) 0 else spacingExtraSmall.roundToPx()

        layout(
            width = if (constraints.hasBoundedWidth) constraints.maxWidth else textPlaceable.width,
            height = chordsHeight + spacingPx + textPlaceable.height,
        ) {
            textPlaceable.placeRelative(0, chordsHeight + spacingPx)

            textLayoutResult?.let { result ->
                chords.forEachIndexed { index, marker ->
                    if (marker.offset <= text.length) {
                        val x = result.getHorizontalPosition(marker.offset, true).toInt()
                        val chipPlaceable = chordPlaceables[index]
                        chipPlaceable.placeRelative(x, 0)
                    }
                }

                // Place the cursor measurable; its actual X, Y and Alpha are handled by graphicsLayer
                cursorPlaceable.placeRelative(0, chordsHeight + spacingPx)
            }
        }
    }
}
