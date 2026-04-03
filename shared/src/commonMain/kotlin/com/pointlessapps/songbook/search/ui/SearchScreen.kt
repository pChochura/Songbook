package com.pointlessapps.songbook.search.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.pointlessapps.songbook.LocalBottomBarPadding
import com.pointlessapps.songbook.LocalNavigator
import com.pointlessapps.songbook.search.SearchEvent
import com.pointlessapps.songbook.search.SearchViewModel
import com.pointlessapps.songbook.search.ui.components.EmptyListCard
import com.pointlessapps.songbook.search.ui.components.PublicLyricsCard
import com.pointlessapps.songbook.search.ui.components.SearchResultCard
import com.pointlessapps.songbook.search.ui.components.dialogs.ConfirmRemoveLastSearchDialog
import com.pointlessapps.songbook.search.ui.components.dialogs.PublicLyricsHelpDialog
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.common_back
import com.pointlessapps.songbook.shared.common_clear
import com.pointlessapps.songbook.shared.common_remove
import com.pointlessapps.songbook.shared.lyrics_allow
import com.pointlessapps.songbook.shared.lyrics_allow_public_lyrics_search_rationale
import com.pointlessapps.songbook.shared.lyrics_deny
import com.pointlessapps.songbook.shared.lyrics_what_is_public_lyrics
import com.pointlessapps.songbook.shared.navigation_search
import com.pointlessapps.songbook.shared.search_last_searches
import com.pointlessapps.songbook.shared.search_public_lyrics
import com.pointlessapps.songbook.shared.search_your_library
import com.pointlessapps.songbook.ui.components.Position
import com.pointlessapps.songbook.ui.components.SongbookButton
import com.pointlessapps.songbook.ui.components.SongbookIcon
import com.pointlessapps.songbook.ui.components.SongbookIconButton
import com.pointlessapps.songbook.ui.components.SongbookLoader
import com.pointlessapps.songbook.ui.components.SongbookScaffoldLayout
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.SongbookTextField
import com.pointlessapps.songbook.ui.components.defaultSongbookButtonStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookIconButtonStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookIconStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextFieldStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.DEFAULT_BORDER_WIDTH
import com.pointlessapps.songbook.ui.theme.IconArrowLeft
import com.pointlessapps.songbook.ui.theme.IconClose
import com.pointlessapps.songbook.ui.theme.IconHelp
import com.pointlessapps.songbook.ui.theme.IconHistory
import com.pointlessapps.songbook.ui.theme.IconNote
import com.pointlessapps.songbook.ui.theme.spacing
import com.pointlessapps.songbook.utils.add
import com.pointlessapps.songbook.utils.collectWithLifecycle
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SearchScreen(
    viewModel: SearchViewModel,
) {
    val navigator = LocalNavigator.current
    val searchResults = viewModel.searchResults.collectAsLazyPagingItems()
    var confirmRemoveLastSearchDialogData by remember { mutableStateOf<String?>(null) }
    var isPublicLyricsHelpDialogVisible by remember { mutableStateOf(false) }

    viewModel.events.collectWithLifecycle { event ->
        when (event) {
            is SearchEvent.NavigateBack -> navigator.navigateBack()
            is SearchEvent.NavigateToPreview -> navigator.navigateToPreview(
                title = event.title,
                artist = event.artist,
                sections = event.sections,
            )
        }
    }

    val shouldShowLastSearches by remember { derivedStateOf { viewModel.queryTextFieldState.text.isEmpty() } }
    val shouldShowNoItemsYourLibrary by remember { derivedStateOf { searchResults.itemCount == 0 } }
    val shouldShowNoItemsPublicLyrics by remember { derivedStateOf { viewModel.state.publicLyrics.isEmpty() } }

    SongbookScaffoldLayout(
        topBar = {
            SearchBar(
                query = viewModel.queryTextFieldState,
                onBackClicked = viewModel::onBackClicked,
                onClearClicked = viewModel::onClearClicked,
                onImeAction = viewModel::onImeAction,
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = MaterialTheme.spacing.large),
            contentPadding = paddingValues.add(
                vertical = MaterialTheme.spacing.medium,
            ).add(bottom = LocalBottomBarPadding.current.padding.value),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        ) {
            if (shouldShowLastSearches) {
                item(key = "last_searches_header") {
                    HeaderItem(stringResource(Res.string.search_last_searches))
                }

                items(viewModel.state.lastSearches, key = { it }) { lastSearch ->
                    LastSearchItem(
                        modifier = Modifier.animateItem(),
                        lastSearch = lastSearch,
                        onItemClicked = { viewModel.onLastSearchClicked(lastSearch) },
                        onRemoveClicked = { confirmRemoveLastSearchDialogData = lastSearch },
                    )
                }
            } else {
                item(key = "your_library_header") {
                    HeaderItem(stringResource(Res.string.search_your_library))
                }

                if (viewModel.state.isLoadingYourLibrary) {
                    item(key = "your_library_loading") {
                        Box(
                            modifier = Modifier
                                .animateItem()
                                .fillMaxWidth()
                                .padding(MaterialTheme.spacing.large),
                            contentAlignment = Alignment.Center,
                            content = { CircularProgressIndicator() },
                        )
                    }
                } else if (shouldShowNoItemsYourLibrary) {
                    item(key = "your_library_no_items") {
                        EmptyListCard(modifier = Modifier.animateItem().fillMaxWidth())
                    }
                }

                items(
                    count = searchResults.itemCount,
                    key = searchResults.itemKey { it.id },
                ) { index ->
                    val result = searchResults[index]
                    if (result != null) {
                        SearchResultCard(
                            modifier = Modifier.animateItem(),
                            result = result,
                            onClick = { navigator.navigateToLyrics(result.id) },
                        )
                    }
                }

                if (viewModel.state.showPublicLyrics != false) {
                    item(key = "public_lyrics_header") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            HeaderItem(stringResource(Res.string.search_public_lyrics))
                            SongbookIconButton(
                                icon = IconHelp,
                                tooltipLabel = Res.string.lyrics_what_is_public_lyrics,
                                onClick = { isPublicLyricsHelpDialogVisible = true },
                                iconButtonStyle = defaultSongbookIconButtonStyle().copy(
                                    containerColor = Color.Transparent,
                                    outlineColor = Color.Transparent,
                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                ),
                            )
                        }
                    }
                }

                if (viewModel.state.showPublicLyrics == true) {
                    if (viewModel.state.isLoadingPublicLyrics) {
                        item(key = "public_lyrics_loading") {
                            Box(
                                modifier = Modifier
                                    .animateItem()
                                    .fillMaxWidth()
                                    .padding(MaterialTheme.spacing.large),
                                contentAlignment = Alignment.Center,
                                content = { CircularProgressIndicator() },
                            )
                        }
                    } else if (shouldShowNoItemsPublicLyrics) {
                        item(key = "public_lyrics_no_items") {
                            EmptyListCard(modifier = Modifier.animateItem().fillMaxWidth())
                        }
                    }

                    items(viewModel.state.publicLyrics, key = { it.id }) { publicLyrics ->
                        PublicLyricsCard(
                            modifier = Modifier.animateItem(),
                            lyrics = publicLyrics,
                            onClick = { viewModel.onPublicLyricsClicked(publicLyrics) },
                        )
                    }
                } else if (viewModel.state.showPublicLyrics == null) {
                    item(key = "public_lyrics_allow") {
                        AllowPublicLyricsCard(
                            modifier = Modifier.animateItem(),
                            onAllowClicked = { viewModel.onAllowPublicLyricsClicked() },
                            onDenyClicked = { viewModel.onDenyPublicLyricsClicked() },
                        )
                    }
                }
            }
        }
    }

    confirmRemoveLastSearchDialogData?.let {
        ConfirmRemoveLastSearchDialog(
            lastSearch = it,
            onConfirmClicked = {
                viewModel.onLastSearchRemoveClicked(it)
                confirmRemoveLastSearchDialogData = null
            },
            onDismissRequest = { confirmRemoveLastSearchDialogData = null },
        )
    }

    if (isPublicLyricsHelpDialogVisible) {
        PublicLyricsHelpDialog(
            onDismissRequest = { isPublicLyricsHelpDialogVisible = false },
            onDenyClicked = {
                viewModel.onDenyPublicLyricsClicked()
                isPublicLyricsHelpDialogVisible = false
            },
        )
    }

    SongbookLoader(viewModel.state.isLoading)
}

@Composable
private fun LastSearchItem(
    lastSearch: String,
    onItemClicked: () -> Unit,
    onRemoveClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(
                role = Role.Button,
                onClick = onItemClicked,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
    ) {
        SongbookIcon(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = CircleShape,
                )
                .padding(MaterialTheme.spacing.extraSmall)
                .size(LAST_SEARCHES_ICON_SIZE),
            icon = IconHistory,
            iconStyle = defaultSongbookIconStyle().copy(
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )

        SongbookText(
            modifier = Modifier.weight(1f),
            text = lastSearch,
            textStyle = defaultSongbookTextStyle().copy(
                typography = MaterialTheme.typography.bodyMedium,
                textColor = MaterialTheme.colorScheme.onSurface,
            ),
        )

        SongbookIconButton(
            icon = IconClose,
            tooltipLabel = Res.string.common_remove,
            onClick = onRemoveClicked,
            iconButtonStyle = defaultSongbookIconButtonStyle().copy(
                containerColor = Color.Transparent,
                outlineColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
        )
    }
}

@Composable
private fun SearchBar(
    query: TextFieldState,
    onBackClicked: () -> Unit,
    onClearClicked: () -> Unit,
    onImeAction: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val shouldShowCloseButton by remember { derivedStateOf { query.text.isNotEmpty() } }

    SideEffect { focusRequester.requestFocus() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f))
            .statusBarsPadding()
            .padding(
                horizontal = MaterialTheme.spacing.medium,
                vertical = MaterialTheme.spacing.medium,
            ),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SongbookIconButton(
            modifier = Modifier.size(SEARCH_BAR_ICON_SIZE),
            icon = IconArrowLeft,
            tooltipLabel = Res.string.common_back,
            onClick = onBackClicked,
            iconButtonStyle = defaultSongbookIconButtonStyle().copy(
                containerColor = Color.Transparent,
                outlineColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tooltipPosition = Position.BELOW,
            ),
        )

        SongbookTextField(
            state = query,
            modifier = Modifier
                .focusRequester(focusRequester)
                .weight(1f)
                .padding(
                    vertical = MaterialTheme.spacing.medium,
                    horizontal = MaterialTheme.spacing.medium,
                ),
            onImeAction = {
                keyboardController?.hide()
                onImeAction()
            },
            textFieldStyle = defaultSongbookTextFieldStyle().copy(
                placeholder = stringResource(Res.string.navigation_search),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    autoCorrectEnabled = true,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search,
                    showKeyboardOnFocus = true,
                ),
                typography = MaterialTheme.typography.titleMedium,
                textColor = MaterialTheme.colorScheme.onSurface,
                placeholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            ),
        )

        AnimatedContent(
            targetState = shouldShowCloseButton,
            transitionSpec = {
                fadeIn() + expandIn(expandFrom = Alignment.Center) {
                    IntSize(it.width / 2, it.height / 2)
                } togetherWith fadeOut() + shrinkOut(shrinkTowards = Alignment.Center) {
                    IntSize(it.width / 2, it.height / 2)
                } using null
            },
            contentAlignment = Alignment.Center,
        ) { shouldShowCloseButton ->
            if (shouldShowCloseButton) {
                SongbookIconButton(
                    modifier = Modifier.size(SEARCH_BAR_ICON_SIZE),
                    icon = IconClose,
                    tooltipLabel = Res.string.common_clear,
                    onClick = onClearClicked,
                    iconButtonStyle = defaultSongbookIconButtonStyle().copy(
                        containerColor = Color.Transparent,
                        outlineColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        tooltipPosition = Position.BELOW,
                    ),
                )
            }
        }
    }
}

@Composable
private fun HeaderItem(text: String) {
    SongbookText(
        text = text,
        textStyle = defaultSongbookTextStyle().copy(
            textColor = MaterialTheme.colorScheme.onSurface,
            typography = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
            ),
        ),
    )
}

@Composable
private fun AllowPublicLyricsCard(
    onAllowClicked: () -> Unit,
    onDenyClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                vertical = MaterialTheme.spacing.medium,
                horizontal = MaterialTheme.spacing.large,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
    ) {
        SongbookIcon(
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(MaterialTheme.spacing.medium)
                .size(36.dp),
            icon = IconNote,
            iconStyle = defaultSongbookIconStyle().copy(
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )

        SongbookText(
            text = stringResource(Res.string.lyrics_allow_public_lyrics_search_rationale),
            textStyle = defaultSongbookTextStyle().copy(
                typography = MaterialTheme.typography.bodyMedium,
                textColor = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            ),
        )

        Row(
            modifier = Modifier.fillMaxWidth(0.7f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        ) {
            SongbookButton(
                modifier = Modifier.border(
                    width = DEFAULT_BORDER_WIDTH,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = CircleShape,
                ),
                label = stringResource(Res.string.lyrics_deny),
                onClick = onDenyClicked,
                buttonStyle = defaultSongbookButtonStyle().copy(
                    containerColor = Color.Transparent,
                    textStyle = defaultSongbookTextStyle().copy(
                        textAlign = TextAlign.Center,
                        textColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ),
            )

            SongbookButton(
                modifier = Modifier.weight(1f),
                label = stringResource(Res.string.lyrics_allow),
                onClick = onAllowClicked,
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
}

private val SEARCH_BAR_ICON_SIZE = 24.dp
private val LAST_SEARCHES_ICON_SIZE = 16.dp
