package com.pointlessapps.songbook.lyrics.di

import com.pointlessapps.songbook.Route
import com.pointlessapps.songbook.lyrics.LyricsViewModel
import com.pointlessapps.songbook.lyrics.ui.LyricsScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
internal val lyricsModule = module {
    viewModelOf(::LyricsViewModel)

    navigation<Route.Lyrics> { route ->
        LyricsScreen(
            viewModel = koinViewModel(key = route.toString()) {
                parametersOf(route.songId)
            },
        )
    }
}
