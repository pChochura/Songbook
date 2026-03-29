package com.pointlessapps.songbook

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.pointlessapps.songbook.ui.BottomBar
import com.pointlessapps.songbook.ui.components.LocalSnackbarHostState
import com.pointlessapps.songbook.ui.components.SongbookScaffoldLayout
import com.pointlessapps.songbook.ui.components.rememberSongbookSnackbarHostState
import com.pointlessapps.songbook.ui.theme.SongbookTheme

@Composable
fun App(
    initialFilterLetter: String? = null,
    openSearch: Boolean = false,
) {
    val songbookSnackbarHostState = rememberSongbookSnackbarHostState()
    val backstack: NavBackStack<NavKey> = rememberNavBackStack(
        configuration = navigationConfig,
        Route.Library(
            initialFilterLetter = initialFilterLetter,
            openSearch = openSearch,
        ),
    )
    val navigator = Navigator(backstack)

    SongbookTheme {
        CompositionLocalProvider(
            LocalNavigator provides navigator,
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
                                onNavigateTo = {
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
                CompositionLocalProvider(LocalInnerPadding provides paddingValues) {
                    NavDisplay(backstack)
                }
            }
        }
    }
}

internal val LocalInnerPadding: ProvidableCompositionLocal<PaddingValues> =
    staticCompositionLocalOf { error("LocalInnerPadding not initialized") }
