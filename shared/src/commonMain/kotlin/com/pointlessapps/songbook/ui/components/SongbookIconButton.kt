package com.pointlessapps.songbook.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun SongbookIconButton(
    icon: DrawableResource,
    tooltipLabel: StringResource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: () -> Unit = {},
    iconButtonStyle: SongbookIconButtonStyle = defaultSongbookIconButtonStyle(),
) {
    SongbookTooltip(
        position = iconButtonStyle.tooltipPosition,
        contentDescription = tooltipLabel,
    ) {
        SongbookIcon(
            modifier = Modifier
                .clip(iconButtonStyle.shape)
                .border(
                    width = ICON_BUTTON_BORDER_WIDTH,
                    color = if (iconButtonStyle.enabled) {
                        iconButtonStyle.outlineColor
                    } else {
                        iconButtonStyle.disabledOutlineColor
                    },
                    shape = iconButtonStyle.shape,
                )
                .background(
                    if (iconButtonStyle.enabled) {
                        iconButtonStyle.containerColor
                    } else {
                        iconButtonStyle.disabledContainerColor
                    },
                )
                .semantics { role = Role.Button }
                .combinedClickable(
                    enabled = iconButtonStyle.enabled,
                    onClick = onClick,
                    onLongClick = onLongClick,
                )
                .then(modifier),
            icon = icon,
            iconStyle = defaultSongbookIconStyle().copy(
                tint = if (iconButtonStyle.enabled) {
                    iconButtonStyle.contentColor
                } else {
                    iconButtonStyle.disabledContentColor
                },
            ),
        )
    }
}

@Composable
internal fun defaultSongbookIconButtonStyle() = SongbookIconButtonStyle(
    containerColor = MaterialTheme.colorScheme.primary,
    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
    contentColor = MaterialTheme.colorScheme.onPrimary,
    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
    outlineColor = MaterialTheme.colorScheme.primary,
    disabledOutlineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
    shape = CircleShape,
    enabled = true,
    tooltipPosition = Position.ABOVE,
)

internal data class SongbookIconButtonStyle(
    val containerColor: Color,
    val disabledContainerColor: Color,
    val contentColor: Color,
    val disabledContentColor: Color,
    val outlineColor: Color,
    val disabledOutlineColor: Color,
    val shape: Shape,
    val enabled: Boolean,
    val tooltipPosition: Position,
)

private val ICON_BUTTON_BORDER_WIDTH = 1.dp
