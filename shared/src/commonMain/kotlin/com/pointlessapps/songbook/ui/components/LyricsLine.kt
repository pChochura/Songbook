package com.pointlessapps.songbook.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import com.pointlessapps.songbook.model.Chord
import com.pointlessapps.songbook.ui.theme.spacing

data class ChordMarker(val chord: Chord, val offset: Int)

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
    val targetX = remember(displayCursorIndex, textLayoutResult) {
        if (displayCursorIndex != null && textLayoutResult != null && displayCursorIndex <= text.length) {
            textLayoutResult!!.getHorizontalPosition(displayCursorIndex, true).also {
                lastTargetX = it
            }
        } else {
            lastTargetX
        }
    }

    val animatedX by animateFloatAsState(targetValue = targetX, label = "cursorX")
    val cursorAlpha by animateFloatAsState(
        targetValue = if (displayCursorIndex != null) 1f else 0f,
        label = "cursorAlpha",
    )

    val spacingExtraSmall = MaterialTheme.spacing.extraSmall
    val spacingSmall = MaterialTheme.spacing.small

    Layout(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = spacingSmall)
            .pointerInput(textLayoutResult) {
                val layoutResult = textLayoutResult ?: return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown()

                    internalCursorIndex = layoutResult.getOffsetForPosition(
                        down.position - textPosition,
                    )

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.pressed } ?: break

                        internalCursorIndex = layoutResult.getOffsetForPosition(
                            change.position - textPosition,
                        )
                        change.consume()
                    }

                    internalCursorIndex?.let {
                        onCursorFinalized(it, Offset(targetX, 0f))
                    }
                    internalCursorIndex = null
                }
            },
        content = {
            chords.forEach { marker ->
                ChordChip(
                    chord = marker.chord,
                    onClick = {
                        val x = textLayoutResult?.getHorizontalPosition(marker.offset, true) ?: 0f
                        onChordClicked(marker, Offset(x, 0f))
                    }
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
                    }
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.primary)
                    .width(2.dp),
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
                minHeight = textPlaceable.height,
                maxHeight = textPlaceable.height,
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

                // Place the cursor measurable; its actual X and Alpha are handled by graphicsLayer
                cursorPlaceable.placeRelative(0, chordsHeight + spacingPx)
            }
        }
    }
}
