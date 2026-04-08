package com.pointlessapps.songbook.preview.di

import com.pointlessapps.songbook.Route
import com.pointlessapps.songbook.preview.ui.PreviewSongScreen
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
internal val previewModule = module {
    navigation<Route.PreviewSong> {
        PreviewSongScreen(
            title = it.title,
            artist = it.artist,
            sections = it.sections,
        )
    }
}
