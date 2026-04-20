package com.pointlessapps.songbook.importsong.di

import com.pointlessapps.songbook.Route
import com.pointlessapps.songbook.importsong.ImportSongViewModel
import com.pointlessapps.songbook.importsong.ui.ImportSongScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
internal val importSongModule = module {
    viewModel { (id: String?, title: String?, artist: String?, lyrics: String?) ->
        ImportSongViewModel(
            id = id,
            title = title,
            artist = artist,
            lyrics = lyrics,
            agent = get(named("Gemini")),
            setlistRepository = get(),
            songRepository = get(),
            prefsRepository = get(),
            appRepository = get(),
            snackbarState = get(),
        )
    }

    navigation<Route.ImportSong> { route ->
        ImportSongScreen(
            viewModel = koinViewModel(key = route.toString()) {
                parametersOf(route.id, route.title, route.artist, route.lyrics)
            },
        )
    }
}
