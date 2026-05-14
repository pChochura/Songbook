package com.pointlessapps.songbook.library.di

import com.pointlessapps.songbook.Route
import com.pointlessapps.songbook.library.LibraryViewModel
import com.pointlessapps.songbook.library.ui.LibraryScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
internal val libraryModule = module {
    viewModel { (initialFilterLetter: String?) ->
        LibraryViewModel(
            initialFilterLetter = initialFilterLetter,
            appRepository = get(),
            queueManager = get(),
            syncRepository = get(),
            songRepository = get(),
            setlistRepository = get(),
            prefsRepository = get(),
            authRepository = get(),
            snackbarState = get(),
            songOptionsDelegate = get(),
        )
    }

    navigation<Route.Library> { route ->
        LibraryScreen(
            viewModel = koinViewModel(key = route.toString()) {
                parametersOf(route.initialFilterLetter)
            },
        )
    }
}
