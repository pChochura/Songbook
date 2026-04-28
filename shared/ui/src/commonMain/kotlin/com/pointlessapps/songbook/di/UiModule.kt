package com.pointlessapps.songbook.di

import com.pointlessapps.songbook.utils.SongbookSnackbarState
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val uiModule = module {
    singleOf(::SongbookSnackbarState)
}
