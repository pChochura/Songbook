package com.pointlessapps.songbook.library.di

import com.pointlessapps.songbook.Route
import com.pointlessapps.songbook.library.ImportSongViewModel
import com.pointlessapps.songbook.library.LibraryViewModel
import com.pointlessapps.songbook.library.ui.ImportSongScreen
import com.pointlessapps.songbook.library.ui.LibraryScreen
import com.pointlessapps.songbook.library.ui.PreviewSongScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
internal val libraryModule = module {
    viewModel { params ->
        LibraryViewModel(
            initialFilterLetter = params.getOrNull(),
            openSearch = params.getOrNull() ?: false,
            setlistRepository = get(),
            songRepository = get(),
            authRepository = get(),
        )
    }

    viewModel {
        ImportSongViewModel(
            agent = get(named("Gemini")),
            setlistRepository = get(),
            songRepository = get(),
            appRepository = get(),
        )
    }

    navigation<Route.Library> { route ->
        LibraryScreen(
            viewModel = koinViewModel { parametersOf(route.initialFilterLetter, route.openSearch) },
        )
    }

    navigation<Route.ImportSong> {
        ImportSongScreen(
            viewModel = koinViewModel(),
        )
    }

    navigation<Route.PreviewSong> {
        PreviewSongScreen(
            title = it.title,
            artist = it.artist,
            sections = it.sections,
        )
    }
}
