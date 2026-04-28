package com.pointlessapps.songbook.library.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.pointlessapps.songbook.shared.ui.Res
import com.pointlessapps.songbook.shared.ui.library_add_setlist
import com.pointlessapps.songbook.ui.components.SongbookCard
import com.pointlessapps.songbook.ui.components.SongbookIcon
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.dashedSongbookCardStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookIconStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.IconAddFolder
import com.pointlessapps.songbook.ui.theme.spacing
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AddSetlistCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SongbookCard(
        modifier = modifier,
        onClick = onClick,
        cardStyle = dashedSongbookCardStyle(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(MaterialTheme.spacing.large),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SongbookIcon(
                icon = IconAddFolder,
                iconStyle = defaultSongbookIconStyle().copy(
                    tint = MaterialTheme.colorScheme.primary,
                ),
            )

            SongbookText(
                text = stringResource(Res.string.library_add_setlist),
                textStyle = defaultSongbookTextStyle().copy(
                    textColor = MaterialTheme.colorScheme.onSurface,
                    typography = MaterialTheme.typography.titleMedium,
                ),
            )
        }
    }
}
