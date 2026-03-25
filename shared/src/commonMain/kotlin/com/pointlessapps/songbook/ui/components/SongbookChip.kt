package com.pointlessapps.songbook.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.movableContentOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.pointlessapps.songbook.ui.theme.IconDone
import com.pointlessapps.songbook.ui.theme.spacing
import org.jetbrains.compose.resources.DrawableResource

@Composable
fun SongbookChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    chipStyle: SongbookChipStyle = defaultSongbookChipStyle(),
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(
                if (isSelected) {
                    chipStyle.selectedContainerColor
                } else {
                    chipStyle.containerColor
                },
            )
            .border(
                width = CHIP_BORDER_WIDTH,
                color = if (isSelected) {
                    chipStyle.selectedOutlineColor
                } else {
                    chipStyle.outlineColor
                },
                shape = CircleShape,
            )
            .clickable(
                onClick = onClick,
                role = Role.RadioButton,
            )
            .padding(
                horizontal = MaterialTheme.spacing.small,
                vertical = MaterialTheme.spacing.extraSmall,
            )
            .animateContentSize(),
        horizontalArrangement = Arrangement.spacedBy(
            space = MaterialTheme.spacing.extraSmall,
            alignment = Alignment.CenterHorizontally,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val iconContent = movableContentOf {
            SongbookIcon(
                modifier = Modifier.size(CHIP_ICON_SIZE),
                iconRes = chipStyle.iconRes,
                iconStyle = defaultSongbookIconStyle().copy(
                    tint = chipStyle.iconColor,
                ),
            )
        }

        if (isSelected && chipStyle.iconAlignment == Alignment.Start) {
            iconContent()
        }

        SongbookText(
            modifier = Modifier
                .sizeIn(minHeight = CHIP_ICON_SIZE),
            text = label,
            textStyle = defaultSongbookTextStyle().copy(
                typography = MaterialTheme.typography.labelLarge,
                textColor = if (isSelected) {
                    chipStyle.selectedLabelColor
                } else {
                    chipStyle.labelColor
                },
            ),
        )

        if (isSelected && chipStyle.iconAlignment == Alignment.End) {
            iconContent()
        }
    }
}

@Composable
fun defaultSongbookChipStyle() = SongbookChipStyle(
    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
    outlineColor = MaterialTheme.colorScheme.outlineVariant,
    selectedOutlineColor = MaterialTheme.colorScheme.secondaryContainer,
    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
    iconColor = MaterialTheme.colorScheme.onSecondaryContainer,
    iconRes = IconDone,
    iconAlignment = Alignment.Start,
)

data class SongbookChipStyle(
    val containerColor: Color,
    val selectedContainerColor: Color,
    val outlineColor: Color,
    val selectedOutlineColor: Color,
    val labelColor: Color,
    val selectedLabelColor: Color,
    val iconColor: Color,
    val iconRes: DrawableResource,
    val iconAlignment: Alignment.Horizontal,
)

private val CHIP_BORDER_WIDTH = 1.dp
private val CHIP_ICON_SIZE = 16.dp
