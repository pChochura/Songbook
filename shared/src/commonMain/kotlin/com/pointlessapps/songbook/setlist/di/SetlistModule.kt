package com.pointlessapps.songbook.setlist.di

import com.pointlessapps.songbook.Route
import com.pointlessapps.songbook.setlist.SetlistViewModel
import com.pointlessapps.songbook.setlist.ui.SetlistScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
internal val setlistModule = module {
    viewModel { (id: Long) ->
        SetlistViewModel(
            id = id,
            syncRepository = get(),
            setlistRepository = get(),
            songRepository = get(),
            snackbarState = get(),
        )
    }

    navigation<Route.Setlist> { route ->
        SetlistScreen(
            viewModel = koinViewModel(key = route.toString()) {
                parametersOf(route.id)
            },
        )
    }
}
