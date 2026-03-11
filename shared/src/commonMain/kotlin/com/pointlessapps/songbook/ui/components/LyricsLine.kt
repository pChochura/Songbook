package com.pointlessapps.songbook.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.TextLayoutResult
import com.pointlessapps.songbook.ui.theme.spacing

data class ChordMarker(val chord: String, val offset: Int)

@Composable
fun LyricsLine(
    text: String,
    chords: List<ChordMarker>,
    modifier: Modifier = Modifier,
) {
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val spacingExtraSmall = MaterialTheme.spacing.extraSmall
    val spacingSmall = MaterialTheme.spacing.small

    Layout(
        modifier = modifier.padding(vertical = spacingSmall),
        content = {
            chords.forEach { marker ->
                ChordChip(chord = marker.chord)
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                onTextLayout = { textLayoutResult = it },
            )
        },
    ) { measurables, constraints ->
        val chordPlaceables = measurables.subList(0, measurables.size - 1).map {
            it.measure(constraints.copy(minWidth = 0, minHeight = 0))
        }
        val textPlaceable = measurables.last().measure(constraints)

        val chordsHeight = if (chordPlaceables.isEmpty()) 0 else chordPlaceables.maxOf { it.height }
        val spacingPx = if (chordPlaceables.isEmpty()) 0 else spacingExtraSmall.roundToPx()

        layout(
            width = textPlaceable.width,
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
            }
        }
    }
}
