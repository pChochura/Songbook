package com.pointlessapps.songbook.introduction.di

import com.pointlessapps.songbook.Route
import com.pointlessapps.songbook.introduction.IntroductionViewModel
import com.pointlessapps.songbook.introduction.ui.IntroductionScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
internal val introductionModule = module {
    viewModelOf(::IntroductionViewModel)

    navigation<Route.Introduction> {
        IntroductionScreen(
            viewModel = koinViewModel(),
        )
    }
}
