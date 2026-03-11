package com.pointlessapps.songbook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.pointlessapps.songbook.ui.theme.spacing
import org.jetbrains.compose.resources.stringResource
import com.pointlessapps.songbook.shared.generated.resources.Res
import com.pointlessapps.songbook.shared.generated.resources.common_app_name

@Composable
fun LyricFlowHeader(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = MaterialTheme.spacing.large,
                vertical = MaterialTheme.spacing.medium
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(MaterialTheme.spacing.large)
            )
            Text(
                text = stringResource(Res.string.common_app_name),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        // Mock User Profile
        Box(
            modifier = Modifier
                .size(MaterialTheme.spacing.huge)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.outline)
                .align(Alignment.CenterEnd)
        )
    }
}
