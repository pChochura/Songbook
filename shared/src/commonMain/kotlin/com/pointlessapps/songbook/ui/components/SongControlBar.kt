package com.pointlessapps.songbook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pointlessapps.songbook.shared.generated.resources.*
import com.pointlessapps.songbook.ui.theme.spacing
import org.jetbrains.compose.resources.stringResource

@Composable
fun SongControlBar(
    transposition: Int,
    onTransposeUp: () -> Unit,
    onTransposeDown: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.spacing
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colorScheme.surface)
            .padding(
                horizontal = spacing.large,
                vertical = spacing.medium,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.large),
        ) {
            TextButton(onClick = onReset) {
                Text(
                    text = stringResource(
                        Res.string.song_control_bar_key,
                        if (transposition >= 0) "+$transposition" else transposition.toString()
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.onSurface,
                )
            }

            Row(
                modifier = Modifier
                    .background(
                        color = colorScheme.outline.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(spacing.small),
                    )
                    .padding(spacing.extraSmall),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onTransposeDown,
                    modifier = Modifier.size(spacing.extraLarge),
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = null,
                        tint = colorScheme.onSurface,
                        modifier = Modifier.size(spacing.large),
                    )
                }
                IconButton(
                    onClick = onTransposeUp,
                    modifier = Modifier.size(spacing.extraLarge),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = colorScheme.onSurface,
                        modifier = Modifier.size(spacing.large),
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.medium),
            ) {
                Text(
                    text = stringResource(Res.string.song_control_bar_text_size_label),
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.onSurface,
                )
                Slider(
                    value = 0.5f,
                    onValueChange = {},
                    modifier = Modifier.width(150.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = colorScheme.onSurface,
                        activeTrackColor = colorScheme.primary,
                    ),
                )
            }
        }

        Row(
            modifier = Modifier
                .background(
                    color = colorScheme.outline.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(spacing.large),
                )
                .padding(spacing.extraSmall),
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
                            colorScheme.outline.copy(alpha = 0.2f)
                        } else {
                            Color.Transparent
                        },
                        contentColor = colorScheme.onSurface,
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(
                        horizontal = spacing.large,
                        vertical = spacing.small,
                    ),
                    modifier = Modifier.height(spacing.huge),
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
