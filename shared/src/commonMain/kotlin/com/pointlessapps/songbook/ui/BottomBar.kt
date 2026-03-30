package com.pointlessapps.songbook.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.pointlessapps.songbook.Route
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.library_header_title
import com.pointlessapps.songbook.ui.components.SongbookIcon
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.defaultSongbookIconStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.DEFAULT_BORDER_WIDTH
import com.pointlessapps.songbook.ui.theme.IconNote
import com.pointlessapps.songbook.ui.theme.IconPlus
import com.pointlessapps.songbook.ui.theme.IconSearch
import com.pointlessapps.songbook.ui.theme.spacing
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BottomBar(
    currentRoute: () -> Route?,
    onNavigateTo: (Route) -> Unit,
    onLongClicked: (Route) -> Unit,
) {
    val currentRoute = remember { currentRoute() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = MaterialTheme.spacing.extraLarge),
        horizontalArrangement = Arrangement.spacedBy(
            space = MaterialTheme.spacing.large,
            alignment = Alignment.CenterHorizontally,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BottomBarBackground {
            BottomBarButton(
                bottomBarButton = when {
                    currentRoute is Route.Library -> BottomBarButton.Active(
                        icon = IconNote,
                        title = stringResource(Res.string.library_header_title),
                    )

                    else -> BottomBarButton.Empty(icon = IconNote)
                },
                isEnabled = true,
                onClicked = { onNavigateTo(Route.Library()) },
                onLongClicked = { onLongClicked(Route.Library()) },
            )
            BottomBarButton(
                bottomBarButton = BottomBarButton.Empty(icon = IconSearch),
                isEnabled = true,
                onClicked = { onNavigateTo(Route.Search) },
                onLongClicked = { onLongClicked(Route.Search) },
            )
        }

        BottomBarSupportingButton(
            icon = IconPlus,
            onClicked = { onNavigateTo(Route.ImportSong) },
            onLongClicked = { onLongClicked(Route.ImportSong) },
        )
    }
}

@Composable
internal fun BottomBarBackground(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(
                width = DEFAULT_BORDER_WIDTH,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = CircleShape,
            )
            .padding(MaterialTheme.spacing.medium),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
internal fun BottomBarButton(
    bottomBarButton: BottomBarButton,
    isEnabled: Boolean,
    onClicked: () -> Unit,
    onLongClicked: () -> Unit,
) {
    AnimatedContent(bottomBarButton is BottomBarButton.Active) { isActive ->
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .then(
                    if (isActive) {
                        Modifier.background(MaterialTheme.colorScheme.primary)
                    } else {
                        Modifier
                    },
                )
                .combinedClickable(
                    enabled = isEnabled,
                    role = Role.Button,
                    onClick = onClicked,
                    onLongClick = onLongClicked,
                )
                .padding(
                    horizontal = MaterialTheme.spacing.large,
                    vertical = MaterialTheme.spacing.medium,
                ),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SongbookIcon(
                iconRes = bottomBarButton.icon,
                modifier = Modifier.size(FAB_ICON_SIZE),
                iconStyle = defaultSongbookIconStyle().copy(
                    tint = if (isActive) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }.copy(alpha = if (isEnabled) 1f else 0.3f),
                ),
            )

            if (isActive) {
                AnimatedContent((bottomBarButton as? BottomBarButton.Active)?.title.orEmpty()) { title ->
                    SongbookText(
                        text = title,
                        textStyle = defaultSongbookTextStyle().copy(
                            textColor = MaterialTheme.colorScheme.onPrimary,
                            typography = MaterialTheme.typography.labelMedium,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
internal fun BottomBarSupportingButton(
    icon: DrawableResource,
    onClicked: () -> Unit,
    onLongClicked: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondary)
            .combinedClickable(
                role = Role.Button,
                onClick = onClicked,
                onLongClick = onLongClicked,
            )
            .padding(MaterialTheme.spacing.large),
        contentAlignment = Alignment.Center,
    ) {
        SongbookIcon(
            iconRes = icon,
            modifier = Modifier.size(FAB_ICON_SIZE),
            iconStyle = defaultSongbookIconStyle().copy(
                tint = MaterialTheme.colorScheme.onSecondary,
            ),
        )
    }
}

internal sealed class BottomBarButton(
    open val icon: DrawableResource,
) {
    data class Empty(override val icon: DrawableResource) : BottomBarButton(icon)
    data class Active(override val icon: DrawableResource, val title: String) :
        BottomBarButton(icon)
}

private val FAB_ICON_SIZE = 24.dp
