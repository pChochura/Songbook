package com.pointlessapps.songbook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pointlessapps.songbook.ui.theme.DEFAULT_BORDER_WIDTH
import com.pointlessapps.songbook.ui.theme.spacing
import org.jetbrains.compose.resources.DrawableResource
import kotlin.math.max

@Composable
fun SongbookDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    dialogStyle: SongbookDialogStyle = defaultSongbookDialogStyle(),
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = dialogStyle.dismissible.isDismissibleOnBackPress(),
            dismissOnClickOutside = dialogStyle.dismissible.isDismissibleOnClickOutside(),
        ),
    ) {
        Box {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.large)
                    .background(dialogStyle.containerColor)
                    .border(
                        width = DEFAULT_BORDER_WIDTH,
                        color = dialogStyle.outlineColor,
                        shape = MaterialTheme.shapes.large,
                    )
                    .then(
                        if (dialogStyle.scrollable) {
                            Modifier.verticalScroll(rememberScrollState())
                        } else {
                            Modifier
                        },
                    )
                    .padding(horizontal = MaterialTheme.spacing.huge)
                    .padding(
                        top = MaterialTheme.spacing.extraHuge,
                        bottom = MaterialTheme.spacing.huge,
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.huge),
            ) {
                SongbookText(
                    text = dialogStyle.label,
                    textStyle = defaultSongbookTextStyle().copy(
                        typography = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        textColor = dialogStyle.textColor,
                    ),
                )

                content()
            }

            if (dialogStyle.icon != null) {
                var iconSize by remember { mutableIntStateOf(0) }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .onSizeChanged { iconSize = max(it.height, it.width) }
                        .offset { IntOffset(x = 0, y = -iconSize / 2) }
                        .background(
                            color = dialogStyle.accentColor,
                            shape = CircleShape,
                        )
                        .border(
                            width = DEFAULT_BORDER_WIDTH,
                            color = dialogStyle.containerColor,
                            shape = CircleShape,
                        )
                        .padding(MaterialTheme.spacing.medium),
                    contentAlignment = Alignment.Center,
                ) {
                    SongbookIcon(
                        modifier = Modifier.size(DIALOG_ICON_SIZE),
                        icon = dialogStyle.icon,
                        iconStyle = defaultSongbookIconStyle().copy(tint = dialogStyle.iconColor),
                    )
                }
            }
        }
    }
}

@Composable
fun defaultSongbookDialogStyle() = SongbookDialogStyle(
    label = "",
    icon = null,
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    outlineColor = MaterialTheme.colorScheme.outlineVariant,
    accentColor = MaterialTheme.colorScheme.primary,
    textColor = MaterialTheme.colorScheme.onSurface,
    iconColor = MaterialTheme.colorScheme.onPrimary,
    scrollable = true,
    dismissible = SongbookDialogDismissible.Both,
)

data class SongbookDialogStyle(
    val label: String,
    val icon: DrawableResource?,
    val containerColor: Color,
    val outlineColor: Color,
    val accentColor: Color,
    val textColor: Color,
    val iconColor: Color,
    val scrollable: Boolean,
    val dismissible: SongbookDialogDismissible,
)

enum class SongbookDialogDismissible {
    None, OnBackPress, OnClickOutside, Both;

    fun isDismissibleOnBackPress() = this in setOf(
        OnBackPress, Both,
    )

    fun isDismissibleOnClickOutside() = this in setOf(
        OnClickOutside, Both,
    )
}

private val DIALOG_ICON_SIZE = 24.dp
