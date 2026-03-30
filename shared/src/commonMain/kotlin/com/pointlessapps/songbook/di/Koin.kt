package com.pointlessapps.songbook.di

import com.pointlessapps.songbook.core.di.coreModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(module, coreModule, aiModule)
    }
}
