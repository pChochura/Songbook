package com.pointlessapps.songbook.search.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.search_no_items_found
import com.pointlessapps.songbook.ui.components.SongbookIcon
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.defaultSongbookIconStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.IconLibrary
import com.pointlessapps.songbook.ui.theme.spacing
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun EmptyListCard(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(MaterialTheme.spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
    ) {
        SongbookIcon(
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(MaterialTheme.spacing.medium)
                .size(36.dp),
            icon = IconLibrary,
            iconStyle = defaultSongbookIconStyle().copy(
                tint = MaterialTheme.colorScheme.onSurface,
            ),
        )

        SongbookText(
            text = stringResource(Res.string.search_no_items_found),
            textStyle = defaultSongbookTextStyle().copy(
                textColor = MaterialTheme.colorScheme.onSurface,
                typography = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            ),
        )
    }
}
