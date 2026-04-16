package com.pointlessapps.songbook.core.app.di

import com.pointlessapps.songbook.core.app.AndroidAppRepository
import com.pointlessapps.songbook.core.app.AndroidAppViewModel
import com.pointlessapps.songbook.core.app.AppRepository
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal val appModule = module {
    viewModelOf(::AndroidAppViewModel)
    singleOf(::AndroidAppRepository).bind<AppRepository>()
}
