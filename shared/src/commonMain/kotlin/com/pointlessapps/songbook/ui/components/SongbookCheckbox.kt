package com.pointlessapps.songbook.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.pointlessapps.songbook.ui.theme.DEFAULT_BORDER_WIDTH
import com.pointlessapps.songbook.ui.theme.IconDone
import com.pointlessapps.songbook.ui.theme.spacing
import org.jetbrains.compose.resources.DrawableResource

@Composable
internal fun SongbookCheckbox(
    label: String,
    checked: Boolean,
    onCheckChanged: () -> Unit,
    modifier: Modifier = Modifier,
    checkboxStyle: SongbookCheckboxStyle = defaultSongbookCheckboxStyle(),
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (checked) {
            checkboxStyle.selectedContainerColor
        } else {
            checkboxStyle.containerColor
        },
    )
    val borderColor by animateColorAsState(
        targetValue = if (checked) {
            checkboxStyle.selectedOutlineColor
        } else {
            checkboxStyle.outlineColor
        },
    )
    val iconAlpha by animateFloatAsState(if (checked) 1f else 0f)

    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .clickable(
                role = Role.Checkbox,
                onClick = onCheckChanged,
            )
            .padding(
                vertical = MaterialTheme.spacing.small,
                horizontal = MaterialTheme.spacing.extraSmall,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
    ) {
        SongbookText(
            modifier = Modifier.weight(1f),
            text = label,
            textStyle = defaultSongbookTextStyle().copy(
                typography = checkboxStyle.labelTypography,
                textColor = checkboxStyle.labelColor,
            ),
        )

        Box(
            modifier = Modifier
                .clip(checkboxStyle.shape)
                .background(
                    color = backgroundColor,
                    shape = checkboxStyle.shape,
                )
                .border(
                    width = DEFAULT_BORDER_WIDTH,
                    color = borderColor,
                    shape = checkboxStyle.shape,
                )
                .padding(MaterialTheme.spacing.extraSmall),
            contentAlignment = Alignment.Center,
        ) {
            SongbookIcon(
                modifier = Modifier
                    .size(CHECKBOX_ICON_SIZE)
                    .graphicsLayer { alpha = iconAlpha },
                icon = checkboxStyle.icon,
                iconStyle = defaultSongbookIconStyle().copy(
                    tint = checkboxStyle.iconColor,
                ),
            )
        }
    }
}

@Composable
internal fun defaultSongbookCheckboxStyle() = SongbookCheckboxStyle(
    shape = CircleShape,
    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    selectedContainerColor = MaterialTheme.colorScheme.primary,
    outlineColor = MaterialTheme.colorScheme.outlineVariant,
    selectedOutlineColor = MaterialTheme.colorScheme.primary,
    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    labelTypography = MaterialTheme.typography.labelSmall,
    iconColor = MaterialTheme.colorScheme.onPrimary,
    icon = IconDone,
)

internal data class SongbookCheckboxStyle(
    val shape: Shape,
    val containerColor: Color,
    val selectedContainerColor: Color,
    val outlineColor: Color,
    val selectedOutlineColor: Color,
    val labelColor: Color,
    val labelTypography: TextStyle,
    val iconColor: Color,
    val icon: DrawableResource,
)

private val CHECKBOX_ICON_SIZE = 12.dp
