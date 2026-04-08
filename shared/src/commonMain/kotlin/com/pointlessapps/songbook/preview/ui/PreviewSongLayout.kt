package com.pointlessapps.songbook.preview.ui

import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pointlessapps.songbook.core.song.model.Section
import com.pointlessapps.songbook.lyrics.DisplayMode
import com.pointlessapps.songbook.lyrics.LyricsViewModel.Companion.MAX_ZOOM
import com.pointlessapps.songbook.lyrics.LyricsViewModel.Companion.MIN_ZOOM
import com.pointlessapps.songbook.lyrics.WrapMode
import com.pointlessapps.songbook.lyrics.ui.components.LyricsSections
import com.pointlessapps.songbook.lyrics.ui.components.SongHeader
import com.pointlessapps.songbook.lyrics.ui.components.TextScaleOverlay
import com.pointlessapps.songbook.preview.ui.components.dialogs.ChordDetailsDialog
import com.pointlessapps.songbook.ui.theme.spacing
import com.pointlessapps.songbook.utils.add
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
    wrapMode: WrapMode = WrapMode.NoWrap,
    paddingValues: PaddingValues = PaddingValues(),
) {
    var currentTextScale by remember { mutableStateOf(textScale) }
    var chordDetailsDialogData by rememberSaveable { mutableStateOf<String?>(null) }
    val transformableState = rememberTransformableState { zoomChange, _, _ ->
        currentTextScale = (currentTextScale * zoomChange)
            .roundToInt()
            .coerceIn(MIN_ZOOM, MAX_ZOOM)
    }

    LaunchedEffect(transformableState.isTransformInProgress) {
        if (!transformableState.isTransformInProgress) {
            onTextScaleChanged(currentTextScale)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .transformable(state = transformableState, canPan = { false }),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            userScrollEnabled = !transformableState.isTransformInProgress,
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
                    modifier = Modifier.padding(
                        horizontal = MaterialTheme.spacing.huge,
                    ),
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
                    onChordClicked = { chordDetailsDialogData = it },
                )
            }
        }

        TextScaleOverlay(
            show = transformableState.isTransformInProgress,
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

private val MAX_WIDTH = 800.dp
