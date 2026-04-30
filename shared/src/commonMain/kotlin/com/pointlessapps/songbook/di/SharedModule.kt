package com.pointlessapps.songbook.di

import com.pointlessapps.songbook.AppViewModel
import com.pointlessapps.songbook.importsong.di.importSongModule
import com.pointlessapps.songbook.introduction.di.introductionModule
import com.pointlessapps.songbook.library.di.libraryModule
import com.pointlessapps.songbook.lyrics.di.lyricsModule
import com.pointlessapps.songbook.preview.di.previewModule
import com.pointlessapps.songbook.search.di.searchModule
import com.pointlessapps.songbook.setlist.di.setlistModule
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

internal val sharedModule = module {
    viewModel { (openSearch: Boolean, initialFilterLetter: String?) ->
        AppViewModel(
            openSearch = openSearch,
            initialFilterLetter = initialFilterLetter,
            chordLibrary = get(),
            syncRepository = get(),
            authRepository = get(),
            queueManager = get(),
            songRepository = get(),
            setlistRepository = get(),
            snackbarState = get(),
        )
    }

    includes(
        introductionModule,
        libraryModule,
        lyricsModule,
        searchModule,
        setlistModule,
        previewModule,
        importSongModule,
    )
}
