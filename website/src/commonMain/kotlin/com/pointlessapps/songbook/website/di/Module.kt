package com.pointlessapps.songbook.website.di

import com.pointlessapps.songbook.core.auth.GoogleAuthManager
import com.pointlessapps.songbook.core.auth.GoogleTokens
import com.pointlessapps.songbook.website.RemoveAccountViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val websiteModule = module {
    viewModelOf(::RemoveAccountViewModel)
}
