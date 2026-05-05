package com.pointlessapps.songbook.library.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pointlessapps.songbook.library.DisplayMode
import com.pointlessapps.songbook.library.DisplayMode.Grid
import com.pointlessapps.songbook.library.DisplayMode.List
import com.pointlessapps.songbook.shared.ui.Res
import com.pointlessapps.songbook.shared.ui.common_confirm
import com.pointlessapps.songbook.shared.ui.common_select_display_mode
import com.pointlessapps.songbook.shared.ui.library_menu_display_mode_grid
import com.pointlessapps.songbook.shared.ui.library_menu_display_mode_grid_description
import com.pointlessapps.songbook.shared.ui.library_menu_display_mode_list
import com.pointlessapps.songbook.shared.ui.library_menu_display_mode_list_description
import com.pointlessapps.songbook.ui.components.SongbookButton
import com.pointlessapps.songbook.ui.components.SongbookDialog
import com.pointlessapps.songbook.ui.components.SongbookDialogDismissible
import com.pointlessapps.songbook.ui.components.SongbookIcon
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.defaultSongbookButtonStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookDialogStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookIconStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.IconDisplayMode
import com.pointlessapps.songbook.ui.theme.IconDone
import com.pointlessapps.songbook.ui.theme.spacing
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DisplayModeDialog(
    mode: DisplayMode,
    onModeSelected: (DisplayMode) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var currentlySelectedMode by rememberSaveable { mutableStateOf(mode) }

    SongbookDialog(
        onDismissRequest = onDismissRequest,
        dialogStyle = defaultSongbookDialogStyle().copy(
            label = stringResource(Res.string.common_select_display_mode),
            icon = IconDisplayMode,
            scrollable = false,
            dismissible = SongbookDialogDismissible.OnBackPress,
        ),
    ) {
        LazyColumn(
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .weight(1f, fill = false),
        ) {
            itemsIndexed(DisplayMode.entries) { index, mode ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (index % 2 == 0) {
                                Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
                            } else {
                                Modifier
                            },
                        )
                        .clickable(
                            role = Role.Button,
                            onClick = { currentlySelectedMode = mode },
                        )
                        .padding(
                            vertical = MaterialTheme.spacing.medium,
                            horizontal = MaterialTheme.spacing.large,
                        ),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                    ) {
                        SongbookText(
                            modifier = Modifier.height(ICON_SIZE),
                            text = stringResource(
                                when (mode) {
                                    Grid -> Res.string.library_menu_display_mode_grid
                                    List -> Res.string.library_menu_display_mode_list
                                },
                            ),
                            textStyle = defaultSongbookTextStyle().copy(
                                textColor = MaterialTheme.colorScheme.onSurface,
                                typography = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = if (currentlySelectedMode == mode) {
                                        FontWeight.Bold
                                    } else {
                                        MaterialTheme.typography.labelMedium.fontWeight
                                    },
                                ),
                            ),
                        )

                        SongbookText(
                            text = stringResource(
                                when (mode) {
                                    Grid -> Res.string.library_menu_display_mode_grid_description
                                    List -> Res.string.library_menu_display_mode_list_description
                                },
                            ),
                            textStyle = defaultSongbookTextStyle().copy(
                                textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                typography = MaterialTheme.typography.labelSmall,
                            ),
                        )
                    }

                    SongbookIcon(
                        modifier = Modifier
                            .size(ICON_SIZE)
                            .graphicsLayer {
                                alpha = if (currentlySelectedMode == mode) 1f else 0f
                            },
                        icon = IconDone,
                        iconStyle = defaultSongbookIconStyle().copy(
                            tint = MaterialTheme.colorScheme.onSurface,
                        ),
                    )
                }
            }
        }

        SongbookButton(
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(Res.string.common_confirm),
            onClick = { onModeSelected(currentlySelectedMode) },
            buttonStyle = defaultSongbookButtonStyle().copy(
                containerColor = MaterialTheme.colorScheme.primary,
                textStyle = defaultSongbookTextStyle().copy(
                    textAlign = TextAlign.Center,
                    textColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ),
        )
    }
}

private val ICON_SIZE = 16.dp
