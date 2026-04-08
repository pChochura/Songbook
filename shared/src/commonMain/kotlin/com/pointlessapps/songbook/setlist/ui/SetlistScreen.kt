package com.pointlessapps.songbook.setlist.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pointlessapps.songbook.LocalBottomBarPadding
import com.pointlessapps.songbook.LocalNavigator
import com.pointlessapps.songbook.library.DisplayMode
import com.pointlessapps.songbook.library.ui.components.SongCard
import com.pointlessapps.songbook.setlist.SetlistEvent
import com.pointlessapps.songbook.setlist.SetlistViewModel
import com.pointlessapps.songbook.setlist.ui.components.SetlistOptionsBottomSheet
import com.pointlessapps.songbook.ui.TopBar
import com.pointlessapps.songbook.ui.TopBarButton
import com.pointlessapps.songbook.ui.components.SongbookScaffoldLayout
import com.pointlessapps.songbook.ui.theme.spacing
import com.pointlessapps.songbook.utils.add
import com.pointlessapps.songbook.utils.collectWithLifecycle

@Composable
internal fun SetlistScreen(
    viewModel: SetlistViewModel,
) {
    val navigator = LocalNavigator.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    var isBottomSheetVisible by rememberSaveable { mutableStateOf(false) }

    viewModel.events.collectWithLifecycle {
        when (it) {
            is SetlistEvent.NavigateBack -> navigator.navigateBack()
            is SetlistEvent.NavigateToLyrics -> navigator.navigateToLyrics(it.songId)
        }
    }

    SongbookScaffoldLayout(
        topBar = @Composable {
            val rotation = remember { Animatable(0f) }
            LaunchedEffect(Unit) {
                rotation.animateTo(360f, infiniteRepeatable(tween(3000)))
            }

            TopBar(
                leftButton = null,
                rightButton = TopBarButton.menu(
                    onClick = { isBottomSheetVisible = true },
                ),
                title = state.setlist?.name.orEmpty(),
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = paddingValues
                .add(MaterialTheme.spacing.huge)
                .add(bottom = LocalBottomBarPadding.current.padding.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
        ) {
            items(state.songs, key = { it.id }) { song ->
                SongCard(
                    modifier = Modifier.animateItem(),
                    song = song,
                    displayMode = DisplayMode.List,
                    onClick = { viewModel.onLyricsClicked(song.id) },
                )
            }
        }
    }

    SetlistOptionsBottomSheet(
        show = isBottomSheetVisible,
        state = state,
        onDismissRequest = { isBottomSheetVisible = false },
        onAction = {
            isBottomSheetVisible = false
        },
    )
}
