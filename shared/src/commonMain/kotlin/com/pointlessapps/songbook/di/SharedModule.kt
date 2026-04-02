package com.pointlessapps.songbook.di

import com.pointlessapps.songbook.library.di.libraryModule
import com.pointlessapps.songbook.lyrics.di.lyricsModule
import com.pointlessapps.songbook.search.di.searchModule
import org.koin.dsl.module

internal val sharedModule = module {
    includes(libraryModule, lyricsModule, searchModule)
}
