package com.pointlessapps.songbook.website

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeViewport
import com.pointlessapps.songbook.core.di.coreModule
import com.pointlessapps.songbook.di.uiModule
import com.pointlessapps.songbook.ui.components.SongbookScaffoldLayout
import com.pointlessapps.songbook.ui.components.SongbookSnackbar
import com.pointlessapps.songbook.ui.theme.SongbookTheme
import com.pointlessapps.songbook.ui.theme.spacing
import com.pointlessapps.songbook.utils.SongbookSnackbarState
import com.pointlessapps.songbook.website.di.websiteModule
import com.pointlessapps.songbook.website.ui.RemoveAccountScreen
import kotlinx.browser.document
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.context.startKoin
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    startKoin {
        modules(websiteModule, uiModule, coreModule)
    }

    ComposeViewport(document.body!!) {
        val snackbarSate = koinInject<SongbookSnackbarState>()

        SongbookTheme {
            SongbookScaffoldLayout(
                modifier = Modifier.fillMaxSize(),
                fab = {
                    AnimatedContent(
                        targetState = snackbarSate.currentSnackbarData,
                        transitionSpec = {
                            fadeIn() + slideInVertically { it / 2 } togetherWith
                                    fadeOut() + slideOutVertically { it / 2 } using null
                        },
                        contentAlignment = Alignment.Center,
                    ) {
                        it?.let {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center,
                            ) {
                                SongbookSnackbar(
                                    modifier = Modifier
                                        .widthIn(max = 600.dp)
                                        .padding(MaterialTheme.spacing.extraLarge * 2),
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
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center,
                ) {
                    RemoveAccountScreen(
                        viewModel = koinViewModel {
                            parametersOf(document.URL)
                        },
                    )
                }
            }
        }
    }
}
