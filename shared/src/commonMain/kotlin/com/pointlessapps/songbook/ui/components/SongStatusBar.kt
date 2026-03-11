package com.pointlessapps.songbook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.pointlessapps.songbook.ui.theme.spacing
import org.jetbrains.compose.resources.stringResource
import com.pointlessapps.songbook.shared.generated.resources.Res
import com.pointlessapps.songbook.shared.generated.resources.*

@Composable
fun SongStatusBar(
    tempo: Int,
    capo: Int,
    isLiveMode: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(
                horizontal = MaterialTheme.spacing.large,
                vertical = MaterialTheme.spacing.small
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large)
        ) {
            Text(
                text = stringResource(Res.string.song_status_bar_tempo, tempo),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = stringResource(Res.string.song_status_bar_capo, capo),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large)
        ) {
            if (isLiveMode) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    Box(
                        modifier = Modifier
                            .size(MaterialTheme.spacing.medium)
                            .background(Color.Green, shape = CircleShape)
                    )
                    Text(
                        text = stringResource(Res.string.song_status_bar_live_mode),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Green
                    )
                }
            }
            
            IconButton(onClick = { }, modifier = Modifier.size(MaterialTheme.spacing.extraLarge)) {
                Icon(
                    Icons.Default.Fullscreen,
                    contentDescription = stringResource(Res.string.song_status_bar_fullscreen),
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(MaterialTheme.spacing.large)
                )
            }
        }
    }
}
