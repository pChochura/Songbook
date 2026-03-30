package com.pointlessapps.songbook.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
        contentWindowInsets = { WindowInsets() },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        scrimColor = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
        dragHandle = null,
        onDismissRequest = onDismissRequest,
        sheetState = state,
    ) {
        LazyColumn(
            contentPadding = PaddingValues(all = MaterialTheme.spacing.extraLarge),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        ) {
            item { Spacer(Modifier.statusBarsPadding()) }
            item { header() }
            items(items) {
                when (it) {
                    is OptionsBottomSheetItem.Button -> OptionsBottomSheetItemButton(it)
                    OptionsBottomSheetItem.Divider -> HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
            }
            item { Spacer(Modifier.navigationBarsPadding()) }
        }
    }
}

@Composable
internal fun OptionsBottomSheetTitleHeader(title: String) {
    SongbookText(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = MaterialTheme.spacing.small),
        text = title,
        textStyle = defaultSongbookTextStyle().copy(
            textColor = MaterialTheme.colorScheme.onSurface,
            typography = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        ),
    )
}

@Composable
internal fun OptionsBottomSheetItemButton(item: OptionsBottomSheetItem.Button) {
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
                icon = it,
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
                    text = item.description,
                    textStyle = defaultSongbookTextStyle().copy(
                        textColor = item.color.copy(0.6f),
                        typography = MaterialTheme.typography.labelSmall,
                    ),
                )
            }
        }
    }
}

internal sealed interface OptionsBottomSheetItem {
    data class Button(
        val icon: DrawableResource?,
        val label: StringResource,
        val description: String?,
        val color: Color,
        val onClick: () -> Unit,
    ) : OptionsBottomSheetItem

    data object Divider : OptionsBottomSheetItem

    companion object {
        @Composable
        fun new(
            label: StringResource,
            onClick: () -> Unit,
            icon: DrawableResource? = null,
            description: String? = null,
            color: Color = MaterialTheme.colorScheme.onSurface,
        ) = Button(
            icon = icon,
            label = label,
            description = description,
            color = color,
            onClick = onClick,
        )
    }
}
