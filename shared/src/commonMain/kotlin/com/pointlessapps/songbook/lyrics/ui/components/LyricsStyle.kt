package com.pointlessapps.songbook.lyrics.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.pointlessapps.songbook.lyrics.WrapMode
import com.pointlessapps.songbook.ui.components.SongbookChipStyle
import com.pointlessapps.songbook.ui.components.SongbookTextStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookChipStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle

@Composable
internal fun calculateLineTextStyle(
    textScale: Int,
    wrapMode: WrapMode,
): SongbookTextStyle {
    val textScaleFloat = textScale / 100f
    val fontSize = MaterialTheme.typography.bodyLarge.fontSize * textScaleFloat

    return defaultSongbookTextStyle().copy(
        softWrap = wrapMode == WrapMode.Wrap,
        textColor = MaterialTheme.colorScheme.onSurface,
        typography = MaterialTheme.typography.bodyLarge.copy(
            fontSize = fontSize,
            lineHeight = fontSize * 1.5f,
        ),
    )
}

@Composable
internal fun calculateChordChipStyle(textScale: Int): SongbookChipStyle {
    val textScaleFloat = textScale / 100f

    return defaultSongbookChipStyle().copy(
        containerColor = MaterialTheme.colorScheme.primary,
        labelColor = MaterialTheme.colorScheme.onPrimary,
        outlineColor = Color.Transparent,
        labelTypography = MaterialTheme.typography.labelLarge.copy(
            fontSize = MaterialTheme.typography.labelLarge.fontSize * textScaleFloat,
        ),
    )
}
