package com.pointlessapps.songbook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.pointlessapps.songbook.shared.generated.resources.song_control_bar_mode_both
import com.pointlessapps.songbook.shared.generated.resources.song_control_bar_mode_side_by_side
import com.pointlessapps.songbook.shared.generated.resources.song_control_bar_mode_standard
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

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
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
                        if (transposition >= 0) "+$transposition" else transposition.toString(),
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Row(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
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
                        tint = MaterialTheme.colorScheme.onSurface,
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
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(spacing.large),
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(spacing.large),
                )
                .padding(spacing.extraSmall),
        ) {
            val modes = listOf(
                Res.string.song_control_bar_mode_standard,
                Res.string.song_control_bar_mode_side_by_side,
                Res.string.song_control_bar_mode_both,
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
