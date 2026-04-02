package com.pointlessapps.songbook.core.prefs.di

import com.pointlessapps.songbook.core.prefs.PrefsRepository
import com.pointlessapps.songbook.core.prefs.PrefsRepositoryImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal val prefsModule = module {
    singleOf(::PrefsRepositoryImpl).bind<PrefsRepository>()
}
