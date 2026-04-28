package com.pointlessapps.songbook.core.setlist.di

import com.pointlessapps.songbook.core.setlist.SetlistRepository
import com.pointlessapps.songbook.core.setlist.SetlistRepositoryImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal val setlistModule = module {
    singleOf(::SetlistRepositoryImpl).bind<SetlistRepository>()
}
