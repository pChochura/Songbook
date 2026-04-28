package com.pointlessapps.songbook.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pointlessapps.songbook.core.setlist.model.Setlist
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.common_confirm
import com.pointlessapps.songbook.shared.import_menu_add_to_setlists
import com.pointlessapps.songbook.shared.import_menu_add_to_setlists_description
import com.pointlessapps.songbook.ui.components.SongbookButton
import com.pointlessapps.songbook.ui.components.SongbookDialog
import com.pointlessapps.songbook.ui.components.SongbookDialogDismissible
import com.pointlessapps.songbook.ui.components.SongbookIcon
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.defaultSongbookButtonStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookDialogStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookIconStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.IconAddFolder
import com.pointlessapps.songbook.ui.theme.IconDone
import com.pointlessapps.songbook.ui.theme.spacing
import com.pointlessapps.songbook.utils.rememberSnapshotMapSaver
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SetlistsDialog(
    setlists: ImmutableMap<Setlist, Boolean>,
    onSetlistsSelected: (ImmutableList<Setlist>) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val currentlySelectedSetlists = rememberSaveable(saver = rememberSnapshotMapSaver()) {
        mutableStateMapOf(*(setlists.toList().toTypedArray()))
    }

    SongbookDialog(
        modifier = Modifier.heightIn(max = 500.dp),
        onDismissRequest = onDismissRequest,
        dialogStyle = defaultSongbookDialogStyle().copy(
            label = stringResource(Res.string.import_menu_add_to_setlists),
            icon = IconAddFolder,
            scrollable = false,
            dismissible = SongbookDialogDismissible.OnBackPress,
        ),
    ) {
        SongbookText(
            text = stringResource(Res.string.import_menu_add_to_setlists_description),
            textStyle = defaultSongbookTextStyle().copy(
                typography = MaterialTheme.typography.bodyMedium,
                textColor = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            ),
        )

        LazyColumn(
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .weight(1f, fill = false),
        ) {
            itemsIndexed(
                items = setlists.keys.toList(),
                key = { _, setlist -> setlist.id },
            ) { index, setlist ->
                SetlistItem(
                    modifier = Modifier.animateItem(),
                    index = index,
                    setlist = setlist,
                    isSelected = currentlySelectedSetlists[setlist] == true,
                    onSetlistSelected = {
                        currentlySelectedSetlists[setlist] =
                            !(currentlySelectedSetlists.getOrElse(setlist) { false })
                    },
                )
            }
        }

        SongbookButton(
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(Res.string.common_confirm),
            onClick = {
                onSetlistsSelected(
                    currentlySelectedSetlists
                        .filterValues { it }.keys
                        .toImmutableList(),
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
private fun SetlistItem(
    index: Int,
    setlist: Setlist,
    isSelected: Boolean,
    onSetlistSelected: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
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
                onClick = onSetlistSelected,
            )
            .padding(
                vertical = MaterialTheme.spacing.medium,
                horizontal = MaterialTheme.spacing.large,
            ),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SongbookText(
            modifier = Modifier.weight(1f),
            text = setlist.name,
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
