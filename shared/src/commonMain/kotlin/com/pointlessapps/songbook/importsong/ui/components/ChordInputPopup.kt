package com.pointlessapps.songbook.importsong.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.pointlessapps.songbook.core.song.ChordLibrary
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.spacing

@Composable
internal fun ChordInputPopup(
    cursorRect: Rect,
    onChordSelected: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val queryTextFieldState = remember { TextFieldState() }
    val chords = remember(queryTextFieldState.text) {
        ChordLibrary.allChords.filter {
            it.startsWith(queryTextFieldState.text, ignoreCase = true)
        }
    }

    Popup(
        popupPositionProvider = object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize,
            ) = IntOffset(
                x = cursorRect.left.toInt() - popupContentSize.width / 2,
                y = cursorRect.bottom.toInt() - popupContentSize.height / 2,
            )
        },
        properties = PopupProperties(
            focusable = true,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
        onDismissRequest = onDismissRequest,
    ) {
        LazyVerticalGrid(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = MaterialTheme.shapes.large,
                )
                .sizeIn(
                    maxHeight = 300.dp,
                    maxWidth = 300.dp,
                )
                .fillMaxSize(),
            columns = GridCells.Adaptive(60.dp),
        ) {
            items(chords, key = { it }) { chord ->
                Box(
                    modifier = Modifier
                        .animateItem()
                        .aspectRatio(1f)
                        .clip(MaterialTheme.shapes.large)
                        .clickable(
                            onClick = { onChordSelected(chord) },
                            role = Role.Button,
                        )
                        .padding(
                            vertical = MaterialTheme.spacing.medium,
                            horizontal = MaterialTheme.spacing.medium,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    SongbookText(
                        text = chord,
                        textStyle = defaultSongbookTextStyle().copy(
                            typography = MaterialTheme.typography.bodyMedium,
                            textColor = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        ),
                    )
                }
            }
        }
    }
}
