package com.pointlessapps.songbook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.pointlessapps.songbook.core.domain.models.Chord
import com.pointlessapps.songbook.shared.generated.resources.Res
import com.pointlessapps.songbook.shared.generated.resources.chord_selection_popup_remove
import com.pointlessapps.songbook.shared.generated.resources.chord_selection_popup_search_placeholder
import com.pointlessapps.songbook.ui.theme.spacing
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChordSelectionPopup(
    offset: IntOffset,
    onChordSelected: (Chord?) -> Unit,
    onDismissRequest: () -> Unit,
    selectedChord: Chord? = null,
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredChords = remember(searchQuery) {
        Chord.allCommon.filter { it.value.contains(searchQuery, ignoreCase = true) }
    }

    val spacing = MaterialTheme.spacing
    val colorScheme = MaterialTheme.colorScheme

    Popup(
        alignment = Alignment.TopStart,
        offset = offset,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true),
    ) {
        Surface(
            modifier = Modifier
                .widthIn(500.dp)
                .heightIn(max = 600.dp)
                .padding(spacing.medium),
            shape = MaterialTheme.shapes.extraLarge,
            color = colorScheme.background,
            shadowElevation = spacing.large,
            tonalElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier
                    .padding(spacing.large)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(spacing.large),
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 64.dp),
                    horizontalArrangement = Arrangement.spacedBy(spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(spacing.medium),
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth(),
                ) {
                    items(filteredChords) { chord ->
                        val isSelected = chord.value == selectedChord?.value
                        Surface(
                            onClick = { onChordSelected(chord) },
                            modifier = Modifier
                                .aspectRatio(1f)
                                .then(
                                    if (isSelected) {
                                        Modifier.border(
                                            width = spacing.extraSmall,
                                            color = colorScheme.primary,
                                            shape = MaterialTheme.shapes.medium,
                                        )
                                    } else {
                                        Modifier
                                    },
                                ),
                            shape = MaterialTheme.shapes.medium,
                            color = if (isSelected) colorScheme.primaryContainer else colorScheme.surface,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = chord.value,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (isSelected) colorScheme.onPrimaryContainer else colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = stringResource(Res.string.chord_selection_popup_search_placeholder),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.outline,
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .background(colorScheme.surface),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = colorScheme.outline,
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.5f,
                            ),
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                            unfocusedLeadingIconColor = MaterialTheme.colorScheme.outline,
                            focusedTrailingIconColor = MaterialTheme.colorScheme.outline,
                            unfocusedTrailingIconColor = MaterialTheme.colorScheme.outline,
                        ),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                    )

                    Button(
                        modifier = Modifier.height(48.dp),
                        onClick = { onChordSelected(null) },
                        enabled = selectedChord != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
                            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                        ),
                    ) {
                        Text(stringResource(Res.string.chord_selection_popup_remove))
                    }
                }
            }
        }
    }
}
