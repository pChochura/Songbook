package com.pointlessapps.songbook.library.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.library_search_placeholder
import com.pointlessapps.songbook.ui.components.SongbookChip
import com.pointlessapps.songbook.ui.components.SongbookTextField
import com.pointlessapps.songbook.ui.components.defaultSongbookChipStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextFieldStyle
import com.pointlessapps.songbook.ui.theme.IconClose
import com.pointlessapps.songbook.ui.theme.spacing
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SearchBar(
    query: String,
    filterLetter: Char?,
    onQueryChanged: (String) -> Unit,
    onFilterLetterRemoved: () -> Unit,
) {
    Row(
        modifier = Modifier
            .padding(
                horizontal = MaterialTheme.spacing.extraLarge,
                vertical = MaterialTheme.spacing.medium,
            )
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SongbookTextField(
            modifier = Modifier
                .weight(1f)
                .clip(MaterialTheme.shapes.medium)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = MaterialTheme.shapes.medium,
                )
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(
                    horizontal = MaterialTheme.spacing.large,
                    vertical = MaterialTheme.spacing.medium,
                ),
            value = query,
            onValueChange = onQueryChanged,
            textFieldStyle = defaultSongbookTextFieldStyle().copy(
                placeholder = stringResource(Res.string.library_search_placeholder),
                placeholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textColor = MaterialTheme.colorScheme.onSurface,
            ),
        )

        AnimatedContent(
            targetState = filterLetter,
            transitionSpec = { fadeIn() togetherWith fadeOut() using null },
        ) { filterLetter ->
            if (filterLetter == null) return@AnimatedContent

            SongbookChip(
                modifier = Modifier.fillMaxHeight().widthIn(min = 64.dp),
                label = filterLetter.toString(),
                isSelected = true,
                onClick = onFilterLetterRemoved,
                chipStyle = defaultSongbookChipStyle().copy(
                    selectedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    selectedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedOutlineColor = MaterialTheme.colorScheme.outlineVariant,
                    iconRes = IconClose,
                    iconAlignment = Alignment.End,
                ),
            )
        }
    }
}
