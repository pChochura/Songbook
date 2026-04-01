package com.pointlessapps.songbook.library.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.pointlessapps.songbook.LocalNavigator
import com.pointlessapps.songbook.core.song.model.Section
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.common_back
import com.pointlessapps.songbook.ui.PreviewSongLayout
import com.pointlessapps.songbook.ui.components.SongbookIconButton
import com.pointlessapps.songbook.ui.components.SongbookScaffoldLayout
import com.pointlessapps.songbook.ui.components.defaultSongbookIconButtonStyle
import com.pointlessapps.songbook.ui.theme.IconClose
import com.pointlessapps.songbook.ui.theme.spacing

@Composable
internal fun PreviewSongScreen(
    title: String,
    artist: String,
    sections: List<Section>,
) {
    val navigator = LocalNavigator.current
    var textScale by remember { mutableIntStateOf(100) }

    SongbookScaffoldLayout(
        topBar = @Composable { Spacer(Modifier.statusBarsPadding()) },
    ) { paddingValues ->
        PreviewSongLayout(
            title = title,
            artist = artist,
            sections = sections,
            textScale = textScale,
            onTextScaleChanged = { textScale = it },
            paddingValues = paddingValues,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(all = MaterialTheme.spacing.huge),
            contentAlignment = Alignment.TopEnd,
        ) {
            SongbookIconButton(
                icon = IconClose,
                tooltipLabel = Res.string.common_back,
                onClick = { navigator.navigateBack() },
                iconButtonStyle = defaultSongbookIconButtonStyle().copy(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        .copy(alpha = 0.7f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    outlineColor = Color.Transparent,
                ),
            )
        }
    }
}
