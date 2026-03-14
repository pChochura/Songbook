package com.pointlessapps.songbook.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessapps.songbook.core.domain.models.ChordMarker
import com.pointlessapps.songbook.ui.theme.spacing
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun LyricsLine(
    text: String,
    chords: List<ChordMarker>,
    modifier: Modifier = Modifier,
    cursorIndex: Int? = null,
    onCursorFinalized: (Int, Offset) -> Unit = { _, _ -> },
    onChordClicked: (ChordMarker, Offset) -> Unit = { _, _ -> },
    onChordMoved: (ChordMarker, Int) -> Unit = { _, _ -> },
) {
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var textPosition by remember { mutableStateOf(Offset.Zero) }

    // Chord drag — read only in measurePolicy (layout phase), no recomposition triggered
    var draggingChordIdx by remember { mutableStateOf<Int?>(null) }
    var draggingChordX by remember { mutableFloatStateOf(0f) }

    // Cursor — Animatable values are read only in graphicsLayer (draw phase), no recomposition
    val cursorXAnim = remember { Animatable(0f) }
    val cursorYAnim = remember { Animatable(0f) }
    val cursorHeightAnim = remember { Animatable(0f) }
    val cursorAlphaAnim = remember { Animatable(0f) }

    // Apply externally-supplied cursorIndex (e.g. restored from state)
    LaunchedEffect(cursorIndex, textLayoutResult) {
        val lr = textLayoutResult ?: return@LaunchedEffect
        if (cursorIndex != null && cursorIndex <= text.length) {
            val lineIdx = lr.getLineForOffset(cursorIndex)
            launch { cursorXAnim.animateTo(lr.getHorizontalPosition(cursorIndex, true), spring()) }
            launch { cursorYAnim.animateTo(lr.getLineTop(lineIdx), spring()) }
            launch {
                cursorHeightAnim.animateTo(
                    lr.getLineBottom(lineIdx) - lr.getLineTop(lineIdx),
                    spring(),
                )
            }
            cursorAlphaAnim.animateTo(1f)
        } else {
            cursorAlphaAnim.animateTo(0f)
        }
    }

    val density = LocalDensity.current
    // Fixed base height for the cursor box; scaleY in graphicsLayer handles the actual height
    val cursorBaseHeightPx = with(density) { 24.dp.toPx() }
    val spacingExtraSmall = MaterialTheme.spacing.extraSmall
    val spacingSmall = MaterialTheme.spacing.small

    Layout(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = spacingSmall)
            .pointerInput(textLayoutResult) {
                val layoutResult = textLayoutResult ?: return@pointerInput
                var currentIndex = 0
                coroutineScope {
                    val scope = this
                    detectDragGesturesAfterLongPress(
                        onDragStart = { startOffset ->
                            currentIndex =
                                layoutResult.getOffsetForPosition(startOffset - textPosition)
                            val x = layoutResult.getHorizontalPosition(currentIndex, true)
                            val lineIdx = layoutResult.getLineForOffset(currentIndex)
                            val y = layoutResult.getLineTop(lineIdx)
                            val h = layoutResult.getLineBottom(lineIdx) - y
                            scope.launch { cursorAlphaAnim.snapTo(1f) }
                            scope.launch { cursorXAnim.snapTo(x) }
                            scope.launch { cursorYAnim.snapTo(y) }
                            scope.launch { cursorHeightAnim.snapTo(h) }
                        },
                        onDrag = { change, _ ->
                            currentIndex =
                                layoutResult.getOffsetForPosition(change.position - textPosition)
                            val x = layoutResult.getHorizontalPosition(currentIndex, true)
                            val lineIdx = layoutResult.getLineForOffset(currentIndex)
                            val y = layoutResult.getLineTop(lineIdx)
                            val h = layoutResult.getLineBottom(lineIdx) - y
                            scope.launch { cursorXAnim.animateTo(x, spring(stiffness = 1200f)) }
                            scope.launch { cursorYAnim.snapTo(y) }
                            scope.launch { cursorHeightAnim.snapTo(h) }
                        },
                        onDragEnd = {
                            onCursorFinalized(
                                currentIndex,
                                Offset(layoutResult.getHorizontalPosition(currentIndex, true), 0f),
                            )
                            scope.launch { cursorAlphaAnim.animateTo(0f) }
                        },
                        onDragCancel = {
                            scope.launch { cursorAlphaAnim.animateTo(0f) }
                        },
                    )
                }
            },
        content = {
            chords.forEachIndexed { chordIndex, marker ->
                ChordChip(
                    chord = marker.chord,
                    modifier = Modifier.pointerInput(marker, textLayoutResult) {
                        val layoutResult = textLayoutResult ?: return@pointerInput
                        detectDragGesturesAfterLongPress(
                            onDragStart = { _ ->
                                draggingChordIdx = chordIndex
                                draggingChordX =
                                    layoutResult.getHorizontalPosition(marker.offset, true)
                            },
                            onDrag = { _, dragAmount ->
                                draggingChordX = (draggingChordX + dragAmount.x)
                                    .coerceIn(0f, layoutResult.size.width.toFloat())
                            },
                            onDragEnd = {
                                val result =
                                    textLayoutResult ?: return@detectDragGesturesAfterLongPress
                                val midLineY = (result.getLineTop(0) + result.getLineBottom(0)) / 2f
                                val newCharIndex =
                                    result.getOffsetForPosition(Offset(draggingChordX, midLineY))
                                onChordMoved(marker, newCharIndex)
                                draggingChordIdx = null
                            },
                            onDragCancel = {
                                draggingChordIdx = null
                            },
                        )
                    },
                    onClick = {
                        val x = textLayoutResult?.getHorizontalPosition(marker.offset, true) ?: 0f
                        onChordClicked(marker, Offset(x, 0f))
                    },
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 32.sp,
                color = MaterialTheme.colorScheme.onSurface,
                onTextLayout = { textLayoutResult = it },
                modifier = Modifier.onGloballyPositioned {
                    textPosition = it.positionInParent()
                },
            )
            // Cursor — position/size/alpha controlled entirely in graphicsLayer (draw phase)
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        alpha = cursorAlphaAnim.value
                        translationX = cursorXAnim.value - size.width / 2
                        translationY = cursorYAnim.value
                        scaleY = cursorHeightAnim.value / cursorBaseHeightPx
                        transformOrigin = TransformOrigin(0.5f, 0f)
                    }
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.primary)
                    .width(2.dp)
                    .height(with(density) { cursorBaseHeightPx.toDp() }),
            )
        },
    ) { measurables, constraints ->
        val chordPlaceables = measurables.subList(0, chords.size).map {
            it.measure(constraints.copy(minWidth = 0, minHeight = 0))
        }
        val textPlaceable = measurables[chords.size].measure(constraints)
        val cursorPlaceable = measurables.last().measure(
            constraints.copy(minWidth = 0, minHeight = 0),
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
                        val x = if (index == draggingChordIdx) {
                            draggingChordX.toInt()
                        } else {
                            result.getHorizontalPosition(marker.offset, true).toInt()
                        }
                        chordPlaceables[index].placeRelative(x, 0)
                    }
                }
                cursorPlaceable.placeRelative(0, chordsHeight + spacingPx)
            }
        }
    }
}
