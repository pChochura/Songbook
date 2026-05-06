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
import com.pointlessapps.songbook.library.SortBy
import com.pointlessapps.songbook.library.SortBy.Field.Artist
import com.pointlessapps.songbook.library.SortBy.Field.DateAdded
import com.pointlessapps.songbook.library.SortBy.Field.Title
import com.pointlessapps.songbook.shared.ui.Res
import com.pointlessapps.songbook.shared.ui.common_confirm
import com.pointlessapps.songbook.shared.ui.library_change_sorting_order
import com.pointlessapps.songbook.shared.ui.library_change_sorting_order_by_artist
import com.pointlessapps.songbook.shared.ui.library_change_sorting_order_by_date_added
import com.pointlessapps.songbook.shared.ui.library_change_sorting_order_by_title
import com.pointlessapps.songbook.shared.ui.library_change_sorting_order_is_ascending
import com.pointlessapps.songbook.ui.components.SongbookButton
import com.pointlessapps.songbook.ui.components.SongbookCheckbox
import com.pointlessapps.songbook.ui.components.SongbookDialog
import com.pointlessapps.songbook.ui.components.SongbookDialogDismissible
import com.pointlessapps.songbook.ui.components.SongbookIcon
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.defaultSongbookButtonStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookDialogStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookIconStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.IconDone
import com.pointlessapps.songbook.ui.theme.IconSort
import com.pointlessapps.songbook.ui.theme.spacing
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SortByDialog(
    sortBy: SortBy,
    onSortBySelected: (SortBy) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var currentlyIsAscending by rememberSaveable { mutableStateOf(sortBy.ascending) }
    var currentlySelectedSortByField by rememberSaveable { mutableStateOf(sortBy.field) }

    SongbookDialog(
        onDismissRequest = onDismissRequest,
        dialogStyle = defaultSongbookDialogStyle().copy(
            label = stringResource(Res.string.library_change_sorting_order),
            icon = IconSort,
            scrollable = false,
            dismissible = SongbookDialogDismissible.OnBackPress,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        ) {
            LazyColumn(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .weight(1f, fill = false),
            ) {
                item {
                    Entry(
                        title = Res.string.library_change_sorting_order_by_title,
                        isSelected = currentlySelectedSortByField == Title,
                        hasAlternativeBackground = false,
                        onClick = { currentlySelectedSortByField = Title },
                    )
                }
                item {
                    Entry(
                        title = Res.string.library_change_sorting_order_by_artist,
                        isSelected = currentlySelectedSortByField == Artist,
                        hasAlternativeBackground = true,
                        onClick = { currentlySelectedSortByField = Artist },
                    )
                }
                item {
                    Entry(
                        title = Res.string.library_change_sorting_order_by_date_added,
                        isSelected = currentlySelectedSortByField == DateAdded,
                        hasAlternativeBackground = false,
                        onClick = { currentlySelectedSortByField = DateAdded },
                    )
                }
            }

            SongbookCheckbox(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium),
                label = stringResource(Res.string.library_change_sorting_order_is_ascending),
                checked = currentlyIsAscending,
                onCheckChanged = { currentlyIsAscending = !currentlyIsAscending },
            )
        }

        SongbookButton(
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(Res.string.common_confirm),
            onClick = {
                onSortBySelected(
                    SortBy(
                        ascending = currentlyIsAscending,
                        field = currentlySelectedSortByField,
                    ),
                )
            },
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

@Composable
private fun Entry(
    title: StringResource,
    isSelected: Boolean,
    hasAlternativeBackground: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (hasAlternativeBackground) {
                    Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
                } else {
                    Modifier
                },
            )
            .clickable(
                role = Role.Button,
                onClick = onClick,
            )
            .padding(
                vertical = MaterialTheme.spacing.medium,
                horizontal = MaterialTheme.spacing.large,
            ),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SongbookText(
            modifier = Modifier.weight(1f).height(ICON_SIZE),
            text = stringResource(title),
            textStyle = defaultSongbookTextStyle().copy(
                textColor = MaterialTheme.colorScheme.onSurface,
                typography = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (isSelected) {
                        FontWeight.Bold
                    } else {
                        MaterialTheme.typography.labelMedium.fontWeight
                    },
                ),
            ),
        )

        SongbookIcon(
            modifier = Modifier
                .size(ICON_SIZE)
                .graphicsLayer { alpha = if (isSelected) 1f else 0f },
            icon = IconDone,
            iconStyle = defaultSongbookIconStyle().copy(
                tint = MaterialTheme.colorScheme.onSurface,
            ),
        )
    }
}

private val ICON_SIZE = 16.dp
