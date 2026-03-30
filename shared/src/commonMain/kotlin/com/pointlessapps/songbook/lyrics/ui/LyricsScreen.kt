package com.pointlessapps.songbook.lyrics.ui

import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.pointlessapps.songbook.LocalNavigator
import com.pointlessapps.songbook.lyrics.LyricsEvent
import com.pointlessapps.songbook.lyrics.LyricsViewModel
import com.pointlessapps.songbook.lyrics.LyricsViewModel.Companion.MAX_ZOOM
import com.pointlessapps.songbook.lyrics.LyricsViewModel.Companion.MIN_ZOOM
import com.pointlessapps.songbook.lyrics.ui.components.LyricsOptionsBottomSheet
import com.pointlessapps.songbook.lyrics.ui.components.LyricsOptionsBottomSheetAction.AddToSetlist
import com.pointlessapps.songbook.lyrics.ui.components.LyricsOptionsBottomSheetAction.Broadcast
import com.pointlessapps.songbook.lyrics.ui.components.LyricsOptionsBottomSheetAction.Delete
import com.pointlessapps.songbook.lyrics.ui.components.LyricsOptionsBottomSheetAction.Edit
import com.pointlessapps.songbook.lyrics.ui.components.LyricsOptionsBottomSheetAction.Mode
import com.pointlessapps.songbook.lyrics.ui.components.LyricsOptionsBottomSheetAction.ShowQueue
import com.pointlessapps.songbook.lyrics.ui.components.LyricsOptionsBottomSheetAction.TextScale
import com.pointlessapps.songbook.lyrics.ui.components.SongHeader
import com.pointlessapps.songbook.lyrics.ui.components.TextScaleOverlay
import com.pointlessapps.songbook.lyrics.ui.components.dialogs.ConfirmBroadcastToTeamDialog
import com.pointlessapps.songbook.lyrics.ui.components.dialogs.ConfirmDeleteDialog
import com.pointlessapps.songbook.lyrics.ui.components.dialogs.ModeDialog
import com.pointlessapps.songbook.lyrics.ui.components.dialogs.TextScaleDialog
import com.pointlessapps.songbook.lyrics.ui.components.lyricsSection
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.common_back
import com.pointlessapps.songbook.shared.common_menu
import com.pointlessapps.songbook.shared.lyrics_section_label
import com.pointlessapps.songbook.ui.TopBar
import com.pointlessapps.songbook.ui.TopBarButton
import com.pointlessapps.songbook.ui.components.SongbookScaffoldLayout
import com.pointlessapps.songbook.ui.theme.IconArrowLeft
import com.pointlessapps.songbook.ui.theme.IconMoveHandle
import com.pointlessapps.songbook.ui.theme.spacing
import com.pointlessapps.songbook.utils.add
import com.pointlessapps.songbook.utils.collectWithLifecycle
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LyricsScreen(
    viewModel: LyricsViewModel,
) {
    val state = viewModel.state
    val navigator = LocalNavigator.current
    var isBottomSheetVisible by rememberSaveable { mutableStateOf(false) }

    val transformableState = rememberTransformableState { zoomChange, _, _ ->
        viewModel.onTextScaleChanged((state.textScale * zoomChange).roundToInt())
    }

    viewModel.events.collectWithLifecycle { event ->
        when (event) {
            LyricsEvent.NavigateBack -> navigator.navigateBack()
        }
    }

    SongbookScaffoldLayout(
        topBar = @Composable {
            TopBar(
                leftButton = TopBarButton(
                    icon = IconArrowLeft,
                    tooltip = Res.string.common_back,
                    onClick = { navigator.navigateBack() },
                ),
                rightButton = TopBarButton(
                    icon = IconMoveHandle,
                    tooltip = Res.string.common_menu,
                    onClick = { isBottomSheetVisible = true },
                ),
                title = Res.string.lyrics_section_label,
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .transformable(state = transformableState, canPan = { false }),
            contentAlignment = Alignment.TopCenter,
        ) {
            val horizontalScrollState = rememberScrollState()
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = MAX_WIDTH)
                    .fillMaxSize()
                    .horizontalScroll(horizontalScrollState),
                contentPadding = paddingValues.add(
                    all = MaterialTheme.spacing.huge,
                ),
                verticalArrangement = Arrangement.spacedBy(
                    space = MaterialTheme.spacing.small,
                    alignment = Alignment.Top,
                ),
                horizontalAlignment = Alignment.Start,
            ) {
                item(key = "header") {
                    SongHeader(
                        modifier = Modifier.graphicsLayer {
                            translationX = horizontalScrollState.value.toFloat()
                        },
                        title = state.title,
                        artist = state.artist,
                    )
                }

                item { Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall)) }

                state.sections.forEach { lyricsSection(it, state.textScale) }
            }

            TextScaleOverlay(
                show = transformableState.isTransformInProgress,
                textScale = state.textScale,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }

    var isModeDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isTextScaleDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isBroadcastToTeamDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isConfirmDeleteDialogVisible by rememberSaveable { mutableStateOf(false) }

    LyricsOptionsBottomSheet(
        show = isBottomSheetVisible,
        state = state,
        onDismissRequest = { isBottomSheetVisible = false },
        onAction = {
            when (it) {
                Edit -> TODO()
                Mode -> isModeDialogVisible = true
                TextScale -> isTextScaleDialogVisible = true
                AddToSetlist -> TODO()
                ShowQueue -> TODO()
                Broadcast -> isBroadcastToTeamDialogVisible = true
                Delete -> isConfirmDeleteDialogVisible = true
            }
        },
    )

    if (isModeDialogVisible) {
        ModeDialog(
            mode = state.mode,
            onModeSelected = {
                viewModel.onModeChanged(it)
                isModeDialogVisible = false
            },
            onDismissRequest = { isModeDialogVisible = false },
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
            },
            onDismissRequest = { isConfirmDeleteDialogVisible = false },
        )
    }
}

private val MAX_WIDTH = 800.dp
