package com.pointlessapps.songbook.di

import com.pointlessapps.songbook.AppViewModel
import com.pointlessapps.songbook.library.di.libraryModule
import com.pointlessapps.songbook.lyrics.di.lyricsModule
import com.pointlessapps.songbook.search.di.searchModule
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val sharedModule = module {
    viewModelOf(::AppViewModel)

    includes(libraryModule, lyricsModule, searchModule)
}
