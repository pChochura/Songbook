package com.pointlessapps.songbook.library.di

import com.pointlessapps.songbook.Route
import com.pointlessapps.songbook.library.LibraryViewModel
import com.pointlessapps.songbook.library.ui.LibraryScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
internal val libraryModule = module {
    viewModelOf(::LibraryViewModel)

    navigation<Route.Library> {
        LibraryScreen(
            viewModel = koinViewModel(),
        )
    }
}
