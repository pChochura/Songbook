package com.pointlessapps.songbook.setlist.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.pointlessapps.songbook.core.song.database.entity.SongSearchResult
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.common_unknown
import com.pointlessapps.songbook.shared.common_unnamed
import com.pointlessapps.songbook.shared.setlist_search_songs
import com.pointlessapps.songbook.shared.setlist_search_songs_placeholder
import com.pointlessapps.songbook.ui.components.SongbookBottomSheet
import com.pointlessapps.songbook.ui.components.SongbookIcon
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.SongbookTextField
import com.pointlessapps.songbook.ui.components.defaultSongbookIconStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextFieldStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.DEFAULT_BORDER_WIDTH
import com.pointlessapps.songbook.ui.theme.IconDone
import com.pointlessapps.songbook.ui.theme.spacing
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AddSongToSetlistBottomSheet(
    show: Boolean,
    textFieldState: TextFieldState,
    searchResults: LazyPagingItems<SongSearchResult>,
    setlistsSongIds: Set<String>,
    onItemClicked: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    SongbookBottomSheet(
        show = show,
        onDismissRequest = onDismissRequest,
    ) {
        Column(modifier = Modifier.animateContentSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = MaterialTheme.spacing.extraLarge),
                horizontalArrangement = Arrangement.Center,
            ) {
                SongbookText(
                    text = stringResource(Res.string.setlist_search_songs),
                    textStyle = defaultSongbookTextStyle().copy(
                        textAlign = TextAlign.Center,
                        textColor = MaterialTheme.colorScheme.onSurface,
                        typography = MaterialTheme.typography.titleLarge,
                    ),
                )
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth()
                    .padding(vertical = MaterialTheme.spacing.medium)
                    .padding(horizontal = MaterialTheme.spacing.extraLarge),
            ) {
                items(
                    count = searchResults.itemCount,
                    key = searchResults.itemKey { it.id },
                ) { index ->
                    val result = searchResults[index]
                    if (result != null) {
                        SearchResultCard(
                            modifier = Modifier.animateItem(),
                            result = result,
                            selected = setlistsSongIds.contains(result.songId),
                            onClick = { onItemClicked(result.songId) },
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = MaterialTheme.spacing.extraLarge)
                    .padding(horizontal = MaterialTheme.spacing.extraLarge),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
            ) {
                SongbookTextField(
                    state = textFieldState,
                    modifier = Modifier
                        .border(
                            width = DEFAULT_BORDER_WIDTH,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = MaterialTheme.shapes.small,
                        )
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = MaterialTheme.shapes.small,
                        )
                        .padding(MaterialTheme.spacing.large),
                    textFieldStyle = defaultSongbookTextFieldStyle().copy(
                        placeholder = stringResource(Res.string.setlist_search_songs_placeholder),
                        placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            autoCorrectEnabled = true,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Search,
                        ),
                    ),
                )
            }
        }
    }
}

@Composable
private fun SearchResultCard(
    result: SongSearchResult,
    onClick: () -> Unit,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    var isLoading by remember(selected) { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClick = {
                    isLoading = true
                    onClick()
                },
                role = Role.Checkbox,
            )
            .padding(vertical = MaterialTheme.spacing.large),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            SongbookText(
                text = result.title.takeIf { it.isNotEmpty() }
                    ?: stringResource(Res.string.common_unnamed),
                textStyle = defaultSongbookTextStyle().copy(
                    textColor = MaterialTheme.colorScheme.onSurface,
                    typography = MaterialTheme.typography.titleMedium,
                ),
            )
            SongbookText(
                text = result.artist.takeIf { it.isNotEmpty() }
                    ?: stringResource(Res.string.common_unknown),
                textStyle = defaultSongbookTextStyle().copy(
                    textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    typography = MaterialTheme.typography.bodySmall,
                ),
            )
        }

        AnimatedContent(
            targetState = isLoading to selected,
            transitionSpec = {
                fadeIn() + expandIn(expandFrom = Alignment.Center) togetherWith
                        fadeOut() + shrinkOut(shrinkTowards = Alignment.Center) using null
            },
            contentAlignment = Alignment.Center,
        ) { (isLoading, selected) ->
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(ICON_SIZE),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            } else if (selected) {
                SongbookIcon(
                    modifier = Modifier.size(ICON_SIZE),
                    icon = IconDone,
                    iconStyle = defaultSongbookIconStyle().copy(
                        tint = MaterialTheme.colorScheme.onSurface,
                    ),
                )
            }
        }
    }
}

private val ICON_SIZE = 24.dp
