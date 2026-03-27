package com.pointlessapps.songbook.di

import com.pointlessapps.songbook.library.di.libraryModule
import com.pointlessapps.songbook.lyrics.di.lyricsModule
import org.koin.dsl.module

internal val module = module {
    includes(libraryModule, lyricsModule)
}
