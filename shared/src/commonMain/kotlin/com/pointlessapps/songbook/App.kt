package com.pointlessapps.songbook

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.rememberNavBackStack
import com.pointlessapps.songbook.ui.BottomBar
import com.pointlessapps.songbook.ui.components.SongbookScaffoldLayout
import com.pointlessapps.songbook.ui.components.SongbookSnackbar
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
    val viewModel = koinViewModel<AppViewModel> {
        parametersOf(openSearch, initialFilterLetter)
    }
    val snackbarSate = koinInject<SongbookSnackbarState>()
    val backstack = rememberNavBackStack(navigationConfig, elements = viewModel.startingRoutes)
    val navigator = remember { Navigator(backstack) }
    val bottomBarPadding = remember { BottomBarPadding() }

    LaunchedEffect(initialFilterLetter, openSearch) {
        if (openSearch) {
            navigator.bottomNavigationTo(Route.Search)
        } else if (initialFilterLetter != null) {
            navigator.bottomNavigationTo(Route.Library(initialFilterLetter))
        }
    }

    viewModel.state.collectWithLifecycle()

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
                fab = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = MaterialTheme.spacing.extraLarge)
                            .navigationBarsPadding(),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        AnimatedVisibility(
                            visible = navigator.currentRoute?.hasBottomBar == true,
                            enter = slideInVertically { it / 2 } + fadeIn(),
                            exit = slideOutVertically { it / 2 } + fadeOut(),
                        ) {
                            BottomBar(
                                currentRoute = { navigator.currentRoute },
                                onNavigateTo = navigator::bottomNavigationTo,
                                onActiveClicked = {
                                    // TODO
                                },
                                onLongClicked = {
                                    // TODO
                                },
                            )
                        }

                        AnimatedContent(
                            targetState = snackbarSate.currentSnackbarData,
                            transitionSpec = {
                                fadeIn() + expandIn(expandFrom = Alignment.Center) togetherWith
                                        fadeOut() + shrinkOut(shrinkTowards = Alignment.Center) using null
                            },
                            contentAlignment = Alignment.Center,
                        ) {
                            it?.let {
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

internal val LocalBottomBarPadding: ProvidableCompositionLocal<BottomBarPadding> =
    staticCompositionLocalOf { error("LocalBottomBarPadding not initialized") }

internal class BottomBarPadding {
    val padding = mutableStateOf(0.dp)
}
