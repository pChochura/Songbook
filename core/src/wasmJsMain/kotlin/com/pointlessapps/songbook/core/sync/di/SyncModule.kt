package com.pointlessapps.songbook.core.sync.di

import com.pointlessapps.songbook.core.sync.SyncRepository
import com.pointlessapps.songbook.core.sync.WasmSyncRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal val syncModule = module {
    singleOf(::WasmSyncRepository).bind<SyncRepository>()
}
