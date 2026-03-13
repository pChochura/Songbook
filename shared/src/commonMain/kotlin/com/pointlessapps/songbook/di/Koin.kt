package com.pointlessapps.songbook.di

import com.pointlessapps.songbook.library.di.libraryModule
import com.pointlessapps.songbook.lyrics.di.lyricsModule
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

fun initKoin(appDeclaration: KoinAppDeclaration = {}, platformModules: List<Module> = emptyList()) {
    startKoin {
        appDeclaration()
        modules(databaseModule + lyricsModule + libraryModule + platformModules)
    }
}
