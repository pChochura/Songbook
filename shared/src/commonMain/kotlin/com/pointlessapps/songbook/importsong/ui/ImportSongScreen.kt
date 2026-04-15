package com.pointlessapps.songbook.importsong.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.pointlessapps.songbook.LocalNavigator
import com.pointlessapps.songbook.importsong.ImportSongEvent
import com.pointlessapps.songbook.importsong.ImportSongViewModel
import com.pointlessapps.songbook.importsong.ui.components.ChordSuggestionPopup
import com.pointlessapps.songbook.importsong.ui.components.ImportSongOptionsBottomSheet
import com.pointlessapps.songbook.importsong.ui.components.ImportSongOptionsBottomSheetAction.AddToSetlists
import com.pointlessapps.songbook.importsong.ui.components.ImportSongOptionsBottomSheetAction.Preview
import com.pointlessapps.songbook.importsong.ui.components.ImportSongOptionsBottomSheetAction.Rescan
import com.pointlessapps.songbook.importsong.ui.components.dialogs.ConfirmDiscardChangesDialog
import com.pointlessapps.songbook.importsong.ui.components.dialogs.ExtractingInProgressDialog
import com.pointlessapps.songbook.importsong.ui.components.dialogs.ScanDialog
import com.pointlessapps.songbook.importsong.ui.utils.ChordHighlightTransformation
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.common_cancel
import com.pointlessapps.songbook.shared.common_import_song
import com.pointlessapps.songbook.shared.common_save_changes
import com.pointlessapps.songbook.shared.common_scan_photo_description
import com.pointlessapps.songbook.shared.common_tooltip
import com.pointlessapps.songbook.shared.import_artist_label
import com.pointlessapps.songbook.shared.import_artist_placeholder
import com.pointlessapps.songbook.shared.import_header_title
import com.pointlessapps.songbook.shared.import_header_title_edit
import com.pointlessapps.songbook.shared.import_lyrics_label
import com.pointlessapps.songbook.shared.import_lyrics_placeholder
import com.pointlessapps.songbook.shared.import_lyrics_tip
import com.pointlessapps.songbook.shared.import_menu_rescan_description
import com.pointlessapps.songbook.shared.import_song_title_label
import com.pointlessapps.songbook.shared.import_song_title_placeholder
import com.pointlessapps.songbook.ui.TopBar
import com.pointlessapps.songbook.ui.TopBarButton
import com.pointlessapps.songbook.ui.components.SongbookButton
import com.pointlessapps.songbook.ui.components.SongbookIcon
import com.pointlessapps.songbook.ui.components.SongbookLoader
import com.pointlessapps.songbook.ui.components.SongbookScaffoldLayout
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.SongbookTextField
import com.pointlessapps.songbook.ui.components.defaultSongbookButtonStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookIconStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextFieldStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.dialogs.SetlistsDialog
import com.pointlessapps.songbook.ui.theme.DEFAULT_BORDER_WIDTH
import com.pointlessapps.songbook.ui.theme.IconHelp
import com.pointlessapps.songbook.ui.theme.spacing
import com.pointlessapps.songbook.utils.collectWithLifecycle
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ImportSongScreen(
    viewModel: ImportSongViewModel,
) {
    val navigator = LocalNavigator.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    var isScanDialogVisible by rememberSaveable(Unit) { mutableStateOf(viewModel.showScanDialog) }
    var isBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
    var isDiscardChangesDialogVisible by rememberSaveable { mutableStateOf(false) }

    viewModel.events.collectWithLifecycle { event ->
        when (event) {
            is ImportSongEvent.DiscardChanges -> isDiscardChangesDialogVisible = true
            is ImportSongEvent.NavigateBack -> navigator.navigateBack()
            is ImportSongEvent.NavigateToLyrics -> navigator.navigateToLyrics(event.songId)
            is ImportSongEvent.NavigateToPreview -> navigator.navigateToPreview(
                title = event.title,
                artist = event.artist,
                sections = event.sections,
            )
        }
    }

    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        onBackCompleted = viewModel::onCancelClicked,
    )

    SongbookScaffoldLayout(
        topBar = @Composable {
            TopBar(
                leftButton = TopBarButton.back(
                    onClick = viewModel::onCancelClicked,
                ),
                rightButton = TopBarButton.menu(
                    onClick = { isBottomSheetVisible = true },
                ),
                title = stringResource(
                    if (state.songId == null) {
                        Res.string.import_header_title
                    } else {
                        Res.string.import_header_title_edit
                    },
                ),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .imePadding()
                .navigationBarsPadding()
                .padding(MaterialTheme.spacing.extraLarge),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
            ) {
                LabeledTextField(
                    required = true,
                    label = stringResource(Res.string.import_song_title_label),
                    textFieldState = viewModel.titleTextFieldState,
                    placeholder = stringResource(Res.string.import_song_title_placeholder),
                    modifier = Modifier.weight(1f).height(IntrinsicSize.Min),
                    imeAction = ImeAction.Next,
                )
                LabeledTextField(
                    label = stringResource(Res.string.import_artist_label),
                    textFieldState = viewModel.artistTextFieldState,
                    placeholder = stringResource(Res.string.import_artist_placeholder),
                    modifier = Modifier.weight(1f).height(IntrinsicSize.Min),
                    imeAction = ImeAction.Next,
                )
            }

            SongLyricsTextField(
                lyricsTextFieldState = viewModel.lyricsTextFieldState,
                chordSuggestions = state.chordSuggestions,
                onChordSelected = viewModel::onChordSelected,
                onDismissChordPopup = viewModel::onDismissChordPopup,
                modifier = Modifier.weight(1f),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
            ) {
                SongbookButton(
                    modifier = Modifier
                        .border(
                            width = DEFAULT_BORDER_WIDTH,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = CircleShape,
                        ),
                    label = stringResource(Res.string.common_cancel),
                    onClick = viewModel::onCancelClicked,
                    buttonStyle = defaultSongbookButtonStyle().copy(
                        shape = CircleShape,
                        containerColor = Color.Transparent,
                        textStyle = defaultSongbookTextStyle().copy(
                            textColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    ),
                )

                SongbookButton(
                    modifier = Modifier.weight(1f),
                    label = stringResource(
                        if (state.songId == null) {
                            Res.string.common_import_song
                        } else {
                            Res.string.common_save_changes
                        },
                    ),
                    onClick = viewModel::onImportSongClicked,
                    buttonStyle = defaultSongbookButtonStyle().copy(
                        enabled = state.canImport,
                        shape = CircleShape,
                    ),
                )
            }
        }
    }

    var isSetlistsDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isRescanDialogVisible by rememberSaveable { mutableStateOf(false) }

    ImportSongOptionsBottomSheet(
        show = isBottomSheetVisible,
        state = state,
        onDismissRequest = { isBottomSheetVisible = false },
        onAction = {
            isBottomSheetVisible = false

            when (it) {
                AddToSetlists -> isSetlistsDialogVisible = true
                Rescan -> isRescanDialogVisible = true
                Preview -> viewModel.onPreviewClicked()
            }
        },
    )

    if (isSetlistsDialogVisible) {
        SetlistsDialog(
            setlists = state.setlistsSelection,
            onSetlistsSelected = {
                viewModel.onSetlistsSelected(it)
                isSetlistsDialogVisible = false
            },
            onDismissRequest = { isSetlistsDialogVisible = false },
        )
    }

    if (isRescanDialogVisible || isScanDialogVisible) {
        ScanDialog(
            description = if (isScanDialogVisible) {
                Res.string.common_scan_photo_description
            } else {
                Res.string.import_menu_rescan_description
            },
            showEnterManuallyButton = isScanDialogVisible,
            onImageCaptured = {
                viewModel.onImageCaptured(it)
                isRescanDialogVisible = false
                isScanDialogVisible = false
            },
            onEnterManuallyClicked = {
                isRescanDialogVisible = false
                isScanDialogVisible = false
            },
            onOpenSettingsClicked = viewModel::onOpenSettingsClicked,
            onDismissRequest = {
                if (isScanDialogVisible) {
                    navigator.navigateBack()
                }
                isRescanDialogVisible = false
                isScanDialogVisible = false
            },
        )
    }

    if (isDiscardChangesDialogVisible) {
        ConfirmDiscardChangesDialog(
            onConfirmClicked = {
                isDiscardChangesDialogVisible = false
                viewModel.onDiscardChangesClicked()
            },
            onDismissRequest = { isDiscardChangesDialogVisible = false },
        )
    }

    if (state.isExtractingInProgress) {
        ExtractingInProgressDialog(
            onDismissRequest = viewModel::onCancelExtractionClicked,
        )
    }

    SongbookLoader(state.isLoading)
}

@Composable
private fun SongLyricsTextField(
    lyricsTextFieldState: TextFieldState,
    chordSuggestions: List<String>,
    onChordSelected: (String) -> Unit,
    onDismissChordPopup: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
    ) {
        LabeledTextField(
            required = true,
            label = stringResource(Res.string.import_lyrics_label),
            textFieldState = lyricsTextFieldState,
            placeholder = stringResource(Res.string.import_lyrics_placeholder),
            modifier = Modifier.weight(1f),
            outputTransformation = ChordHighlightTransformation,
            imeAction = ImeAction.None,
            popupContent = { textLayoutResultCallback ->
                val cursorPosition = lyricsTextFieldState.selection.end
                textLayoutResultCallback()?.let {
                    val cursorRect = it.getCursorRect(cursorPosition)
                    ChordSuggestionPopup(
                        cursorRect = cursorRect,
                        suggestions = chordSuggestions,
                        onChordSelected = onChordSelected,
                        onDismissRequest = onDismissChordPopup,
                    )
                }
            },
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        ) {
            SongbookIcon(
                icon = IconHelp,
                contentDescription = stringResource(Res.string.common_tooltip),
                iconStyle = defaultSongbookIconStyle().copy(
                    tint = MaterialTheme.colorScheme.onSurface,
                ),
            )

            SongbookText(
                text = stringResource(Res.string.import_lyrics_tip),
                modifier = Modifier.weight(1f),
                textStyle = defaultSongbookTextStyle().copy(
                    typography = MaterialTheme.typography.bodySmall,
                    textColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        }
    }
}

@Composable
private fun LabeledTextField(
    label: String,
    textFieldState: TextFieldState,
    placeholder: String,
    imeAction: ImeAction,
    required: Boolean = false,
    outputTransformation: OutputTransformation? = null,
    modifier: Modifier = Modifier,
    popupContent: @Composable ((() -> TextLayoutResult?) -> Unit)? = null,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
        ) {
            SongbookText(
                text = label,
                textStyle = defaultSongbookTextStyle().copy(
                    textColor = MaterialTheme.colorScheme.onSurface,
                    typography = MaterialTheme.typography.labelMedium,
                ),
            )

            if (required) {
                SongbookText(
                    text = "*",
                    textStyle = defaultSongbookTextStyle().copy(
                        textColor = MaterialTheme.colorScheme.error,
                        typography = MaterialTheme.typography.labelMedium,
                    ),
                )
            }
        }
        Box {
            var onTextLayoutCallback by remember { mutableStateOf<() -> TextLayoutResult?>({ null }) }

            SongbookTextField(
                state = textFieldState,
                onTextLayout = {
                    if (onTextLayoutCallback() == null) {
                        onTextLayoutCallback = it
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = DEFAULT_BORDER_WIDTH,
                        color = MaterialTheme.colorScheme.outline,
                        shape = MaterialTheme.shapes.small,
                    )
                    .padding(
                        horizontal = MaterialTheme.spacing.medium,
                        vertical = MaterialTheme.spacing.small,
                    ),
                textFieldStyle = defaultSongbookTextFieldStyle().copy(
                    placeholder = placeholder,
                    placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    outputTransformation = outputTransformation,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        autoCorrectEnabled = true,
                        keyboardType = KeyboardType.Text,
                        imeAction = imeAction,
                    ),
                ),
            )
            popupContent?.invoke(onTextLayoutCallback)
        }
    }
}
