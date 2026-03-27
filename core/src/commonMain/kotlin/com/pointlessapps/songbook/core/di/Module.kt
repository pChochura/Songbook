package com.pointlessapps.songbook.core.di

import com.pointlessapps.songbook.core.repository.SongRepository
import com.pointlessapps.songbook.core.repository.SongRepositoryImpl
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal expect val platformModule: Module

val coreModule = module {
    includes(platformModule)

    singleOf(::SongRepositoryImpl).bind<SongRepository>()
}
