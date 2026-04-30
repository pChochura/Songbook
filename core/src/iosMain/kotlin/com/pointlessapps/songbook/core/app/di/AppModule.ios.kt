package com.pointlessapps.songbook.core.app.di

import com.pointlessapps.songbook.core.BuildKonfig
import com.pointlessapps.songbook.core.app.AppRepository
import com.pointlessapps.songbook.core.app.IosAppRepository
import org.koin.dsl.module

internal val appModule = module {
    single<AppRepository> {
        IosAppRepository(
            removeAccountUrl = BuildKonfig.REMOVE_ACCOUNT_WEBSITE_URL.orEmpty(),
        )
    }
}
