package com.pointlessapps.songbook.settings.di

import com.pointlessapps.songbook.Route
import com.pointlessapps.songbook.settings.SettingsViewModel
import com.pointlessapps.songbook.settings.ui.SettingsScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
internal val settingsModule = module {
    viewModelOf(::SettingsViewModel)

    navigation<Route.Settings> {
        SettingsScreen(
            viewModel = koinViewModel(),
        )
    }
}
