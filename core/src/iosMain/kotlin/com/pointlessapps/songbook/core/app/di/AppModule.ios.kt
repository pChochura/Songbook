package com.pointlessapps.songbook.core.app.di

import com.pointlessapps.songbook.core.app.AppRepository
import com.pointlessapps.songbook.core.app.IosAppRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal val appModule = module {
    singleOf(::IosAppRepository).bind<AppRepository>()
}
