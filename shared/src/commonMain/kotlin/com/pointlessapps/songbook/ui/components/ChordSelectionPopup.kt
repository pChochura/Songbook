package com.pointlessapps.songbook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.pointlessapps.songbook.model.Chord
import com.pointlessapps.songbook.shared.generated.resources.*
import com.pointlessapps.songbook.ui.theme.spacing
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChordSelectionPopup(
    offset: IntOffset,
    onChordSelected: (Chord) -> Unit,
    onDismissRequest: () -> Unit,
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
                .width(500.dp)
                .heightIn(max = 600.dp)
                .padding(spacing.medium),
            shape = RoundedCornerShape(24.dp),
            color = colorScheme.background,
            shadowElevation = 16.dp,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(spacing.huge)
                    .fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(Res.string.chord_selection_popup_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = colorScheme.onBackground
                )
                Text(
                    text = stringResource(Res.string.chord_selection_popup_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.outline
                )

                Spacer(modifier = Modifier.height(spacing.huge))

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 64.dp),
                    horizontalArrangement = Arrangement.spacedBy(spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(spacing.medium),
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth(),
                ) {
                    items(filteredChords) { chord ->
                        Surface(
                            onClick = { onChordSelected(chord) },
                            modifier = Modifier.aspectRatio(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = colorScheme.surface,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = chord.value,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(spacing.huge))

                // Search Bar
                TextField(
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
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(colorScheme.surface),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = colorScheme.outline,
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        cursorColor = colorScheme.onSurface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = colorScheme.onSurface,
                        unfocusedTextColor = colorScheme.onSurface,
                    ),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(spacing.large))

                // Bottom badge style indicator
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = colorScheme.surface,
                        modifier = Modifier.height(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = spacing.large)) {
                            Text(
                                text = stringResource(Res.string.chord_selection_popup_all_chords),
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}
