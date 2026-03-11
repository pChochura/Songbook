package com.pointlessapps.songbook.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pointlessapps.songbook.ui.theme.spacing

data class ChordMarker(val chord: String, val offset: Int)

@Composable
fun LyricsLine(
    text: String,
    chords: List<ChordMarker>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(vertical = MaterialTheme.spacing.small),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large)) {
            chords.forEach { marker ->
                ChordChip(chord = marker.chord)
            }
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
