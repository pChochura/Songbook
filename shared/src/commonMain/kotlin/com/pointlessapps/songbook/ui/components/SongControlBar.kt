package com.pointlessapps.songbook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pointlessapps.songbook.ui.theme.spacing
import org.jetbrains.compose.resources.stringResource
import com.pointlessapps.songbook.shared.generated.resources.*

@Composable
fun SongControlBar(
    modifier: Modifier = Modifier,
    onAutoScrollClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(
                horizontal = MaterialTheme.spacing.large,
                vertical = MaterialTheme.spacing.medium
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Key and Font Size Controls
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large)
        ) {
            Text(
                text = stringResource(Res.string.song_control_bar_key, "+0"),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(MaterialTheme.spacing.small))
                    .padding(MaterialTheme.spacing.extraSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { }, modifier = Modifier.size(MaterialTheme.spacing.extraLarge)) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(MaterialTheme.spacing.large)
                    )
                }
                IconButton(onClick = { }, modifier = Modifier.size(MaterialTheme.spacing.extraLarge)) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(MaterialTheme.spacing.large)
                    )
                }
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
            ) {
                Text(
                    text = stringResource(Res.string.song_control_bar_text_size_label),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Slider(
                    value = 0.5f,
                    onValueChange = {},
                    modifier = Modifier.width(150.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.onSurface,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        // View Mode Toggle (Segmented-like)
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(MaterialTheme.spacing.large))
                .padding(MaterialTheme.spacing.extraSmall)
        ) {
            val modes = listOf(
                Res.string.song_control_bar_mode_standard,
                Res.string.song_control_bar_mode_side_by_side,
                Res.string.song_control_bar_mode_text_only
            )
            var selectedMode by remember { mutableStateOf(modes[0]) }
            modes.forEach { mode ->
                TextButton(
                    onClick = { selectedMode = mode },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = if (selectedMode == mode) MaterialTheme.colorScheme.outline.copy(alpha = 0.2f) else Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(
                        horizontal = MaterialTheme.spacing.large,
                        vertical = MaterialTheme.spacing.small
                    ),
                    modifier = Modifier.height(MaterialTheme.spacing.huge)
                ) {
                    Text(stringResource(mode), style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // Action Buttons
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
        ) {
            TextButton(
                onClick = { },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(MaterialTheme.spacing.large)
                    )
                    Text(stringResource(Res.string.song_control_bar_reset), style = MaterialTheme.typography.labelMedium)
                }
            }
            
            Button(
                onClick = onAutoScrollClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                shape = RoundedCornerShape(MaterialTheme.spacing.small)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(MaterialTheme.spacing.large)
                    )
                    Text(stringResource(Res.string.song_control_bar_auto_scroll), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
