package com.pointlessapps.songbook.core.auth.di

import com.pointlessapps.songbook.core.auth.AuthRepository
import com.pointlessapps.songbook.core.auth.AuthRepositoryImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal val authModule = module {
    singleOf(::AuthRepositoryImpl).bind<AuthRepository>()
}
