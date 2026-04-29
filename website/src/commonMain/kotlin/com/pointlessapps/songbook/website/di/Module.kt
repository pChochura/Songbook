package com.pointlessapps.songbook.website.di

import com.pointlessapps.songbook.website.RemoveAccountViewModel
import com.pointlessapps.songbook.website.auth.di.authModule
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val websiteModule = module {
    includes(authModule)
    viewModelOf(::RemoveAccountViewModel)
}
