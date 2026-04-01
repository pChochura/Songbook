package com.pointlessapps.songbook.ui

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pointlessapps.songbook.core.song.model.Section
import com.pointlessapps.songbook.lyrics.LyricsMode
import com.pointlessapps.songbook.lyrics.ui.components.LyricsSections
import com.pointlessapps.songbook.lyrics.ui.components.SongHeader
import com.pointlessapps.songbook.lyrics.ui.components.TextScaleOverlay
import com.pointlessapps.songbook.ui.theme.spacing
import com.pointlessapps.songbook.utils.add
import kotlin.math.roundToInt

@Composable
internal fun PreviewSongLayout(
    title: String,
    artist: String,
    sections: List<Section>,
    textScale: Int,
    onTextScaleChanged: (Int) -> Unit,
    mode: LyricsMode = LyricsMode.Inline,
    paddingValues: PaddingValues = PaddingValues(),
) {
    val transformableState = rememberTransformableState { zoomChange, _, _ ->
        onTextScaleChanged((textScale * zoomChange).roundToInt())
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .transformable(state = transformableState, canPan = { false }),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
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
                    sections = sections,
                    textScale = textScale,
                    mode = mode,
                    modifier = Modifier.padding(
                        horizontal = MaterialTheme.spacing.huge,
                    ),
                )
            }
        }

        TextScaleOverlay(
            show = transformableState.isTransformInProgress,
            textScale = textScale,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

private val MAX_WIDTH = 800.dp
