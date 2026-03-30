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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.pointlessapps.songbook.LocalNavigator
import com.pointlessapps.songbook.lyrics.LyricsViewModel
import com.pointlessapps.songbook.lyrics.ui.components.SongHeader
import com.pointlessapps.songbook.lyrics.ui.components.TextScaleOverlay
import com.pointlessapps.songbook.lyrics.ui.components.lyricsSection
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.common_back
import com.pointlessapps.songbook.shared.common_menu
import com.pointlessapps.songbook.shared.lyrics_section_label
import com.pointlessapps.songbook.ui.OptionsBottomSheet
import com.pointlessapps.songbook.ui.OptionsBottomSheetItem
import com.pointlessapps.songbook.ui.OptionsBottomSheetTitleHeader
import com.pointlessapps.songbook.ui.TopBar
import com.pointlessapps.songbook.ui.TopBarButton
import com.pointlessapps.songbook.ui.components.SongbookScaffoldLayout
import com.pointlessapps.songbook.ui.theme.IconArrowLeft
import com.pointlessapps.songbook.ui.theme.IconMoveHandle
import com.pointlessapps.songbook.ui.theme.spacing
import com.pointlessapps.songbook.utils.add
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LyricsScreen(
    viewModel: LyricsViewModel,
) {
    val state = viewModel.state
    val navigator = LocalNavigator.current
    var isBottomSheetVisible by remember { mutableStateOf(false) }

    val transformableState = rememberTransformableState { zoomChange, _, _ ->
        viewModel.onFontScaleChanged(state.fontScale * zoomChange)
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

                state.sections.forEach { lyricsSection(it, state.fontScale) }
            }

            TextScaleOverlay(
                show = transformableState.isTransformInProgress,
                fontScale = state.fontScale,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }

    if (isBottomSheetVisible) {
        OptionsBottomSheet(
            state = rememberModalBottomSheetState(),
            onDismissRequest = { isBottomSheetVisible = false },
            header = { OptionsBottomSheetTitleHeader(stringResource(Res.string.common_menu)) },
            items = listOf(
                OptionsBottomSheetItem.new(
                    label = Res.string.common_menu,
                    onClick = {

                    },
                ),
            ),
        )
    }
}

private val MAX_WIDTH = 800.dp
