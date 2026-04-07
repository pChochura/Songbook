package com.pointlessapps.songbook

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.pointlessapps.songbook.ui.BottomBar
import com.pointlessapps.songbook.ui.components.LocalSnackbarHostState
import com.pointlessapps.songbook.ui.components.SongbookLoader
import com.pointlessapps.songbook.ui.components.SongbookScaffoldLayout
import com.pointlessapps.songbook.ui.components.rememberSongbookSnackbarHostState
import com.pointlessapps.songbook.ui.theme.SongbookTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(
    initialFilterLetter: String? = null,
    openSearch: Boolean = false,
    viewModel: AppViewModel = koinViewModel(),
) {
    val songbookSnackbarHostState = rememberSongbookSnackbarHostState()
    val backstack: NavBackStack<NavKey> = rememberNavBackStack(
        configuration = navigationConfig,
        Route.Library,
    )
    val navigator = Navigator(backstack)
    val bottomBarPadding = remember { BottomBarPadding() }

    val state by viewModel.state.collectAsStateWithLifecycle()

    SongbookTheme {
        CompositionLocalProvider(
            LocalNavigator provides navigator,
            LocalBottomBarPadding provides bottomBarPadding,
            LocalSnackbarHostState provides songbookSnackbarHostState,
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
                    AnimatedContent(
                        targetState = navigator.currentRoute,
                        contentKey = { it?.hasBottomBar == true },
                        transitionSpec = {
                            (fadeIn() + slideInVertically(initialOffsetY = { it / 2 }))
                                .togetherWith(fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })) using null
                        },
                    ) {
                        if (it?.hasBottomBar == true) {
                            BottomBar(
                                currentRoute = { it },
                                onNavigateTo = navigator::bottomNavigationTo,
                                onActiveClicked = {
                                    // TODO
                                },
                                onLongClicked = {
                                    // TODO
                                },
                            )
                        }
                    }
                },
            ) { paddingValues ->
                LaunchedEffect(paddingValues) {
                    bottomBarPadding.padding.value = paddingValues.calculateBottomPadding()
                }

                AnimatedContent(
                    targetState = state,
                    transitionSpec = { fadeIn() togetherWith fadeOut() using null },
                ) { state ->
                    if (state.isLoading || state.error != null) {
                        SongbookLoader(true, scrimAlpha = 1.0f)
                    } else {
                        NavDisplay(backstack)
                    }
                }
            }
        }
    }
}

internal val LocalBottomBarPadding: ProvidableCompositionLocal<BottomBarPadding> =
    staticCompositionLocalOf { error("LocalBottomBarPadding not initialized") }

internal class BottomBarPadding {
    val padding = mutableStateOf(0.dp)
}
