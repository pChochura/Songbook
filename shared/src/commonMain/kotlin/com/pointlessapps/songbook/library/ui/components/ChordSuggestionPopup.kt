package com.pointlessapps.songbook.library.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.spacing

@Composable
internal fun ChordSuggestionPopup(
    cursorRect: Rect,
    suggestions: List<String>,
    onChordSelected: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    Popup(
        offset = IntOffset(
            x = cursorRect.left.toInt(),
            y = cursorRect.bottom.toInt() + with(
                LocalDensity.current,
            ) { MaterialTheme.spacing.small.roundToPx() },
        ),
        onDismissRequest = onDismissRequest,
    ) {
        LazyColumn(
            modifier = Modifier.background(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = MaterialTheme.shapes.small,
            ).animateContentSize(),
            contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.small),
        ) {
            items(suggestions, key = { it }) { chord ->
                SongbookText(
                    modifier = Modifier
                        .animateItem()
                        .fillMaxHeight()
                        .clickable(
                            onClick = { onChordSelected(chord) },
                            role = Role.Button,
                        )
                        .padding(
                            vertical = MaterialTheme.spacing.small,
                            horizontal = MaterialTheme.spacing.small,
                        ),
                    text = chord,
                    textStyle = defaultSongbookTextStyle().copy(
                        typography = MaterialTheme.typography.bodyMedium,
                        textColor = MaterialTheme.colorScheme.onSurface,
                    ),
                )
            }
        }
    }
}
