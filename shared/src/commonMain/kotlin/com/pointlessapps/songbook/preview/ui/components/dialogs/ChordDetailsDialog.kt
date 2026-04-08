package com.pointlessapps.songbook.preview.ui.components.dialogs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import com.pointlessapps.songbook.core.song.ChordLibrary
import com.pointlessapps.songbook.core.song.model.ChordPosition
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.common_dismiss
import com.pointlessapps.songbook.shared.common_next
import com.pointlessapps.songbook.shared.common_previous
import com.pointlessapps.songbook.ui.components.SongbookButton
import com.pointlessapps.songbook.ui.components.SongbookDialog
import com.pointlessapps.songbook.ui.components.SongbookDialogDismissible
import com.pointlessapps.songbook.ui.components.SongbookIconButton
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.defaultSongbookButtonStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookDialogStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.DEFAULT_BORDER_WIDTH
import com.pointlessapps.songbook.ui.theme.IconArrowLeft
import com.pointlessapps.songbook.ui.theme.IconArrowRight
import com.pointlessapps.songbook.ui.theme.IconNote
import com.pointlessapps.songbook.ui.theme.MEDIUM_CORNER_RADIUS
import com.pointlessapps.songbook.ui.theme.spacing
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
internal fun ChordDetailsDialog(
    chord: String,
    keyOffset: Int,
    onDismissRequest: () -> Unit,
) {
    val chordLibrary = koinInject<ChordLibrary>()

    val transposedChord = remember(chord, keyOffset) { ChordLibrary.transpose(chord, keyOffset) }
    val positions = remember(transposedChord) { chordLibrary.getChordPositions(transposedChord) }
    var currentPositionIndex by rememberSaveable { mutableIntStateOf(0) }

    SongbookDialog(
        onDismissRequest = onDismissRequest,
        dialogStyle = defaultSongbookDialogStyle().copy(
            label = transposedChord,
            icon = IconNote,
            dismissible = SongbookDialogDismissible.Both,
        ),
    ) {
        ChordDiagram(
            position = positions[currentPositionIndex],
            modifier = Modifier.fillMaxWidth().aspectRatio(1f),
        )

        Counter(
            value = currentPositionIndex + 1,
            maxValue = positions.size,
            onPreviousClicked = {
                currentPositionIndex = (currentPositionIndex - 1 + positions.size) % positions.size
            },
            onNextClicked = {
                currentPositionIndex = (currentPositionIndex + 1) % positions.size
            },
        )

        SongbookButton(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = DEFAULT_BORDER_WIDTH,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = CircleShape,
                ),
            label = stringResource(Res.string.common_dismiss),
            onClick = { onDismissRequest() },
            buttonStyle = defaultSongbookButtonStyle().copy(
                containerColor = Color.Transparent,
                textStyle = defaultSongbookTextStyle().copy(
                    textAlign = TextAlign.Center,
                    textColor = MaterialTheme.colorScheme.onSurface,
                ),
            ),
        )
    }
}

@Composable
private fun Counter(
    value: Int,
    maxValue: Int,
    onPreviousClicked: () -> Unit,
    onNextClicked: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
    ) {
        SongbookIconButton(
            icon = IconArrowLeft,
            tooltipLabel = Res.string.common_previous,
            onClick = onPreviousClicked,
        )

        SongbookText(
            text = "$value/$maxValue",
            textStyle = defaultSongbookTextStyle().copy(
                typography = MaterialTheme.typography.titleMedium,
            ),
        )

        SongbookIconButton(
            icon = IconArrowRight,
            tooltipLabel = Res.string.common_next,
            onClick = onNextClicked,
        )
    }
}

@Composable
private fun ChordDiagram(
    position: ChordPosition,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()

    val fingerColor = MaterialTheme.colorScheme.primary
    val fretColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    val stringColor = MaterialTheme.colorScheme.onSurfaceVariant
    val labelStyle = MaterialTheme.typography.labelSmall.copy(
        color = MaterialTheme.colorScheme.onSurface,
    )
    val fingerStyle = MaterialTheme.typography.labelSmall.copy(
        color = MaterialTheme.colorScheme.onPrimary,
    )

    val spacingSmall = MaterialTheme.spacing.small
    val spacingMedium = MaterialTheme.spacing.medium

    Canvas(modifier = modifier) {
        val spacingSmall = spacingSmall.toPx()
        val spacingMedium = spacingMedium.toPx()

        val numStrings = 6
        val numFrets = 4

        val topPadding = spacingSmall + textMeasurer.measure("X", labelStyle).size.height
        val horizontalPadding = spacingMedium + textMeasurer.measure("12", labelStyle).size.width
        val bottomPadding = spacingSmall + textMeasurer.measure("EADGBE", labelStyle).size.height

        val strokeWidth = DEFAULT_BORDER_WIDTH.toPx()
        val cornerRadius = MEDIUM_CORNER_RADIUS.toPx()

        val diagramWidth = size.width - 2 * horizontalPadding
        val diagramHeight = size.height - bottomPadding

        val stringSpacing = diagramWidth / (numStrings - 1)
        val fretSpacing = diagramHeight / numFrets

        // Draw frets (horizontal lines)
        for (i in 0..numFrets) {
            val y = topPadding + i * fretSpacing
            val strokeWidth =
                if (i == 0 && position.baseFret == 1) strokeWidth * 3f else strokeWidth
            drawLine(
                color = fretColor,
                start = Offset(horizontalPadding, y),
                end = Offset(horizontalPadding + diagramWidth, y),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )

            // Draw fret numbers
            if (i < numFrets) {
                val fretNumber = position.baseFret + i
                val textLayoutResult = textMeasurer.measure(fretNumber.toString(), labelStyle)
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(
                        x = horizontalPadding - textLayoutResult.size.width - spacingMedium,
                        y = y + (fretSpacing - textLayoutResult.size.height) / 2,
                    ),
                )
            }
        }

        // Draw strings (vertical lines)
        for (i in 0 until numStrings) {
            val x = horizontalPadding + i * stringSpacing
            drawLine(
                color = stringColor,
                start = Offset(x, topPadding),
                end = Offset(x, topPadding + diagramHeight),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }

        // Draw string labels
        "EADGBE".forEachIndexed { i, label ->
            val x = horizontalPadding + i * stringSpacing
            val textLayoutResult = textMeasurer.measure(label.toString(), labelStyle)
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(
                    x = x - textLayoutResult.size.width / 2,
                    y = topPadding + diagramHeight + spacingSmall,
                ),
            )
        }

        // Draw markers (muted/open)
        position.frets.forEachIndexed { i, fret ->
            val x = horizontalPadding + i * stringSpacing
            if (fret == -1) {
                val textLayoutResult = textMeasurer.measure("X", labelStyle)
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(
                        x = x - textLayoutResult.size.width / 2,
                        y = 0f,
                    ),
                )
            } else if (fret == 0) {
                val textLayoutResult = textMeasurer.measure("O", labelStyle)
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(
                        x = x - textLayoutResult.size.width / 2,
                        y = 0f,
                    ),
                )
            }
        }

        // Draw barres
        position.barres.forEach { barreFret ->
            val fretIndex = barreFret - 1
            if (fretIndex in 0 until numFrets) {
                val y = topPadding + fretIndex * fretSpacing + fretSpacing / 2

                // Find first and last string for the barre at this fret
                var firstString = -1
                var lastString = -1
                position.frets.forEachIndexed { i, fret ->
                    if (fret == barreFret) {
                        if (firstString == -1) firstString = i
                        lastString = i
                    }
                }

                if (firstString != -1 && lastString != -1) {
                    val startX = horizontalPadding + firstString * stringSpacing
                    val endX = horizontalPadding + lastString * stringSpacing
                    drawRoundRect(
                        color = fingerColor,
                        topLeft = Offset(startX - cornerRadius, y - cornerRadius),
                        size = Size(endX - startX + cornerRadius * 2, cornerRadius * 2),
                        cornerRadius = CornerRadius(cornerRadius),
                    )
                }
            }
        }

        // Draw dots
        position.frets.forEachIndexed { stringIndex, fretIndex ->
            if (fretIndex > 0) {
                val x = horizontalPadding + stringIndex * stringSpacing
                val y = topPadding + (fretIndex - 1) * fretSpacing + fretSpacing / 2
                drawCircle(
                    color = fingerColor,
                    radius = cornerRadius,
                    center = Offset(x, y),
                )

                val finger = position.fingers.getOrNull(stringIndex) ?: 0
                if (finger > 0) {
                    val fingerText = finger.toString()
                    val textLayoutResult = textMeasurer.measure(fingerText, fingerStyle)
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(
                            x = x - textLayoutResult.size.width / 2,
                            y = y - textLayoutResult.size.height / 2,
                        ),
                    )
                }
            }
        }
    }
}
