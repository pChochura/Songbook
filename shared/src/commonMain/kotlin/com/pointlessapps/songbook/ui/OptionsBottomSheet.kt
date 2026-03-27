package com.pointlessapps.songbook.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import com.pointlessapps.songbook.ui.components.SongbookIcon
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.defaultSongbookIconStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.spacing
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OptionsBottomSheet(
    state: SheetState,
    onDismissRequest: () -> Unit,
    items: List<OptionsBottomSheetItem>,
    header: @Composable () -> Unit = { },
) {
    ModalBottomSheet(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        scrimColor = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
        dragHandle = null,
        onDismissRequest = onDismissRequest,
        sheetState = state,
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.extraLarge),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        ) {
            header()
            items.forEach { OptionsBottomSheetItemButton(it) }
        }
    }
}

@Composable
fun OptionsBottomSheetTitleHeader(
    title: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SongbookText(
            modifier = Modifier.fillMaxWidth(),
            text = title,
            textStyle = defaultSongbookTextStyle().copy(
                textColor = MaterialTheme.colorScheme.onSurface,
                typography = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            ),
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    }
}

@Composable
fun OptionsBottomSheetItemButton(item: OptionsBottomSheetItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .clickable(role = Role.Button, onClick = item.onClick)
            .padding(vertical = MaterialTheme.spacing.small),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        item.icon?.let {
            SongbookIcon(
                iconRes = it,
                iconStyle = defaultSongbookIconStyle().copy(tint = item.color),
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
        ) {
            SongbookText(
                text = stringResource(item.label),
                textStyle = defaultSongbookTextStyle().copy(
                    textColor = item.color,
                    typography = MaterialTheme.typography.labelLarge,
                ),
            )

            if (item.description != null) {
                SongbookText(
                    text = stringResource(item.description),
                    textStyle = defaultSongbookTextStyle().copy(
                        textColor = item.color.copy(0.6f),
                        typography = MaterialTheme.typography.labelSmall,
                    ),
                )
            }
        }
    }
}

@ConsistentCopyVisibility
data class OptionsBottomSheetItem private constructor(
    val icon: DrawableResource?,
    val label: StringResource,
    val description: StringResource?,
    val color: Color,
    val onClick: () -> Unit,
) {
    companion object {
        @Composable
        fun new(
            label: StringResource,
            onClick: () -> Unit,
            icon: DrawableResource? = null,
            description: StringResource? = null,
            color: Color = MaterialTheme.colorScheme.onSurface,
        ) = OptionsBottomSheetItem(
            icon = icon,
            label = label,
            description = description,
            color = color,
            onClick = onClick,
        )
    }
}
