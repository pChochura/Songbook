package com.pointlessapps.songbook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pointlessapps.songbook.shared.generated.resources.Res
import com.pointlessapps.songbook.shared.generated.resources.song_control_bar_key
import com.pointlessapps.songbook.shared.generated.resources.song_control_bar_mode_side_by_side
import com.pointlessapps.songbook.shared.generated.resources.song_control_bar_mode_standard
import com.pointlessapps.songbook.shared.generated.resources.song_control_bar_mode_text_only
import com.pointlessapps.songbook.shared.generated.resources.song_control_bar_text_size_label
import com.pointlessapps.songbook.ui.theme.spacing
import org.jetbrains.compose.resources.stringResource

@Composable
fun SongControlBar(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(
                horizontal = MaterialTheme.spacing.large,
                vertical = MaterialTheme.spacing.medium,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
        ) {
            Text(
                text = stringResource(Res.string.song_control_bar_key, "+0"),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Row(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(MaterialTheme.spacing.small),
                    )
                    .padding(MaterialTheme.spacing.extraSmall),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = { },
                    modifier = Modifier.size(MaterialTheme.spacing.extraLarge),
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(MaterialTheme.spacing.large),
                    )
                }
                IconButton(
                    onClick = { },
                    modifier = Modifier.size(MaterialTheme.spacing.extraLarge),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(MaterialTheme.spacing.large),
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
            ) {
                Text(
                    text = stringResource(Res.string.song_control_bar_text_size_label),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Slider(
                    value = 0.5f,
                    onValueChange = {},
                    modifier = Modifier.width(150.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.onSurface,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                    ),
                )
            }
        }

        Row(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(MaterialTheme.spacing.large),
                )
                .padding(MaterialTheme.spacing.extraSmall),
        ) {
            val modes = listOf(
                Res.string.song_control_bar_mode_standard,
                Res.string.song_control_bar_mode_side_by_side,
                Res.string.song_control_bar_mode_text_only,
            )
            var selectedMode by remember { mutableStateOf(modes[0]) }
            modes.forEach { mode ->
                TextButton(
                    onClick = { selectedMode = mode },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = if (selectedMode == mode) {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        } else {
                            Color.Transparent
                        },
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(
                        horizontal = MaterialTheme.spacing.large,
                        vertical = MaterialTheme.spacing.small,
                    ),
                    modifier = Modifier.height(MaterialTheme.spacing.huge),
                ) {
                    Text(
                        text = stringResource(mode),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}
