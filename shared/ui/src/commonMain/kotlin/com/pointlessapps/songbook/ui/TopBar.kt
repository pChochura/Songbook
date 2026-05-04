package com.pointlessapps.songbook.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pointlessapps.songbook.shared.ui.Res
import com.pointlessapps.songbook.shared.ui.common_back
import com.pointlessapps.songbook.shared.ui.common_menu
import com.pointlessapps.songbook.ui.components.Position
import com.pointlessapps.songbook.ui.components.SongbookIconButton
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.defaultSongbookIconButtonStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.IconArrowLeft
import com.pointlessapps.songbook.ui.theme.IconMoveHandle
import com.pointlessapps.songbook.ui.theme.spacing
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

@Composable
fun TopBar(
    leftButton: (@Composable () -> Unit)?,
    rightButton: (@Composable () -> Unit)?,
    title: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f))
            .statusBarsPadding()
            .padding(
                horizontal = MaterialTheme.spacing.large,
                vertical = MaterialTheme.spacing.medium,
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leftButton?.invoke() ?: run {
            Box(Modifier.size(TOP_BAR_ICON_SIZE))
        }

        Box(
            modifier = Modifier
                .heightIn(min = TOP_BAR_ICON_SIZE)
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            SongbookText(
                text = title,
                textStyle = defaultSongbookTextStyle().copy(
                    textColor = MaterialTheme.colorScheme.onSurface,
                    typography = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                ),
            )
        }

        rightButton?.invoke() ?: run {
            Box(Modifier.size(TOP_BAR_ICON_SIZE))
        }
    }
}

@Composable
fun TopBarButton(
    enabled: Boolean,
    icon: DrawableResource,
    tooltip: StringResource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SongbookIconButton(
        modifier = modifier
            .size(TOP_BAR_ICON_SIZE)
            .padding(MaterialTheme.spacing.extraSmall),
        icon = icon,
        tooltipLabel = tooltip,
        onClick = onClick,
        iconButtonStyle = defaultSongbookIconButtonStyle().copy(
            enabled = enabled,
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            outlineColor = Color.Transparent,
            disabledOutlineColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContentColor = MaterialTheme.colorScheme.onSurface,
            tooltipPosition = Position.BELOW,
        ),
    )
}

@Composable
fun MenuTopBarButton(onClick: () -> Unit) = TopBarButton(
    enabled = true,
    icon = IconMoveHandle,
    tooltip = Res.string.common_menu,
    onClick = onClick,
)

@Composable
fun BackTopBarButton(onClick: () -> Unit) = TopBarButton(
    enabled = true,
    icon = IconArrowLeft,
    tooltip = Res.string.common_back,
    onClick = onClick,
)

val TOP_BAR_ICON_SIZE = 36.dp
