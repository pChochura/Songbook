package com.pointlessapps.songbook.library.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.common_show_all
import com.pointlessapps.songbook.ui.components.SongbookIconButton
import com.pointlessapps.songbook.ui.components.defaultSongbookIconButtonStyle
import com.pointlessapps.songbook.ui.theme.IconArrowRight
import com.pointlessapps.songbook.ui.theme.spacing

@Composable
internal fun ShowMoreButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SongbookIconButton(
        icon = IconArrowRight,
        tooltipLabel = Res.string.common_show_all,
        onClick = onClick,
        modifier = modifier.padding(MaterialTheme.spacing.medium),
        iconButtonStyle = defaultSongbookIconButtonStyle().copy(
            outlineColor = Color.Transparent,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    )
}
