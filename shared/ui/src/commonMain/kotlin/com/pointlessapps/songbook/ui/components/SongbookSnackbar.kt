package com.pointlessapps.songbook.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pointlessapps.songbook.ui.theme.spacing
import org.jetbrains.compose.resources.DrawableResource

@Composable
fun SongbookSnackbar(
    message: String,
    icon: DrawableResource?,
    actionLabel: String?,
    actionCallback: (() -> Unit)?,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.medium),
        onClick = onDismissRequest,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.inverseSurface,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = MaterialTheme.spacing.large,
                vertical = MaterialTheme.spacing.medium,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                space = MaterialTheme.spacing.medium,
                alignment = Alignment.CenterHorizontally,
            ),
        ) {
            if (icon != null) {
                SongbookIcon(
                    icon = icon,
                    modifier = Modifier.size(ICON_SIZE),
                    iconStyle = defaultSongbookIconStyle().copy(
                        tint = MaterialTheme.colorScheme.inverseOnSurface,
                    ),
                )
            }

            SongbookText(
                modifier = Modifier.weight(1f),
                text = message,
                textStyle = defaultSongbookTextStyle().copy(
                    textColor = MaterialTheme.colorScheme.inverseOnSurface,
                    typography = MaterialTheme.typography.bodyMedium,
                ),
            )

            if (actionLabel != null) {
                SongbookText(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .clickable { actionCallback?.invoke() }
                        .padding(MaterialTheme.spacing.small),
                    text = actionLabel.uppercase(),
                    textStyle = defaultSongbookTextStyle().copy(
                        textColor = MaterialTheme.colorScheme.inverseOnSurface,
                        typography = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    ),
                )
            }
        }
    }
}

private val ICON_SIZE = 24.dp
