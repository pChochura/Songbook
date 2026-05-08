package com.pointlessapps.songbook

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.Down
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.Up
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.rememberNavBackStack
import com.pointlessapps.songbook.core.song.model.Song
import com.pointlessapps.songbook.shared.ui.Res
import com.pointlessapps.songbook.shared.ui.common_dismiss
import com.pointlessapps.songbook.ui.NavigationBottomBar
import com.pointlessapps.songbook.ui.components.SongbookIconButton
import com.pointlessapps.songbook.ui.components.SongbookScaffoldLayout
import com.pointlessapps.songbook.ui.components.SongbookSnackbar
import com.pointlessapps.songbook.ui.theme.IconClose
import com.pointlessapps.songbook.ui.theme.SongbookTheme
import com.pointlessapps.songbook.ui.theme.spacing
import com.pointlessapps.songbook.utils.SongbookSnackbarCallbackAction.AddSongToSetlist
import com.pointlessapps.songbook.utils.SongbookSnackbarCallbackAction.LoadToQueueAndOpen
import com.pointlessapps.songbook.utils.SongbookSnackbarState
import com.pointlessapps.songbook.utils.collectWithLifecycle
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun App(
    initialFilterLetter: String? = null,
    openSearch: Boolean = false,
) {
    val viewModel = koinViewModel<AppViewModel>(key = "{$initialFilterLetter$openSearch}") {
        parametersOf(openSearch, initialFilterLetter)
    }
    val snackbarSate = koinInject<SongbookSnackbarState>()
    val backstack = key(viewModel.startingRoutes) {
        rememberNavBackStack(navigationConfig, elements = viewModel.startingRoutes)
    }
    val navigator = remember(backstack) { Navigator(backstack) }
    val bottomBarPadding = remember { BottomBarPadding() }

    val currentlyPlayedSong by viewModel.currentlyPlayedSongState.collectAsStateWithLifecycle()
    viewModel.initializationState.collectWithLifecycle()

    snackbarSate.callbackActionsFlow.collectWithLifecycle {
        when (it) {
            is LoadToQueueAndOpen -> viewModel.openSong(it.songId)
            is AddSongToSetlist -> viewModel.addSongToSetlist(it.setlistId, it.songId, it.order)
        }
    }

    SongbookTheme {
        CompositionLocalProvider(
            LocalNavigator provides navigator,
            LocalBottomBarPadding provides bottomBarPadding,
            LocalTextSelectionColors provides TextSelectionColors(
                handleColor = MaterialTheme.colorScheme.onSurfaceVariant,
                backgroundColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            ),
        ) {
            SongbookScaffoldLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                fab = @Composable {
                    BottomBarLayout(
                        navigator = navigator,
                        snackbarSate = snackbarSate,
                        currentlyPlayedSong = currentlyPlayedSong,
                        onClearSongClicked = viewModel::clearCurrentlyPlayedSong,
                    )
                },
            ) { paddingValues ->
                LaunchedEffect(paddingValues) {
                    bottomBarPadding.padding.value = paddingValues.calculateBottomPadding()
                }

                NavDisplay(navigator)
            }
        }
    }
}

@Composable
private fun BottomBarLayout(
    navigator: Navigator,
    snackbarSate: SongbookSnackbarState,
    currentlyPlayedSong: Song?,
    onClearSongClicked: () -> Unit,
) {
    val shouldShowNavigationBottomBar by remember {
        derivedStateOf { navigator.currentRoute?.hasBottomBar == true }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AnimatedVisibility(
                visible = shouldShowNavigationBottomBar,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 },
            ) {
                NavigationBottomBar(
                    currentRoute = navigator::currentRoute,
                    onNavigateTo = navigator::bottomNavigationTo,
                )
            }

            AnimatedContent(
                targetState = snackbarSate.currentSnackbarData,
                transitionSpec = {
                    fadeIn() + scaleIn(initialScale = 0.5f) togetherWith
                            fadeOut() + scaleOut(targetScale = 0.5f) using SizeTransform(clip = false)
                },
                contentAlignment = Alignment.BottomCenter,
            ) {
                if (it != null) {
                    SongbookSnackbar(
                        message = it.visuals.message,
                        icon = it.visuals.icon,
                        actionLabel = it.visuals.actionLabel,
                        actionCallback = it::performAction,
                        onDismissRequest = it::dismiss,
                    )
                }
            }
        }

        AnimatedContent(
            targetState = currentlyPlayedSong to shouldShowNavigationBottomBar,
            contentKey = { (song, show) -> song?.id to show },
            transitionSpec = {
                slideIntoContainer(Up) togetherWith
                        slideOutOfContainer(Down) using SizeTransform(clip = false)
            },
            contentAlignment = Alignment.BottomCenter,
        ) { (song, show) ->
            if (show && song != null) {
                CurrentlyPlayingBottomBar(
                    song = song,
                    onClearSongClicked = onClearSongClicked,
                )
            } else {
                Spacer(
                    Modifier.windowInsetsBottomHeight(
                        WindowInsets.navigationBars.add(
                            WindowInsets(bottom = MaterialTheme.spacing.extraLarge),
                        ),
                    ),
                )
            }
        }
    }
}

@Composable
private fun CurrentlyPlayingBottomBar(
    song: Song,
    onClearSongClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .navigationBarsPadding()
            .padding(MaterialTheme.spacing.large),
    ) {
        SongbookIconButton(
            icon = IconClose,
            tooltipLabel = Res.string.common_dismiss,
            onClick = onClearSongClicked,
        )
    }
}

internal val LocalBottomBarPadding: ProvidableCompositionLocal<BottomBarPadding> =
    staticCompositionLocalOf { error("LocalBottomBarPadding not initialized") }

internal class BottomBarPadding {
    val padding = mutableStateOf(0.dp)

    companion object {
        @Composable
        fun Modifier.bottomBarHeight(): Modifier {
            val padding by LocalBottomBarPadding.current.padding
            val animatedPadding by animateDpAsState(padding)
            return this then padding(bottom = animatedPadding)
        }
    }
}
