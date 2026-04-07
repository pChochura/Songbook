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
    viewModel {
        LibraryViewModel(
            setlistRepository = get(),
            songRepository = get(),
        )
    }

    viewModel { (id: Long?, title: String?, artist: String?, lyrics: String?) ->
        ImportSongViewModel(
            id = id,
            title = title,
            artist = artist,
            lyrics = lyrics,
            agent = get(named("Gemini")),
            setlistRepository = get(),
            songRepository = get(),
            appRepository = get(),
        )
    }

    navigation<Route.Library> {
        LibraryScreen(
            viewModel = koinViewModel(),
        )
    }

    navigation<Route.ImportSong> { route ->
        ImportSongScreen(
            viewModel = koinViewModel(key = route.toString()) {
                parametersOf(route.id, route.title, route.artist, route.lyrics)
            },
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
