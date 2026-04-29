package com.pointlessapps.songbook.core.app.di

import com.pointlessapps.songbook.core.BuildKonfig
import com.pointlessapps.songbook.core.app.AndroidAppRepository
import com.pointlessapps.songbook.core.app.AndroidAppViewModel
import com.pointlessapps.songbook.core.app.AppRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val appModule = module {
    viewModelOf(::AndroidAppViewModel)
    single<AppRepository> {
        AndroidAppRepository(
            context = androidContext(),
            removeAccountUrl = BuildKonfig.REMOVE_ACCOUNT_WEBSITE_URL.orEmpty(),
        )
    }
}
