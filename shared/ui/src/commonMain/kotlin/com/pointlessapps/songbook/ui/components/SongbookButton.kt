package com.pointlessapps.songbook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.movableContentOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.pointlessapps.songbook.ui.theme.spacing
import org.jetbrains.compose.resources.DrawableResource

@Composable
fun SongbookButton(
    label: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    onLongClick: () -> Unit = {},
    buttonStyle: SongbookButtonStyle = defaultSongbookButtonStyle(),
) {
    Row(
        modifier = modifier
            .defaultMinSize(
                minWidth = ButtonDefaults.MinWidth,
                minHeight = ButtonDefaults.MinHeight,
            )
            .clip(buttonStyle.shape)
            .background(
                color = if (buttonStyle.enabled) {
                    buttonStyle.containerColor
                } else {
                    buttonStyle.disabledContainerColor
                },
                shape = buttonStyle.shape,
            )
            .combinedClickable(
                enabled = buttonStyle.enabled,
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val content = movableContentOf {
            if (buttonStyle.icon != null) {
                SongbookIcon(
                    icon = buttonStyle.icon,
                    modifier = Modifier
                        .size(ICON_SIZE)
                        .then(iconModifier),
                    iconStyle = defaultSongbookIconStyle().copy(
                        tint = if (buttonStyle.enabled) {
                            buttonStyle.textStyle.textColor
                        } else {
                            buttonStyle.textStyle.disabledTextColor
                        },
                    ),
                )
            }

            if (label != null) {
                SongbookText(
                    text = label,
                    textStyle = buttonStyle.textStyle.copy(
                        textColor = if (buttonStyle.enabled) {
                            buttonStyle.textStyle.textColor
                        } else {
                            buttonStyle.textStyle.disabledTextColor
                        },
                    ),
                )
            }
        }

        when (buttonStyle.orientation) {
            SongbookButtonOrientation.Vertical -> Column(
                modifier = Modifier.padding(
                    vertical = MaterialTheme.spacing.medium,
                    horizontal = MaterialTheme.spacing.large,
                ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                content = { content() },
            )

            SongbookButtonOrientation.Horizontal -> Row(
                modifier = Modifier.padding(
                    vertical = MaterialTheme.spacing.medium,
                    horizontal = MaterialTheme.spacing.large,
                ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                content = { content() },
            )
        }
    }
}

@Composable
fun defaultSongbookButtonStyle() = SongbookButtonStyle(
    containerColor = MaterialTheme.colorScheme.primary,
    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
    shape = CircleShape,
    icon = null,
    orientation = SongbookButtonOrientation.Horizontal,
    textStyle = defaultSongbookButtonTextStyle(),
    enabled = true,
)

@Composable
fun defaultSongbookButtonTextStyle() = defaultSongbookTextStyle().copy(
    textColor = MaterialTheme.colorScheme.onPrimary,
    typography = MaterialTheme.typography.labelLarge,
)

@Stable
data class SongbookButtonStyle(
    val containerColor: Color,
    val disabledContainerColor: Color,
    val shape: Shape,
    val icon: DrawableResource?,
    val orientation: SongbookButtonOrientation,
    val textStyle: SongbookTextStyle,
    val enabled: Boolean,
)

enum class SongbookButtonOrientation { Vertical, Horizontal }

private val ICON_SIZE = 24.dp
