package com.pointlessapps.songbook.preview.ui

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.pointlessapps.overscrolled.Direction
import com.pointlessapps.overscrolled.OverscrolledEffectNode
import com.pointlessapps.overscrolled.createOverscrolledEffectNode
import com.pointlessapps.overscrolled.rememberHorizonalOverscrolledEffect
import com.pointlessapps.songbook.core.song.model.Section
import com.pointlessapps.songbook.lyrics.DisplayMode
import com.pointlessapps.songbook.lyrics.LyricsViewModel.Companion.MAX_ZOOM
import com.pointlessapps.songbook.lyrics.LyricsViewModel.Companion.MIN_ZOOM
import com.pointlessapps.songbook.lyrics.WrapMode
import com.pointlessapps.songbook.lyrics.ui.components.LyricsSections
import com.pointlessapps.songbook.lyrics.ui.components.SongHeader
import com.pointlessapps.songbook.lyrics.ui.components.TextScaleOverlay
import com.pointlessapps.songbook.preview.ui.components.PinchToZoomBox
import com.pointlessapps.songbook.preview.ui.components.collectIsPinchedToZoomAsState
import com.pointlessapps.songbook.preview.ui.components.dialogs.ChordDetailsDialog
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.lyrics_no_next_song
import com.pointlessapps.songbook.shared.lyrics_no_previous_song
import com.pointlessapps.songbook.ui.theme.spacing
import com.pointlessapps.songbook.utils.add
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@Composable
internal fun PreviewSongLayout(
    title: String,
    artist: String,
    sections: List<Section>,
    textScale: Int,
    keyOffset: Int,
    onTextScaleChanged: (Int) -> Unit,
    displayMode: DisplayMode = DisplayMode.Inline,
    wrapMode: WrapMode = WrapMode.Wrap,
    previousSongTitle: String? = null,
    nextSongTitle: String? = null,
    onPreviousSongRequested: () -> Unit = {},
    onNextSongRequested: () -> Unit = {},
    paddingValues: PaddingValues = PaddingValues(),
) {
    var currentTextScale by remember(textScale) { mutableStateOf(textScale) }
    var chordDetailsDialogData by rememberSaveable { mutableStateOf<String?>(null) }

    val interactionSource = remember { MutableInteractionSource() }
    val isPinchingToZoom by interactionSource.collectIsPinchedToZoomAsState()

    val hapticFeedback = LocalHapticFeedback.current
    val horizontalScrollState = rememberScrollState()
    val overscrollEffect = rememberHorizonalOverscrolledEffect(
        threshold = 300f,
        onOverscrolled = { finished, side ->
            if (finished) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                if (side == Direction.FromStart) onPreviousSongRequested()
                else onNextSongRequested()
            } else {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
            }
        },
        effectNode = rememberSwipeEffectNode(
            previousSongTitle = previousSongTitle
                ?: stringResource(Res.string.lyrics_no_previous_song),
            nextSongTitle = nextSongTitle ?: stringResource(Res.string.lyrics_no_next_song),
        ),
    )

    var didTransform by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(isPinchingToZoom) {
        if (isPinchingToZoom) {
            didTransform = true
        } else if (didTransform) {
            onTextScaleChanged(currentTextScale)
        }
    }

    PinchToZoomBox(
        modifier = Modifier
            .fillMaxSize()
            .scrollable(
                orientation = Orientation.Horizontal,
                state = rememberScrollableState { delta ->
                    if (wrapMode == WrapMode.NoWrap || displayMode.shouldShowSideBySide) {
                        -horizontalScrollState.dispatchRawDelta(-delta)
                    } else {
                        0f
                    }
                },
                overscrollEffect = overscrollEffect,
                enabled = !isPinchingToZoom,
            )
            .overscroll(overscrollEffect),
        interactionSource = interactionSource,
        onZoomChanged = {
            currentTextScale = (currentTextScale * it).roundToInt().coerceIn(MIN_ZOOM, MAX_ZOOM)
        },
    ) {
        LazyColumn(
            userScrollEnabled = !isPinchingToZoom,
            modifier = Modifier
                .widthIn(max = MAX_WIDTH)
                .fillMaxSize(),
            contentPadding = paddingValues.add(
                vertical = MaterialTheme.spacing.huge,
            ),
            verticalArrangement = Arrangement.spacedBy(
                space = MaterialTheme.spacing.small,
                alignment = Alignment.Top,
            ),
            horizontalAlignment = Alignment.Start,
        ) {
            item(key = "header") {
                SongHeader(
                    title = title,
                    artist = artist,
                    modifier = Modifier.padding(horizontal = MaterialTheme.spacing.huge),
                )
            }

            item { Spacer(Modifier.height(MaterialTheme.spacing.extraSmall)) }

            item(key = "sections") {
                LyricsSections(
                    modifier = Modifier.padding(horizontal = MaterialTheme.spacing.huge),
                    sections = sections,
                    textScale = currentTextScale,
                    keyOffset = keyOffset,
                    displayMode = displayMode,
                    wrapMode = wrapMode,
                    editable = false,
                    onChordClicked = { _, chord, _ -> chordDetailsDialogData = chord.value },
                    userScrollEnabled = false,
                    scrollState = horizontalScrollState,
                    overscrollEffect = overscrollEffect,
                )
            }
        }

        TextScaleOverlay(
            show = isPinchingToZoom,
            textScale = currentTextScale,
            modifier = Modifier.align(Alignment.Center),
        )
    }

    chordDetailsDialogData?.let { chord ->
        ChordDetailsDialog(
            chord = chord,
            keyOffset = keyOffset,
            onDismissRequest = { chordDetailsDialogData = null },
        )
    }
}

@Composable
private fun rememberSwipeEffectNode(
    previousSongTitle: String,
    nextSongTitle: String,
): OverscrolledEffectNode {
    val textMeasurer = rememberTextMeasurer()

    val density = LocalDensity.current
    val padding = with(density) { MaterialTheme.spacing.medium.toPx() }
    val backgroundColor = MaterialTheme.colorScheme.background
    val contentColor = MaterialTheme.colorScheme.onBackground

    val textConstrains = Constraints(
        maxWidth = with(density) { 54.dp.roundToPx() },
    )

    val previousSongTextLayout by rememberUpdatedState(
        textMeasurer.measure(
            text = previousSongTitle,
            style = MaterialTheme.typography.labelSmall.copy(
                textAlign = TextAlign.Center,
            ),
            constraints = textConstrains,
        ),
    )
    val nextSongTextLayout by rememberUpdatedState(
        textMeasurer.measure(
            text = nextSongTitle,
            style = MaterialTheme.typography.labelSmall.copy(
                textAlign = TextAlign.Center,
            ),
            constraints = textConstrains,
        ),
    )

    return remember {
        createOverscrolledEffectNode { currentProgress ->
            object : Modifier.Node(), DrawModifierNode {
                override fun ContentDrawScope.draw() {
                    val (offset, progress, side) = currentProgress()

                    translate(left = offset * 0.4f) {
                        this@draw.drawContent()
                    }

                    val fromStart = side == Direction.FromStart
                    val textLayout = if (fromStart) previousSongTextLayout else nextSongTextLayout
                    val containerSize = Size(
                        width = textLayout.size.width + 2 * padding,
                        height = size.height,
                    )

                    val tooltipOffset = offset * 0.2f
                    val x = if (fromStart) {
                        tooltipOffset - 50f
                    } else {
                        size.width - containerSize.width + tooltipOffset + 50f
                    }

                    drawRect(
                        color = backgroundColor.copy(alpha = progress * 0.7f),
                        topLeft = Offset(x, 0f),
                        size = containerSize,
                    )

                    drawText(
                        textLayoutResult = textLayout,
                        color = contentColor,
                        alpha = progress,
                        topLeft = Offset(
                            x = x + padding,
                            y = (size.height - textLayout.size.height) / 2f,
                        ),
                    )
                }
            }
        }
    }
}

private val MAX_WIDTH = 800.dp
