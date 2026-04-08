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
import com.pointlessapps.songbook.lyrics.LyricsEvent
import com.pointlessapps.songbook.lyrics.LyricsViewModel
import com.pointlessapps.songbook.lyrics.LyricsViewModel.Companion.MAX_ZOOM
import com.pointlessapps.songbook.lyrics.LyricsViewModel.Companion.MIN_ZOOM
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
import com.pointlessapps.songbook.lyrics.ui.components.dialogs.ConfirmDeleteDialog
import com.pointlessapps.songbook.lyrics.ui.components.dialogs.DisplayModeDialog
import com.pointlessapps.songbook.lyrics.ui.components.dialogs.KeyOffsetDialog
import com.pointlessapps.songbook.lyrics.ui.components.dialogs.TextScaleDialog
import com.pointlessapps.songbook.lyrics.ui.components.dialogs.WrapModeDialog
import com.pointlessapps.songbook.preview.ui.PreviewSongLayout
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.common_close_fullscreen
import com.pointlessapps.songbook.shared.lyrics_section_label
import com.pointlessapps.songbook.ui.TopBar
import com.pointlessapps.songbook.ui.TopBarButton
import com.pointlessapps.songbook.ui.components.SongbookIconButton
import com.pointlessapps.songbook.ui.components.SongbookLoader
import com.pointlessapps.songbook.ui.components.SongbookScaffoldLayout
import com.pointlessapps.songbook.ui.components.defaultSongbookIconButtonStyle
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
    var isTopBarVisible by rememberSaveable { mutableStateOf(true) }
    var isBottomSheetVisible by rememberSaveable { mutableStateOf(false) }

    viewModel.events.collectWithLifecycle { event ->
        when (event) {
            is LyricsEvent.NavigateBack -> navigator.navigateBack()
            is LyricsEvent.NavigateToImportSong -> {
                navigator.navigateToImportSong(
                    id = event.songId,
                    title = event.title,
                    artist = event.artist,
                    lyrics = event.lyrics,
                )
            }
        }
    }

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
                            onClick = { navigator.navigateBack() },
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
                onTextScaleChanged = viewModel::onTextScaleChanged,
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
                Edit -> viewModel.onEditSongClicked()
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
                viewModel.onWrapModeChanged(it)
                isWrapDialogVisible = false
            },
            onDismissRequest = { isWrapDialogVisible = false },
        )
    }

    if (isDisplayModeDialogVisible) {
        DisplayModeDialog(
            mode = state.displayMode,
            onModeSelected = {
                viewModel.onDisplayModeChanged(it)
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
                viewModel.onTextScaleChanged(it)
                isTextScaleDialogVisible = false
            },
            onDismissRequest = { isTextScaleDialogVisible = false },
        )
    }

    if (isKeyOffsetDialogVisible) {
        KeyOffsetDialog(
            keyOffset = state.keyOffset,
            onKeyOffsetSelected = {
                viewModel.onKeyOffsetChanged(it)
                isKeyOffsetDialogVisible = false
            },
            onDismissRequest = { isKeyOffsetDialogVisible = false },
        )
    }

    if (isBroadcastToTeamDialogVisible) {
        ConfirmBroadcastToTeamDialog(
            onConfirmClicked = {
                viewModel.broadcastSongToTeam()
                isBroadcastToTeamDialogVisible = false
            },
            onDismissRequest = { isBroadcastToTeamDialogVisible = false },
        )
    }

    if (isConfirmDeleteDialogVisible) {
        ConfirmDeleteDialog(
            onConfirmClicked = {
                viewModel.deleteSong()
                isConfirmDeleteDialogVisible = false
                isBottomSheetVisible = false
            },
            onDismissRequest = { isConfirmDeleteDialogVisible = false },
        )
    }

    SongbookLoader(state.isLoading)
}
