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
    viewModel { params ->
        LibraryViewModel(
            initialFilterLetter = params.getOrNull(),
            openSearch = params.getOrNull() ?: false,
            songDao = get(),
        )
    }

    navigation<Route.Library> { route ->
        LibraryScreen(
            viewModel = koinViewModel { parametersOf(route.initialFilterLetter, route.openSearch) },
        )
    }
}
