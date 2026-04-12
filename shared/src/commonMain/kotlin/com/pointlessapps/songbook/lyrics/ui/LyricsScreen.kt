package com.pointlessapps.songbook.lyrics.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.pointlessapps.songbook.LocalNavigator
import com.pointlessapps.songbook.lyrics.DisplayMode
import com.pointlessapps.songbook.lyrics.LyricsEvent
import com.pointlessapps.songbook.lyrics.LyricsState
import com.pointlessapps.songbook.lyrics.LyricsViewModel
import com.pointlessapps.songbook.lyrics.LyricsViewModel.Companion.MAX_ZOOM
import com.pointlessapps.songbook.lyrics.LyricsViewModel.Companion.MIN_ZOOM
import com.pointlessapps.songbook.lyrics.WrapMode
import com.pointlessapps.songbook.lyrics.ui.components.LyricsOptionsBottomSheet
import com.pointlessapps.songbook.lyrics.ui.components.LyricsOptionsBottomSheetAction.AddToSetlist
import com.pointlessapps.songbook.lyrics.ui.components.LyricsOptionsBottomSheetAction.Broadcast
import com.pointlessapps.songbook.lyrics.ui.components.LyricsOptionsBottomSheetAction.Delete
import com.pointlessapps.songbook.lyrics.ui.components.LyricsOptionsBottomSheetAction.DisplayMode
import com.pointlessapps.songbook.lyrics.ui.components.LyricsOptionsBottomSheetAction.Edit
import com.pointlessapps.songbook.lyrics.ui.components.LyricsOptionsBottomSheetAction.Fullscreen
import com.pointlessapps.songbook.lyrics.ui.components.LyricsOptionsBottomSheetAction.KeyOffset
import com.pointlessapps.songbook.lyrics.ui.components.LyricsOptionsBottomSheetAction.ShowQueue
import com.pointlessapps.songbook.lyrics.ui.components.LyricsOptionsBottomSheetAction.TextScale
import com.pointlessapps.songbook.lyrics.ui.components.LyricsOptionsBottomSheetAction.WrapMode
import com.pointlessapps.songbook.lyrics.ui.components.dialogs.ConfirmBroadcastToTeamDialog
import com.pointlessapps.songbook.lyrics.ui.components.dialogs.DisplayModeDialog
import com.pointlessapps.songbook.lyrics.ui.components.dialogs.KeyOffsetDialog
import com.pointlessapps.songbook.lyrics.ui.components.dialogs.TextScaleDialog
import com.pointlessapps.songbook.lyrics.ui.components.dialogs.WrapModeDialog
import com.pointlessapps.songbook.preview.ui.PreviewSongLayout
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.common_close_fullscreen
import com.pointlessapps.songbook.shared.lyrics_delete_song
import com.pointlessapps.songbook.shared.lyrics_delete_song_description
import com.pointlessapps.songbook.shared.lyrics_section_label
import com.pointlessapps.songbook.ui.TopBar
import com.pointlessapps.songbook.ui.TopBarButton
import com.pointlessapps.songbook.ui.components.SongbookIconButton
import com.pointlessapps.songbook.ui.components.SongbookLoader
import com.pointlessapps.songbook.ui.components.SongbookScaffoldLayout
import com.pointlessapps.songbook.ui.components.defaultSongbookIconButtonStyle
import com.pointlessapps.songbook.ui.dialogs.ConfirmDeleteDialog
import com.pointlessapps.songbook.ui.theme.IconClose
import com.pointlessapps.songbook.ui.theme.spacing
import com.pointlessapps.songbook.utils.collectWithLifecycle
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LyricsScreen(
    viewModel: LyricsViewModel,
) {
    val navigator = LocalNavigator.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    viewModel.events.collectWithLifecycle { event ->
        when (event) {
            is LyricsEvent.NavigateBack -> navigator.navigateBack()
            is LyricsEvent.NavigateToImportSong -> navigator.navigateToImportSong(
                id = event.songId,
                title = event.title,
                artist = event.artist,
                lyrics = event.lyrics,
            )
        }
    }

    when (val state = state) {
        LyricsState.Loading -> SongbookLoader(true)
        is LyricsState.Loaded -> LyricsScreenContent(
            state = state,
            onNavigateBack = navigator::navigateBack,
            onEditSongClicked = viewModel::onEditSongClicked,
            onTextScaleChanged = viewModel::onTextScaleChanged,
            onKeyOffsetChanged = viewModel::onKeyOffsetChanged,
            onDisplayModeChanged = viewModel::onDisplayModeChanged,
            onWrapModeChanged = viewModel::onWrapModeChanged,
            onBroadcastToTeamConfirmClicked = viewModel::onBroadcastToTeamConfirmClicked,
            onDeleteSongConfirmClicked = viewModel::onDeleteSongConfirmClicked,
        )
    }
}

@Composable
private fun LyricsScreenContent(
    state: LyricsState.Loaded,
    onNavigateBack: () -> Unit,
    onEditSongClicked: () -> Unit,
    onTextScaleChanged: (Int) -> Unit,
    onKeyOffsetChanged: (Int) -> Unit,
    onDisplayModeChanged: (DisplayMode) -> Unit,
    onWrapModeChanged: (WrapMode) -> Unit,
    onBroadcastToTeamConfirmClicked: () -> Unit,
    onDeleteSongConfirmClicked: () -> Unit,
) {
    var isTopBarVisible by rememberSaveable { mutableStateOf(true) }
    var isBottomSheetVisible by rememberSaveable { mutableStateOf(false) }

    NavigationBackHandler(
        state = rememberNavigationEventState(
            currentInfo = NavigationEventInfo.None,
        ),
        isBackEnabled = !isTopBarVisible,
        onBackCompleted = { isTopBarVisible = true },
    )

    SongbookScaffoldLayout(
        topBar = @Composable {
            AnimatedContent(isTopBarVisible) {
                if (it) {
                    TopBar(
                        leftButton = TopBarButton.back(
                            onClick = onNavigateBack,
                        ),
                        rightButton = TopBarButton.menu(
                            onClick = { isBottomSheetVisible = true },
                        ),
                        title = stringResource(Res.string.lyrics_section_label),
                    )
                } else {
                    Spacer(Modifier.statusBarsPadding())
                }
            }
        },
    ) { paddingValues ->
        Box(Modifier.fillMaxSize()) {
            PreviewSongLayout(
                title = state.title,
                artist = state.artist,
                sections = state.sections,
                textScale = state.textScale,
                keyOffset = state.keyOffset,
                displayMode = state.displayMode,
                wrapMode = state.wrapMode,
                onTextScaleChanged = onTextScaleChanged,
                paddingValues = paddingValues,
            )

            AnimatedVisibility(
                visible = !isTopBarVisible,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(paddingValues)
                    .padding(all = MaterialTheme.spacing.huge),
            ) {
                SongbookIconButton(
                    icon = IconClose,
                    tooltipLabel = Res.string.common_close_fullscreen,
                    onClick = { isTopBarVisible = true },
                    iconButtonStyle = defaultSongbookIconButtonStyle().copy(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            .copy(alpha = 0.7f),
                        contentColor = MaterialTheme.colorScheme.primary,
                        outlineColor = Color.Transparent,
                    ),
                )
            }
        }
    }

    var isWrapDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isDisplayModeDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isTextScaleDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isKeyOffsetDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isBroadcastToTeamDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isConfirmDeleteDialogVisible by rememberSaveable { mutableStateOf(false) }

    LyricsOptionsBottomSheet(
        show = isBottomSheetVisible,
        state = state,
        onDismissRequest = { isBottomSheetVisible = false },
        onAction = {
            isBottomSheetVisible = false

            when (it) {
                Edit -> onEditSongClicked()
                Fullscreen -> isTopBarVisible = !isTopBarVisible
                WrapMode -> isWrapDialogVisible = true
                DisplayMode -> isDisplayModeDialogVisible = true
                TextScale -> isTextScaleDialogVisible = true
                KeyOffset -> isKeyOffsetDialogVisible = true
                AddToSetlist -> {}
                ShowQueue -> {}
                Broadcast -> isBroadcastToTeamDialogVisible = true
                Delete -> isConfirmDeleteDialogVisible = true
            }
        },
    )

    if (isWrapDialogVisible) {
        WrapModeDialog(
            mode = state.wrapMode,
            onModeSelected = {
                onWrapModeChanged(it)
                isWrapDialogVisible = false
            },
            onDismissRequest = { isWrapDialogVisible = false },
        )
    }

    if (isDisplayModeDialogVisible) {
        DisplayModeDialog(
            mode = state.displayMode,
            onModeSelected = {
                onDisplayModeChanged(it)
                isDisplayModeDialogVisible = false
            },
            onDismissRequest = { isDisplayModeDialogVisible = false },
        )
    }

    if (isTextScaleDialogVisible) {
        TextScaleDialog(
            textScale = state.textScale,
            minTextScale = MIN_ZOOM,
            maxTextScale = MAX_ZOOM,
            onTextScaleSelected = {
                onTextScaleChanged(it)
                isTextScaleDialogVisible = false
            },
            onDismissRequest = { isTextScaleDialogVisible = false },
        )
    }

    if (isKeyOffsetDialogVisible) {
        KeyOffsetDialog(
            keyOffset = state.keyOffset,
            onKeyOffsetSelected = {
                onKeyOffsetChanged(it)
                isKeyOffsetDialogVisible = false
            },
            onDismissRequest = { isKeyOffsetDialogVisible = false },
        )
    }

    if (isBroadcastToTeamDialogVisible) {
        ConfirmBroadcastToTeamDialog(
            onConfirmClicked = {
                onBroadcastToTeamConfirmClicked()
                isBroadcastToTeamDialogVisible = false
            },
            onDismissRequest = { isBroadcastToTeamDialogVisible = false },
        )
    }

    if (isConfirmDeleteDialogVisible) {
        ConfirmDeleteDialog(
            title = Res.string.lyrics_delete_song,
            description = Res.string.lyrics_delete_song_description,
            onConfirmClicked = {
                onDeleteSongConfirmClicked()
                isConfirmDeleteDialogVisible = false
                isBottomSheetVisible = false
            },
            onDismissRequest = { isConfirmDeleteDialogVisible = false },
        )
    }
}
