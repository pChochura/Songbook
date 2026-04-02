package com.pointlessapps.songbook.search.di

import com.pointlessapps.songbook.Route
import com.pointlessapps.songbook.search.SearchViewModel
import com.pointlessapps.songbook.search.ui.SearchScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
internal val searchModule = module {
    viewModelOf(::SearchViewModel)

    navigation<Route.Search> {
        SearchScreen(viewModel = koinViewModel())
    }
}
