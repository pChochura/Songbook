package com.pointlessapps.songbook.lyrics.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.IntOffset
import com.pointlessapps.songbook.core.song.model.Section
import com.pointlessapps.songbook.ui.components.SongbookChip
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.defaultSongbookChipStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle

internal fun LazyListScope.lyricsSection(section: Section, textScale: Int) {
    val textScaleFloat = textScale / 100f

    if (section.name.isNotEmpty()) {
        item {
            SongbookText(
                text = section.name,
                textStyle = defaultSongbookTextStyle().copy(
                    textColor = MaterialTheme.colorScheme.primary,
                    typography = MaterialTheme.typography.labelSmall,
                ),
            )
        }
    }

    items(section.lines) { line ->
        Column(modifier = Modifier.fillMaxWidth()) {
            var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

            Box {
                textLayoutResult?.let { layout ->
                    line.chords.forEach { chord ->
                        val lineIndex = layout.getLineForOffset(chord.position)
                        val horizontalPosition = layout.getHorizontalPosition(
                            offset = chord.position,
                            usePrimaryDirection = true,
                        )
                        val verticalPosition = layout.getLineTop(lineIndex)

                        SongbookChip(
                            modifier = Modifier.offset {
                                IntOffset(
                                    x = horizontalPosition.toInt(),
                                    y = verticalPosition.toInt(),
                                )
                            },
                            label = chord.value,
                            isSelected = false,
                            onClick = {
                                // TODO add chord explanation dialog
                            },
                            chipStyle = defaultSongbookChipStyle().copy(
                                containerColor = MaterialTheme.colorScheme.primary,
                                labelColor = MaterialTheme.colorScheme.onPrimary,
                                outlineColor = Color.Transparent,
                                labelTypography = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = MaterialTheme.typography.labelLarge.fontSize * textScaleFloat,
                                ),
                            ),
                        )
                    }
                }
            }

            SongbookText(
                text = line.line,
                onTextLayout = { textLayoutResult = it },
                textStyle = defaultSongbookTextStyle().copy(
                    softWrap = false,
                    textColor = MaterialTheme.colorScheme.onSurface,
                    typography = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize * textScaleFloat,
                    ),
                ),
            )
        }
    }
}
