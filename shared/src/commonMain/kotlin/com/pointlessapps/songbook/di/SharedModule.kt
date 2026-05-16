package com.pointlessapps.songbook.di

import com.pointlessapps.songbook.AppViewModel
import com.pointlessapps.songbook.importsong.di.importSongModule
import com.pointlessapps.songbook.introduction.di.introductionModule
import com.pointlessapps.songbook.library.di.libraryModule
import com.pointlessapps.songbook.lyrics.di.lyricsModule
import com.pointlessapps.songbook.preview.di.previewModule
import com.pointlessapps.songbook.search.di.searchModule
import com.pointlessapps.songbook.setlist.di.setlistModule
import com.pointlessapps.songbook.settings.di.settingsModule
import com.pointlessapps.songbook.utils.SongOptionsBottomSheetDelegate
import com.pointlessapps.songbook.utils.SongOptionsBottomSheetDelegateImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
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
            setlistRepository = get(),
            snackbarState = get(),
        )
    }

    factoryOf(::SongOptionsBottomSheetDelegateImpl).bind<SongOptionsBottomSheetDelegate>()

    includes(
        introductionModule,
        libraryModule,
        settingsModule,
        lyricsModule,
        searchModule,
        setlistModule,
        previewModule,
        importSongModule,
    )
}
